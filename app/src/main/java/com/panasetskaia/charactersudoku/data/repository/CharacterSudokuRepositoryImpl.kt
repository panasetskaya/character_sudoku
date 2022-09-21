package com.panasetskaia.charactersudoku.data.repository

import com.panasetskaia.charactersudoku.data.gameGenerator.SudokuGame
import com.panasetskaia.charactersudoku.domain.CharacterSudokuRepository
import com.panasetskaia.charactersudoku.domain.entities.Board
import com.panasetskaia.charactersudoku.domain.entities.Cell
import com.panasetskaia.charactersudoku.domain.entities.ChineseCharacter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.withContext

class CharacterSudokuRepositoryImpl : CharacterSudokuRepository {

    private val scope = CoroutineScope(Dispatchers.IO)

    private val temporaryDict = listOf("留","融","砌","铝","洞","乳","廖","部","伞")


    override fun getNineRandomCharFromDict(): List<String> {
        return temporaryDict
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

    override suspend fun getSolution(gridString: String): Board? {
        val solution = SudokuGame().getSolution(gridString)
        if (solution!=null) {
            return mapStringGridToBoard(solution)
        } else return solution
    }

    /**
     * Just to test the game itself
     */
    suspend fun getNewNumberGameTestFun(): Board {
        val grid = generateNumberGrid().values.toList()[0]
        return mapStringGridToBoard(grid)
    }

    fun cancelScope() {
        scope.cancel()
    }

    private suspend fun generateNumberGrid(): Map<String, String> {
        return withContext(Dispatchers.Default) {
            SudokuGame().fillGrid()
        }
    }

    private fun mapStringGridToBoard(stringGrid: String): Board {
        val cells = List(SudokuGame.GRID_SIZE * SudokuGame.GRID_SIZE) { i ->
            Cell(
                i / SudokuGame.GRID_SIZE,
                i % SudokuGame.GRID_SIZE,
                stringGrid[i].toString()
            )
        }
        val board = Board(cells = cells)
        return board
    }
}
