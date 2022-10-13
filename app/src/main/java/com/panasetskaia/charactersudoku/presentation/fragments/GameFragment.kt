package com.panasetskaia.charactersudoku.presentation.fragments

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.animation.LinearInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.panasetskaia.charactersudoku.R
import com.panasetskaia.charactersudoku.application.SudokuApplication
import com.panasetskaia.charactersudoku.databinding.FragmentGameBinding
import com.panasetskaia.charactersudoku.domain.entities.Cell
import com.panasetskaia.charactersudoku.presentation.customViews.SudokuBoardView
import com.panasetskaia.charactersudoku.presentation.fragments.dialogFragments.ConfirmRefreshFragment
import com.panasetskaia.charactersudoku.presentation.viewmodels.GameViewModel
import com.panasetskaia.charactersudoku.presentation.viewmodels.ViewModelFactory
import javax.inject.Inject

class GameFragment : Fragment(), SudokuBoardView.OnTouchListener {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val component by lazy {
        (requireActivity().application as SudokuApplication).component
    }

    private val linearInterpolator = LinearInterpolator()

    private lateinit var viewModel: GameViewModel

    private var _binding: FragmentGameBinding? = null
    private val binding: FragmentGameBinding
        get() = _binding ?: throw RuntimeException("FragmentGameBinding is null")

    override fun onAttach(context: Context) {
        component.inject(this)
        super.onAttach(context)
    }

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

        viewModel = ViewModelProvider(this, viewModelFactory)[GameViewModel::class.java]
        binding.sudokuBoard.registerListener(this)
        viewModel.selectedCellLiveData.observe(viewLifecycleOwner) {
            updateSelectedCellUI(it)
        }
        viewModel.boardLiveData.observe(viewLifecycleOwner) {
            updateCells(it.cells)
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
            val fragment = ConfirmRefreshFragment.newInstance()
            parentFragmentManager.beginTransaction()
                .add(R.id.gameContainerView, fragment)
                .addToBackStack(null)
                .commit()
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
                        val fragment = DictionaryFragment.newInstance()
                        parentFragmentManager.popBackStack()
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fcvMain, fragment)
                            .addToBackStack(null)
                            .commit()
                        true
                    }
                    R.id.game_help_icon -> {
                        val fragment = HelpFragment.newInstance()
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fcvMain, fragment)
                            .addToBackStack(null)
                            .commit()
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

    companion object {
        fun newInstance() = GameFragment()
    }
}
