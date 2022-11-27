package com.panasetskaia.charactersudoku.presentation.fragments

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.SystemClock
import android.view.*
import android.view.animation.AccelerateInterpolator
import android.widget.Button
import android.widget.Toast
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
import com.panasetskaia.charactersudoku.presentation.viewmodels.ChineseCharacterViewModel
import com.panasetskaia.charactersudoku.presentation.viewmodels.GameViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class GameFragment : Fragment(), SudokuBoardView.OnTouchListener {

    private val mInterpolator = AccelerateInterpolator()
    private lateinit var gameViewModel: GameViewModel
    private lateinit var characterViewModel: ChineseCharacterViewModel
    private var wasThisGameAlreadyFinished = false
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
        gameViewModel.saveBoard(timeWhenStopped)

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
                        val arguments = Bundle().apply {
                            putString(
                                DictionaryFragment.FILTER_EXTRA,
                                DictionaryFragment.NO_FILTER
                            )
                        }
                        parentFragmentManager.popBackStack()
                        replaceWithThisFragment(DictionaryFragment::class.java, arguments)
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

    private fun interactWithViewModel() {
        gameViewModel = (activity as MainActivity).gameViewModel
        characterViewModel = (activity as MainActivity).characterViewModel
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
                gameViewModel.handleInput(index)
                AnimatorSet().apply {
                    play(shakeAnimator(it, -10f, 0f, 40))
                    start()
                }
            }
            button.setOnLongClickListener {
                viewLifecycleOwner.lifecycleScope.launch {
                    viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                        characterViewModel.getOneCharacterByChinese(button.text.toString())
                            .collectLatest {
                                if (it.pinyin.length > 0 || it.translation.length > 0) {
                                    Toast.makeText(
                                        activity,
                                        "${it.character} [ ${it.pinyin.trim()} ] ${it.translation}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(activity, it.character, Toast.LENGTH_SHORT)
                                        .show()
                                }

                            }
                    }
                }
                true
            }
        }
        binding.refreshGame.setOnClickListener {
            AnimatorSet().apply {
                play(shakeAnimator(it, -360f, 0f, 250))
                start()
            }
            initiateNewGame()
        }
        binding.newGameButton.setOnClickListener {
            initiateNewGame()
        }
        binding.clearCell.setOnClickListener {
            AnimatorSet().apply {
                play(shakeAnimator(it, -10f, 0f, 40))
                start()
            }

            gameViewModel.clearSelected()
        }
    }

    private fun collectFlows(buttons: List<Button>) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    gameViewModel.boardSharedFlow.collectLatest {
                        updateCells(it.cells)
                        wasThisGameAlreadyFinished = it.alreadyFinished
                    }
                }
                launch {
                    gameViewModel.selectedCellFlow.collectLatest {
                        updateSelectedCellUI(it)
                    }
                }
                launch {
                    gameViewModel.nineCharSharedFlow.collectLatest {
                        buttons.forEachIndexed { index, button ->
                            button.text = it[index]
                        }
                    }
                }
                launch {
                    gameViewModel.settingsFinishedStateFlow.collectLatest { areSettingsDone ->
                        binding.refreshGame.isClickable = areSettingsDone
                    }
                }
                launch {
                    gameViewModel.timeSpentFlow.collectLatest { time ->
                        if (time != -1L) {
                            continueTimer(time)
                        } else {
                            binding.chTimer.stop()
                        }
                    }
                }
                launch {
                    gameViewModel.isWinFlow.collectLatest {
                        if (!wasThisGameAlreadyFinished && it) {
                            binding.buttonsGroup.visibility = View.GONE
                            binding.gameFinishedGroup.visibility = View.VISIBLE
                            val timePassedMillis =
                                (SystemClock.elapsedRealtime() - binding.chTimer.base)
                            val timePassed = String.format(
                                getString(R.string.time_formatted),
                                TimeUnit.MILLISECONDS.toMinutes(timePassedMillis),
                                TimeUnit.MILLISECONDS.toSeconds(timePassedMillis) -
                                        TimeUnit.MINUTES.toSeconds(
                                            TimeUnit.MILLISECONDS.toMinutes(
                                                timePassedMillis
                                            )
                                        )
                            );
                            binding.tvGameFinished.text =
                                getString(R.string.game_finished, timePassed)
                            with(binding.winAnimationView) {
                                repeatCount = 2
                                playAnimation()
                            }
                        } else if (wasThisGameAlreadyFinished && it) {
                            binding.buttonsGroup.visibility = View.GONE
                            binding.tvGameFinished.visibility = View.GONE
                            binding.newGameButton.visibility = View.VISIBLE
                        } else {
                            binding.buttonsGroup.visibility = View.VISIBLE
                            binding.gameFinishedGroup.visibility = View.GONE
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
        dur: Long
    ) =
        ObjectAnimator.ofFloat(shake, "rotation", startValue, endValue).apply {
            duration = dur
            interpolator = mInterpolator
        }

    override fun onCellTouched(row: Int, col: Int) {
        gameViewModel.updateSelection(row, col)
    }

    override fun onCellLongTouched(row: Int, col: Int) {
        gameViewModel.updateSelection(row, col)
        gameViewModel.markSelectedAsDoubtful()
    }

    private fun replaceWithThisFragment(fragment: Class<out Fragment>, args: Bundle?) {
        parentFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .replace(R.id.fcvMain, fragment, args)
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
            gameViewModel.updateTimer(timeWhenStopped)
        }
    }

    private fun initiateNewGame() {
        addThisFragment(ConfirmRefreshFragment::class.java)
        gameViewModel.setSettingsState(false)
    }
}
