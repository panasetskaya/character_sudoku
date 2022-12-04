package com.panasetskaia.charactersudoku.presentation.fragments

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Application
import android.os.Bundle
import android.os.CountDownTimer
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
import com.panasetskaia.charactersudoku.domain.entities.Board
import com.panasetskaia.charactersudoku.domain.entities.Cell
import com.panasetskaia.charactersudoku.presentation.MainActivity
import com.panasetskaia.charactersudoku.presentation.customViews.SudokuBoardView
import com.panasetskaia.charactersudoku.presentation.fragments.dialogFragments.ConfirmRefreshFragment
import com.panasetskaia.charactersudoku.presentation.viewmodels.*
import com.panasetskaia.charactersudoku.utils.formatToTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.lang.Math.abs
import java.util.concurrent.TimeUnit

class GameFragment : Fragment(), SudokuBoardView.OnTouchListener {

    private val mInterpolator = AccelerateInterpolator()

    private lateinit var gameViewModel: GameViewModel
    private lateinit var characterViewModel: ChineseCharacterViewModel

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
        setAnimations()
        interactWithViewModel()

    }

    override fun onPause() {
        val timeWhenStopped = binding.chTimer.base - SystemClock.elapsedRealtime()
        gameViewModel.saveBoard(timeWhenStopped)
        super.onPause()
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
                        val timeWhenStopped = binding.chTimer.base - SystemClock.elapsedRealtime()
                        gameViewModel.saveBoard(timeWhenStopped)
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
                        val timeWhenStopped = binding.chTimer.base - SystemClock.elapsedRealtime()
                        gameViewModel.saveBoard(timeWhenStopped)
                        replaceWithThisFragment(HelpFragment::class.java, null)
                        true
                    }
                    R.id.records_icon -> {
                        val timeWhenStopped = binding.chTimer.base - SystemClock.elapsedRealtime()
                        gameViewModel.saveBoard(timeWhenStopped)
                        parentFragmentManager.popBackStack()
                        replaceWithThisFragment(RecordsFragment::class.java, null)
                        true
                    }
                    else -> {
                        true
                    }
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
    }

    private fun setListeners(buttons: List<Button>) {
        buttons.forEachIndexed { index, button ->
            button.setOnClickListener {
                val currentTime =
                    binding.chTimer.base - SystemClock.elapsedRealtime()
                gameViewModel.handleInput(index, currentTime)
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
            val currentTime = binding.chTimer.base - SystemClock.elapsedRealtime()
            gameViewModel.clearSelected(currentTime)
        }
    }

    private fun collectFlows(buttons: List<Button>) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                gameViewModel.gameStateFlow.collectLatest {
                    when (it) {
                        is REFRESHING -> {
                            refresh()
                        }
                        is PLAYING -> {
                            play(it.currentBoard, buttons)
                            launch {
                                gameViewModel.selectedCellFlow.collectLatest { selectedCell ->
                                    updateSelectedCellUI(selectedCell)
                                }
                            }
                            launch {
                                gameViewModel.finalErrorFlow.collectLatest { error ->
                                    if (error) {
                                        Toast.makeText(
                                            requireContext(),
                                            getString(R.string.check_again),
                                            Toast.LENGTH_SHORT
                                        )
                                            .show()
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
        val currentTime = binding.chTimer.base - SystemClock.elapsedRealtime()
        gameViewModel.markSelectedAsDoubtful(currentTime)
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

    private fun initiateNewGame() {
        addThisFragment(ConfirmRefreshFragment::class.java)
        gameViewModel.setSettingsState()
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
            updateSelectedCellUI(Pair(-1,-1))
            winAnimationView.playAnimation()
        }
    }

    private fun play(board: Board, buttons: List<Button>) {
        with (binding) {
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
        val timeWhenStopped = binding.chTimer.base - SystemClock.elapsedRealtime()
        gameViewModel.saveBoard(timeWhenStopped)
        with (binding) {
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
        with (binding) {
            buttonsGroup.visibility = View.GONE
            rippleAnimationView.visibility = View.GONE
            winAnimationView.visibility = View.GONE
            sudokuBoard.visibility = View.VISIBLE
            chTimer.visibility = View.VISIBLE
            tvGameFinished.visibility = View.GONE
            newGameButton.visibility = View.VISIBLE
            updateCells(board.cells)
            updateSelectedCellUI(Pair(-1,-1))
        }
    }

    private fun launchRefreshed() {
        gameViewModel.launchRefreshedGame()
    }

    private fun launchOldBoard() {
        gameViewModel.launchOldBoard()
    }


}




