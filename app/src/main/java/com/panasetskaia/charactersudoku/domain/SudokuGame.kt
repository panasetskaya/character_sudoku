package com.panasetskaia.charactersudoku.domain

import android.util.Log
import com.panasetskaia.charactersudoku.domain.SudokuSolver.GRID_SIZE
import com.panasetskaia.charactersudoku.domain.SudokuSolver.GRID_SIZE_SQUARE_ROOT
import com.panasetskaia.charactersudoku.domain.SudokuSolver.MAX_DIGIT_INDEX
import com.panasetskaia.charactersudoku.domain.SudokuSolver.MAX_DIGIT_VALUE
import com.panasetskaia.charactersudoku.domain.SudokuSolver.MIN_DIGIT_INDEX
import com.panasetskaia.charactersudoku.domain.SudokuSolver.MIN_DIGIT_VALUE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.random.Random

class SudokuGame {

    private var grid = Array(GRID_SIZE) { IntArray(GRID_SIZE) {0} }
    private var printableGridRemoved: MutableList<Int> = mutableListOf()
    private var printableGridFull: MutableList<Int> = mutableListOf()
    private lateinit var level: Level

    suspend fun fillGrid(levelNew: Level = Level.JUNIOR): Map<MutableList<Int>,MutableList<Int>> {
        return withContext(Dispatchers.Default){
            level = levelNew
            fillDiagonalBoxes()
            fillRemaining(0, GRID_SIZE_SQUARE_ROOT)
            makePrintableGrid(printableGridFull)
            removeDigits()
            makePrintableGrid(printableGridRemoved)
            mapOf(printableGridFull to printableGridRemoved)
        }
    }

    private fun makePrintableGrid(printableGrid: MutableList<Int>) {
        for (i in 0 until GRID_SIZE) {
            for (j in 0 until GRID_SIZE) {
                printableGrid.add(grid[i][j])
            }
        }
    }
    private fun fillDiagonalBoxes() {
        for (i in 0 until GRID_SIZE step GRID_SIZE_SQUARE_ROOT) {
            fillBox(i, i)
        }
    }

    private fun fillBox(row: Int, column: Int) {
        var generatedDigit: Int

        for (i in 0 until GRID_SIZE_SQUARE_ROOT) {
            for (j in 0 until GRID_SIZE_SQUARE_ROOT) {
                do {
                    generatedDigit = generateRandomInt(MIN_DIGIT_VALUE, MAX_DIGIT_VALUE)
                } while (!isUnusedInBox(row, column, generatedDigit))

                grid[row + i][column + j] = generatedDigit
            }
        }
    }

    private fun generateRandomInt(min: Int, max: Int) = Random.nextInt(min, max + 1)

    private fun isUnusedInBox(rowStart: Int, columnStart: Int, digit: Int) : Boolean {
        for (i in 0 until GRID_SIZE_SQUARE_ROOT) {
            for (j in 0 until GRID_SIZE_SQUARE_ROOT) {
                if (grid[rowStart + i][columnStart + j] == digit) {
                    return false
                }
            }
        }
        return true
    }

    private fun fillRemaining(i: Int, j: Int) : Boolean {
        var i = i
        var j = j

        if (j >= GRID_SIZE && i < GRID_SIZE - 1) {
            i += 1
            j = 0
        }
        if (i >= GRID_SIZE && j >= GRID_SIZE) {
            return true
        }
        if (i < GRID_SIZE_SQUARE_ROOT) {
            if (j < GRID_SIZE_SQUARE_ROOT) {
                j = GRID_SIZE_SQUARE_ROOT
            }
        } else if (i < GRID_SIZE - GRID_SIZE_SQUARE_ROOT) {
            if (j == (i / GRID_SIZE_SQUARE_ROOT) * GRID_SIZE_SQUARE_ROOT) {
                j += GRID_SIZE_SQUARE_ROOT
            }
        } else {
            if (j == GRID_SIZE - GRID_SIZE_SQUARE_ROOT) {
                i += 1
                j = 0
                if (i >= GRID_SIZE) {
                    return true
                }
            }
        }

        for (digit in 1..MAX_DIGIT_VALUE) {
            if (isSafeToPutIn(i, j, digit)) {
                grid[i][j] = digit
                if (fillRemaining(i, j + 1)) {
                    return true
                }
                grid[i][j] = 0
            }
        }
        return false
    }

    private fun isSafeToPutIn(row: Int, column: Int, digit: Int) =
        isUnusedInBox(findBoxStart(row), findBoxStart(column), digit)
                && isUnusedInRow(row, digit)
                && isUnusedInColumn(column, digit)

    private fun findBoxStart(index: Int) = index - index % GRID_SIZE_SQUARE_ROOT

    private fun isUnusedInRow(row: Int, digit: Int) : Boolean {
        for (i in 0 until GRID_SIZE) {
            if (grid[row][i] == digit) {
                return false
            }
        }
        return true
    }

    private fun isUnusedInColumn(column: Int, digit: Int) : Boolean {
        for (i in 0 until GRID_SIZE) {
            if (grid[i][column] == digit) {
                return false
            }
        }
        return true
    }

    private fun removeDigits() {
        var digitsToRemove = GRID_SIZE * GRID_SIZE - level.numberOfProvidedDigits

        while (digitsToRemove > 0) {
            val randomRow = generateRandomInt(MIN_DIGIT_INDEX, MAX_DIGIT_INDEX)
            val randomColumn = generateRandomInt(MIN_DIGIT_INDEX, MAX_DIGIT_INDEX)

            if (grid[randomRow][randomColumn] != 0) {
                val digitToRemove = grid[randomRow][randomColumn]
                grid[randomRow][randomColumn] = 0
                if (!SudokuSolver.solvable(grid)) {
                    grid[randomRow][randomColumn] = digitToRemove
                } else {
                    digitsToRemove --
                }
            }
        }
    }
}