package com.panasetskaia.charactersudoku.presentation.fragments

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.animation.LinearInterpolator
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
import kotlinx.coroutines.flow.onEach
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
        setupMenu()
        binding.sudokuBoard.registerListener(this)
        setupViewModel()
    }

    private fun setupViewModel() {
        viewModel = (activity as MainActivity).gameViewModel
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
//                launch {
//                    // second flow here!!!!
//                }
//                launch {
//                    // second flow here!!!!
//                }
            }
        }
        viewModel.settingsFinishedLiveData.observe(viewLifecycleOwner) { areSettingsDone ->
            binding.refreshGame.isClickable = areSettingsDone
        }
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
        viewModel.nineCharactersLiveData.observe(viewLifecycleOwner) { nineCharacters ->
            buttons.forEachIndexed { index, button ->
                button.text = nineCharacters[buttons.indexOf(button)]
                button.setOnClickListener {
                    viewModel.handleInput(index)
                    AnimatorSet().apply {
                        play(shakeAnimator(it, -10f, 0f, 40, 2))
                        start()
                    }
                }
            }
        }
        binding.refreshGame.setOnClickListener {
            AnimatorSet().apply {
                play(shakeAnimator(it, -360f, 0f, 250, 0))
                start()
            }
            addThisFragment(ConfirmRefreshFragment::class.java,null)
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

    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.game_toolbar_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.dictionary_icon -> {
                        parentFragmentManager.popBackStack()
                        replaceWithThisFragment(DictionaryFragment::class.java,null)
                        true
                    }
                    R.id.game_help_icon -> {
                        replaceWithThisFragment(HelpFragment::class.java, null)
                        true
                    }
                    else -> true
                }
            }
        }, viewLifecycleOwner)
    }

    override fun onResume() {
        super.onResume()
        binding.refreshGame.isClickable = true
        binding.clearCell.isClickable = true
    }

    override fun onPause() {
        super.onPause()
        viewModel.saveBoard()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCellTouched(row: Int, col: Int) {
        viewModel.updateSelection(row, col)
    }

    override fun onCellLongTouched(row: Int, col: Int) {
        viewModel.updateSelection(row, col)
        viewModel.markSelectedAsDoubtful()
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

    private fun replaceWithThisFragment(fragment: Class<out Fragment>, args: Bundle?) {
        parentFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .replace(R.id.fcvMain, fragment, args)
            .addToBackStack(null)
            .commit()
    }

    private fun addThisFragment(fragment: Class<out Fragment>, args: Bundle?) {
        parentFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .add(R.id.gameContainerView, fragment, args)
            .addToBackStack(null)
            .commit()
    }

}
