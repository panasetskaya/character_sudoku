package com.panasetskaia.charactersudoku.domain.entities

import com.panasetskaia.charactersudoku.data.gameGenerator.SudokuGame

data class Board(
    var id: Int = UNDEFINED_ID,
    val size: Int = SudokuGame.GRID_SIZE,
    val cells: List<Cell>,
    val nineChars: List<String>,
    var timeSpent: Long = 0,
    var alreadyFinished: Boolean = false
) {

    fun getCell(row: Int, col: Int) = cells[row * size + col]

    companion object {
        const val UNDEFINED_ID = 0
    }
}