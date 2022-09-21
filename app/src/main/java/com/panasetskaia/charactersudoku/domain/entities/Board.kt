package com.panasetskaia.charactersudoku.domain.entities

import com.panasetskaia.charactersudoku.data.gameGenerator.SudokuGame

class Board(val size: Int = SudokuGame.GRID_SIZE,
            val cells: List<Cell>) {

    fun getCell(row: Int, col: Int) = cells[row * size + col]

}