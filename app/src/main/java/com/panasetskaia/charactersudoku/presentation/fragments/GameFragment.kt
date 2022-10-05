package com.panasetskaia.charactersudoku.presentation.fragments

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.*
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import com.panasetskaia.charactersudoku.R
import com.panasetskaia.charactersudoku.databinding.FragmentGameBinding
import com.panasetskaia.charactersudoku.domain.entities.Cell
import com.panasetskaia.charactersudoku.presentation.MainActivity
import com.panasetskaia.charactersudoku.presentation.customViews.SudokuBoardView
import com.panasetskaia.charactersudoku.presentation.fragments.dialogFragments.ConfirmRefreshFragment
import com.panasetskaia.charactersudoku.presentation.viewmodels.GameViewModel

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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMenu()
        viewModel = (activity as MainActivity).gameViewModel
        binding.sudokuBoard.registerListener(this)
        viewModel.selectedCellLiveData.observe(viewLifecycleOwner) {
            updateSelectedCellUI(it)
        }
        viewModel.boardLiveData.observe(viewLifecycleOwner) {
            updateCells(it.cells)
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
                        play(shakeAnimator(it, "rotation"))
                        start()
                    }
                }
            }
        }

        binding.refreshGame.setOnClickListener {
            val fragment = ConfirmRefreshFragment.newInstance()
            parentFragmentManager.beginTransaction()
                .add(R.id.fcvMain,fragment)
                .addToBackStack(null)
                .commit()
        }
        binding.clearCell.setOnClickListener {
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
                            .replace(R.id.fcvMain,fragment)
                            .addToBackStack(null)
                            .commit()
                        true
                    }
                    R.id.game_help_icon -> {
                        Toast.makeText(context, "Will go to Help", Toast.LENGTH_LONG).show()
                        true
                    }
                    else -> true
                }
            }
        }, viewLifecycleOwner)
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


    private fun shakeAnimator(shake: View, propertyName: String) =
        ObjectAnimator.ofFloat(shake, propertyName, -10f, 0f).apply {
            repeatMode = ValueAnimator.RESTART
            repeatCount = 2
            duration = 40
            interpolator = linearInterpolator
        }

    companion object {
        fun newInstance() = GameFragment()
    }
}

//todo: навигацию смотри! popBackStack
