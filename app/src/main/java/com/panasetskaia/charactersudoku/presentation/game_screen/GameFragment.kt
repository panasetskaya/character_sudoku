package com.panasetskaia.charactersudoku.presentation.game_screen

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.panasetskaia.charactersudoku.R
import com.panasetskaia.charactersudoku.databinding.FragmentGameBinding
import com.panasetskaia.charactersudoku.domain.entities.Board
import com.panasetskaia.charactersudoku.domain.entities.Cell
import com.panasetskaia.charactersudoku.presentation.base.BaseFragment
import com.panasetskaia.charactersudoku.presentation.dict_screen.ChineseCharacterViewModel
import com.panasetskaia.charactersudoku.presentation.viewmodels.ViewModelFactory
import com.panasetskaia.charactersudoku.utils.getAppComponent
import com.panasetskaia.charactersudoku.utils.toast
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

class GameFragment : BaseFragment<FragmentGameBinding, GameViewModel>(FragmentGameBinding::inflate), SudokuBoardView.OnTouchListener {

    private val mInterpolator = AccelerateInterpolator()

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    override val viewModel by viewModels<GameViewModel> { viewModelFactory }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        getAppComponent().inject(this)
    }

    override fun onReady(savedInstanceState: Bundle?) {
        binding.sudokuBoard.registerListener(this)
        setAnimations()
        setButtons()
    }

    override fun onPause() {
        val timeWhenStopped = binding.chTimer.base - SystemClock.elapsedRealtime()
        viewModel.saveBoard(timeWhenStopped)
        super.onPause()
    }

    private fun setButtons() {
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
    }

    private fun setListeners(buttons: List<Button>) {
        buttons.forEachIndexed { index, button ->
            button.setOnClickListener {
                val currentTime =
                    binding.chTimer.base - SystemClock.elapsedRealtime()
                viewModel.handleInput(index, currentTime)
                AnimatorSet().apply {
                    play(shakeAnimator(it, -10f, 0f, 40))
                    start()
                }
            }
            button.setOnLongClickListener {
                viewLifecycleOwner.lifecycleScope.launch {
                    viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                        viewModel.getOneCharacterByChinese(button.text.toString())
                            .collectLatest {
                                if (it.pinyin.length > 0 || it.translation.length > 0) {
                                    toast("${it.character} [ ${it.pinyin.trim()} ] ${it.translation}")
                                } else {
                                    toast(it.character)
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
            val currentTime = binding.chTimer.base - SystemClock.elapsedRealtime()
            viewModel.clearSelected(currentTime)
        }
    }

    private fun collectFlows(buttons: List<Button>) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.gameStateFlow.collectLatest {
                        when (it) {
                            is REFRESHING -> {
                                refresh()
                            }
                            is PLAYING -> {
                                play(it.currentBoard, buttons)
                                launch {
                                    viewModel.finalErrorFlow.collectLatest { error ->
                                        if (error) {
                                            toast(R.string.check_again)
                                        }
                                    }
                                }
                            }
                            is WIN -> {
                                celebrate()
                            }
                            is SETTING -> {
                                setSettings()
                            }
                            is DISPLAY -> {
                                displayOldBoard(it.oldBoard)
                            }
                        }
                    }
                }
                launch {
                    viewModel.selectedCellFlow.collectLatest { selectedCell ->
                        updateSelectedCellUI(selectedCell)
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
        viewModel.updateSelection(row, col)
    }

    override fun onCellLongTouched(row: Int, col: Int) {
        viewModel.updateSelection(row, col)
        val currentTime = binding.chTimer.base - SystemClock.elapsedRealtime()
        viewModel.markSelectedAsDoubtful(currentTime)
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

    private fun initiateNewGame() {
        addThisFragment(ConfirmRefreshFragment::class.java)
        viewModel.setSettingsState()
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private fun setAnimations() {
        binding.rippleAnimationView.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator) {
            }

            override fun onAnimationEnd(p0: Animator) {
                binding.rippleAnimationView.visibility = View.GONE
                launchRefreshed()
            }

            override fun onAnimationCancel(p0: Animator) {
            }

            override fun onAnimationRepeat(p0: Animator) {
            }
        })
        binding.winAnimationView.repeatCount = 1
        binding.winAnimationView.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator) {
            }

            override fun onAnimationEnd(p0: Animator) {
                binding.tvGameFinished.visibility = View.GONE
                binding.newGameButton.visibility = View.VISIBLE
                launchOldBoard()
            }

            override fun onAnimationCancel(p0: Animator) {
            }

            override fun onAnimationRepeat(p0: Animator) {
            }
        })
    }

    private fun refresh() {
        with(binding) {
            buttonsGroup.visibility = View.INVISIBLE
            rippleAnimationView.visibility = View.VISIBLE
            winAnimationView.visibility = View.GONE
            sudokuBoard.visibility = View.GONE
            chTimer.visibility = View.INVISIBLE
            tvGameFinished.visibility = View.GONE
            newGameButton.visibility = View.GONE
            rippleAnimationView.playAnimation()
        }
    }

    private fun celebrate() {
        with(binding) {
            buttonsGroup.visibility = View.GONE
            rippleAnimationView.visibility = View.GONE
            winAnimationView.visibility = View.VISIBLE
            sudokuBoard.visibility = View.VISIBLE
            chTimer.visibility = View.VISIBLE
            tvGameFinished.visibility = View.VISIBLE
            newGameButton.visibility = View.GONE
            chTimer.stop()
            updateSelectedCellUI(Pair(-1, -1))
            winAnimationView.playAnimation()
        }
    }

    private fun play(board: Board, buttons: List<Button>) {
        with(binding) {
            buttonsGroup.visibility = View.VISIBLE
            rippleAnimationView.visibility = View.GONE
            winAnimationView.visibility = View.GONE
            sudokuBoard.visibility = View.VISIBLE
            chTimer.visibility = View.VISIBLE
            tvGameFinished.visibility = View.GONE
            newGameButton.visibility = View.GONE
            continueTimer(board.timeSpent)
            refreshGame.isClickable = true
            clearCell.isClickable = true
            buttons.forEachIndexed { index, button ->
                button.text = board.nineChars[index]
            }
            updateCells(board.cells)
        }
    }

    private fun setSettings() {
        with(binding) {
            buttonsGroup.visibility = View.GONE
            rippleAnimationView.visibility = View.GONE
            winAnimationView.visibility = View.GONE
            sudokuBoard.visibility = View.VISIBLE
            chTimer.visibility = View.GONE
            tvGameFinished.visibility = View.GONE
            newGameButton.visibility = View.GONE

            refreshGame.isClickable = false
            clearCell.isClickable = false
        }
    }

    private fun displayOldBoard(board: Board) {
        with(binding) {
            buttonsGroup.visibility = View.GONE
            rippleAnimationView.visibility = View.GONE
            winAnimationView.visibility = View.GONE
            sudokuBoard.visibility = View.VISIBLE
            chTimer.visibility = View.VISIBLE
            tvGameFinished.visibility = View.GONE
            newGameButton.visibility = View.VISIBLE
            updateCells(board.cells)
            updateSelectedCellUI(Pair(-1, -1))
        }
    }

    private fun launchRefreshed() {
        viewModel.launchRefreshedGame()
    }

    private fun launchOldBoard() {
        viewModel.launchOldBoard()
    }


}




