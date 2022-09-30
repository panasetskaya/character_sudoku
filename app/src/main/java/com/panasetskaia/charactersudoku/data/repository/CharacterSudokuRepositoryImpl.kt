package com.panasetskaia.charactersudoku.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.panasetskaia.charactersudoku.application.SudokuApplication
import com.panasetskaia.charactersudoku.data.database.SudokuDatabase
import com.panasetskaia.charactersudoku.data.gameGenerator.SudokuGame
import com.panasetskaia.charactersudoku.domain.CharacterSudokuRepository
import com.panasetskaia.charactersudoku.domain.FAILED
import com.panasetskaia.charactersudoku.domain.GameResult
import com.panasetskaia.charactersudoku.domain.SUCCESS
import com.panasetskaia.charactersudoku.domain.entities.Board
import com.panasetskaia.charactersudoku.domain.entities.Cell
import com.panasetskaia.charactersudoku.domain.entities.ChineseCharacter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CharacterSudokuRepositoryImpl : CharacterSudokuRepository {

    private val charactersDao =
        SudokuDatabase.getInstance(SudokuApplication.instance).chineseCharacterDao()

    private val mapper = SudokuMapper()

    private var temporaryDict = listOf("留", "融", "砌", "铝", "洞", "乳", "廖", "部", "伞")


    override suspend fun getNineRandomCharFromDict(): List<String> {
        val nineRandom = charactersDao.getNineRandomCharacters()
        val listOfStringCharacters = mutableListOf<String>()
        for (i in nineRandom) {
            listOfStringCharacters.add(i.character)
        }
        temporaryDict = listOfStringCharacters
        return temporaryDict
    }

    override suspend fun addOrEditCharToDict(character: ChineseCharacter) {
        val dbModel = mapper.mapDomainChineseCharacterToDbModel(character)
        charactersDao.addOrEditCharacter(dbModel)
    }

    override suspend fun deleteCharFromDict(character: ChineseCharacter) {
        val dbModel = mapper.mapDomainChineseCharacterToDbModel(character)
        charactersDao.deleteCharFromDict(dbModel.id)
    }

    override fun searchForCharacter(character: String): LiveData<List<ChineseCharacter>> {
        return Transformations.map(
            charactersDao.searchForCharacter(character)
        ) { dbModelList ->
            val entityList = mutableListOf<ChineseCharacter>()
            for (i in dbModelList) {
                val entity = mapper.mapDbChineseCharacterToDomainEntity(i)
                entityList.add(entity)
            }
            entityList
        }
    }

    override fun getWholeDictionary(): LiveData<List<ChineseCharacter>> {
        return Transformations.map(
            charactersDao.getWholeDictionary()
        ) { dbModelList ->
            val entityList = mutableListOf<ChineseCharacter>()
            for (i in dbModelList) {
                val entity = mapper.mapDbChineseCharacterToDomainEntity(i)
                entityList.add(entity)
            }
            entityList
        }
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

    override suspend fun getGameResult(board: Board): GameResult {
        val stringGrid = translateCharactersToNumbers(board)
        val solution = SudokuGame().getSolution(stringGrid)
        if (solution != null) {
            val solutionBoard = mapStringGridToBoard(solution)
            return SUCCESS(solutionBoard)
        } else return FAILED
    }


    // Just to test the game itself
    suspend fun getNewGameTestFun(): Board {
        val grid = generateNumberGrid().values.toList()[0]
        val board = mapStringGridToBoard(grid)
        return translateNumbersToCharacters(board)
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

    private fun translateNumbersToCharacters(board: Board): Board {
        for (i in board.cells) {
            if (i.value != "0") {
                i.isFixed = true
                val index = i.value.toInt() - 1
                i.value = temporaryDict[index] // поменять на нужный словарь
            }
        }
        return board
    }

    private fun translateCharactersToNumbers(board: Board): String {
        var gridString = ""
        for (i in board.cells) {
            var number = 0
            if (i.value != "0") {
                number = temporaryDict.indexOf(i.value) + 1  // поменять на нужный словарь
            }
            gridString += number.toString()
        }

        return gridString
    }
}
