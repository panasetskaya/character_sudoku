package com.panasetskaia.charactersudoku.data.repository

import androidx.lifecycle.MutableLiveData
import com.panasetskaia.charactersudoku.data.gameGenerator.SudokuGame
import com.panasetskaia.charactersudoku.domain.CharacterSudokuRepository
import com.panasetskaia.charactersudoku.domain.entities.Board
import com.panasetskaia.charactersudoku.domain.entities.Cell
import com.panasetskaia.charactersudoku.domain.entities.ChineseCharacter
import kotlinx.coroutines.*

class CharacterSudokuRepositoryImpl : CharacterSudokuRepository {

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun getNineRandomCharFromDict(): List<ChineseCharacter> {
        TODO("Not yet implemented")
    }

    override fun addCharToDict(character: ChineseCharacter) {
        TODO("Not yet implemented")
    }

    override fun deleteCharFromDict(character: ChineseCharacter) {
        TODO("Not yet implemented")
    }

    override fun editCharinDict(character: ChineseCharacter) {
        TODO("Not yet implemented")
    }

    override fun searchForCharacter(character: String): ChineseCharacter {
        TODO("Not yet implemented")
    }

    override fun getWholeDictionary(): List<ChineseCharacter> {
        TODO("Not yet implemented")
    }

    override fun getNewGame(nineCharacters: List<ChineseCharacter>): Board {
        TODO("Not yet implemented")
    }

    override fun saveGame(board: Board) {
        TODO("Not yet implemented")
    }

    override fun getSavedGame(): Board {
        TODO("Not yet implemented")
    }

    /**
     * Just to test the game itself
     */
    suspend fun getNewNumberGameTestFun(): Board {
        val grid = generateNumberGrid().values.toList()[0]
        val cells = List(SudokuGame.GRID_SIZE * SudokuGame.GRID_SIZE) { i ->
            Cell(
                i / SudokuGame.GRID_SIZE,
                i % SudokuGame.GRID_SIZE,
                grid[i].toString()
            )
        }
        cells[11].isStartingCell = true
        cells[21].isStartingCell = true
        val board = Board(SudokuGame.GRID_SIZE, cells)
        return board
    }

    fun cancelScope() {
        scope.cancel()
    }

    private suspend fun generateNumberGrid(): Map<String, String> {
        return withContext(Dispatchers.Default) {
            SudokuGame().fillGrid()
        }
    }
}
