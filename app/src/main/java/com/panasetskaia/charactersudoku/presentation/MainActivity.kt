package com.panasetskaia.charactersudoku.presentation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.panasetskaia.charactersudoku.databinding.ActivityMainBinding
import com.panasetskaia.charactersudoku.domain.entities.Cell
import com.panasetskaia.charactersudoku.presentation.customViews.SudokuBoardView

class MainActivity : AppCompatActivity(), SudokuBoardView.OnTouchListener {

    private val viewModel by lazy {
        ViewModelProvider(this)[MainViewModel::class.java]
    }
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.sudokuBoard.registerListener(this)
        viewModel.selectedCellLiveData.observe(this) {
            updateSelectedCellUI(it)
        }
        viewModel.boardLiveData.observe(this) {
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
        viewModel.nineCharactersLiveData.observe(this) {
            for (button in buttons) {
                button.text = it[buttons.indexOf(button)]
            }
        }
        buttons.forEachIndexed { index, button ->
            button.setOnClickListener { viewModel.handleInput(index) }
        }
    }

    override fun onCellTouched(row: Int, col: Int) {
        viewModel.updateSelection(row, col)
    }

    private fun updateSelectedCellUI(cell: Pair<Int, Int>?) = cell?.let {
        binding.sudokuBoard.updateSelectedCellUI(cell.first, cell.second)
    }

    private fun updateCells(cells: List<Cell>?) = cells?.let {
        binding.sudokuBoard.updateCells(cells)
    }

}