package com.panasetskaia.charactersudoku.presentation.fragments

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.os.SystemClock
import android.view.*
import android.view.animation.LinearInterpolator
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.panasetskaia.charactersudoku.R
import com.panasetskaia.charactersudoku.databinding.FragmentGameBinding
import com.panasetskaia.charactersudoku.domain.entities.Cell
import com.panasetskaia.charactersudoku.presentation.MainActivity
import com.panasetskaia.charactersudoku.presentation.customViews.SudokuBoardView
import com.panasetskaia.charactersudoku.presentation.fragments.dialogFragments.ConfirmRefreshFragment
import com.panasetskaia.charactersudoku.presentation.viewmodels.GameViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class GameFragment : Fragment(), SudokuBoardView.OnTouchListener {

    private val linearInterpolator = LinearInterpolator()
    private lateinit var viewModel: GameViewModel
    private var _binding: FragmentGameBinding? = null
    private val binding: FragmentGameBinding
        get() = _binding ?: throw RuntimeException("FragmentGameBinding is null")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGameBinding.inflate(inflater, container, false)
        (activity as AppCompatActivity).setSupportActionBar(binding.appBar)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.sudokuBoard.registerListener(this)
        setupMenu()
        interactWithViewModel()
    }

    override fun onResume() {
        super.onResume()
        binding.refreshGame.isClickable = true
        binding.clearCell.isClickable = true
    }

    override fun onPause() {
        super.onPause()
        val timeWhenStopped = binding.chTimer.base - SystemClock.elapsedRealtime()
        viewModel.saveBoard(timeWhenStopped)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.game_toolbar_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.dictionary_icon -> {
                        parentFragmentManager.popBackStack()
                        replaceWithThisFragment(DictionaryFragment::class.java)
                        true
                    }
                    R.id.game_help_icon -> {
                        replaceWithThisFragment(HelpFragment::class.java)
                        true
                    }
                    else -> true
                }
            }
        }, viewLifecycleOwner)
    }

    private fun interactWithViewModel() {
        viewModel = (activity as MainActivity).gameViewModel
        val buttons = listOf(
            binding.oneButton,
            binding.twoButton,
            binding.threeButton,
            binding.fourButton,
            binding.fiveButton,
            binding.sixButton,
            binding.sevenButton,
            binding.eightButton,
            binding.nineButton
        )
        setListeners(buttons)
        collectFlows(buttons)
        updateViewModelTimer()
    }

    private fun setListeners(buttons: List<Button>) {
        buttons.forEachIndexed { index, button ->
            button.setOnClickListener {
                viewModel.handleInput(index)
                AnimatorSet().apply {
                    play(shakeAnimator(it, -10f, 0f, 40, 2))
                    start()
                }
            }
        }
        binding.refreshGame.setOnClickListener {
            AnimatorSet().apply {
                play(shakeAnimator(it, -360f, 0f, 250, 0))
                start()
            }
            addThisFragment(ConfirmRefreshFragment::class.java)
            viewModel.setSettingsState(false)
        }
        binding.clearCell.setOnClickListener {
            AnimatorSet().apply {
                play(shakeAnimator(it, -10f, 0f, 40, 2))
                start()
            }

            viewModel.clearSelected()
        }
    }

    private fun collectFlows(buttons: List<Button>) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.boardSharedFlow.collectLatest {
                        updateCells(it.cells)
                    }
                }
                launch {
                    viewModel.selectedCellFlow.collectLatest {
                        updateSelectedCellUI(it)
                    }
                }
                launch {
                    viewModel.nineCharSharedFlow.collectLatest {
                        buttons.forEachIndexed { index, button ->
                            button.text = it[index]
                        }
                    }
                }
                launch {
                    viewModel.settingsFinishedStateFlow.collectLatest { areSettingsDone ->
                        binding.refreshGame.isClickable = areSettingsDone
                    }
                }
                launch {
                    viewModel.timeSpentFlow.collectLatest { time ->
                        if (time!=-1L) {
                            continueTimer(time)
                        } else {
                            binding.chTimer.stop()
                        }
                    }
                }
            }
        }
    }

    private fun updateSelectedCellUI(cell: Pair<Int, Int>?) = cell?.let {
        binding.sudokuBoard.updateSelectedCellUI(cell.first, cell.second)
    }

    private fun updateCells(cells: List<Cell>?) = cells?.let {
        binding.sudokuBoard.updateCells(cells)
    }

    private fun shakeAnimator(
        shake: View,
        startValue: Float,
        endValue: Float,
        dur: Long,
        repeat: Int
    ) =
        ObjectAnimator.ofFloat(shake, "rotation", startValue, endValue).apply {
            repeatMode = ValueAnimator.RESTART
            repeatCount = repeat
            duration = dur
            interpolator = linearInterpolator
        }

    override fun onCellTouched(row: Int, col: Int) {
        viewModel.updateSelection(row, col)
    }

    override fun onCellLongTouched(row: Int, col: Int) {
        viewModel.updateSelection(row, col)
        viewModel.markSelectedAsDoubtful()
    }

    private fun replaceWithThisFragment(fragment: Class<out Fragment>) {
        parentFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .replace(R.id.fcvMain, fragment, null)
            .addToBackStack(null)
            .commit()
    }

    private fun addThisFragment(fragment: Class<out Fragment>) {
        parentFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .add(R.id.gameContainerView, fragment, null)
            .addToBackStack(null)
            .commit()
    }

    private fun continueTimer(time: Long) {
        binding.chTimer.base = SystemClock.elapsedRealtime() + time
        binding.chTimer.start()

    }

    private fun updateViewModelTimer() {
        binding.chTimer.setOnChronometerTickListener {
            val timeWhenStopped = it.base - SystemClock.elapsedRealtime()
            viewModel.updateTimer(timeWhenStopped)
        }
    }
}
