package com.panasetskaia.charactersudoku.presentation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.panasetskaia.charactersudoku.R

class MainActivity : AppCompatActivity(), SudokuBoardView.OnTouchListener {

    private val viewModel by lazy {
        ViewModelProvider(this)[MainViewModel::class.java]
    }
    private lateinit var sudokuBoardView: SudokuBoardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewModel.getGame()
        sudokuBoardView = findViewById(R.id.sudoku_board)
        sudokuBoardView.registerListener(this)
        viewModel.selectedCellLiveData.observe(this) {
            updateSelectedCellUI(it)
        }
    }

    override fun onCellTouched(row: Int, col: Int) {
        viewModel.updateSelectedCell(row, col)
    }

    private fun updateSelectedCellUI(cell: Pair<Int, Int>?) = cell?.let {
        sudokuBoardView.updateSelectedCellUI(cell.first, cell.second)
    }
}