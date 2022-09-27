package com.panasetskaia.charactersudoku.presentation

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.CycleInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.panasetskaia.charactersudoku.R
import com.panasetskaia.charactersudoku.databinding.ActivityMainBinding
import com.panasetskaia.charactersudoku.domain.entities.Cell
import com.panasetskaia.charactersudoku.presentation.customViews.SudokuBoardView

class MainActivity : AppCompatActivity(), SudokuBoardView.OnTouchListener {

    private val viewModel by lazy {
        ViewModelProvider(this)[GameViewModel::class.java]
    }
    private lateinit var binding: ActivityMainBinding

    private val linearInterpolator = LinearInterpolator()

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


        for (button in buttons) {
            button.text = viewModel.nineCharacters[buttons.indexOf(button)]
        }
        buttons.forEachIndexed { index, button ->
            button.setOnClickListener {
                viewModel.handleInput(index)
                AnimatorSet().apply {
                    play(shakeAnimator(it, "rotation"))
                    start()
                }
            }
        }
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
        ObjectAnimator.ofFloat(shake, propertyName, -5f, 0f).apply {
            repeatMode = ValueAnimator.RESTART
            repeatCount = 1
            duration = 40
            interpolator = linearInterpolator
        }
}