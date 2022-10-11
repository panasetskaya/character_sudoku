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
import kotlinx.coroutines.*

class CharacterSudokuRepositoryImpl : CharacterSudokuRepository {

    private val charactersDao =
        SudokuDatabase.getInstance(SudokuApplication.instance).chineseCharacterDao()
    private val boardDao =
        SudokuDatabase.getInstance(SudokuApplication.instance).boardDao()

    private val mapper = SudokuMapper()

    private var temporaryDict = INITIAL_9_CHAR

    override suspend fun getNineRandomCharFromDict(): List<String> {
        temporaryDict = INITIAL_9_CHAR
        return withContext(Dispatchers.Default) {
            val idList = charactersDao.getAllChinese()?.shuffled()
            val listOfStringCharacters = mutableListOf<String>()
            if (idList!=null && idList.size>=9) {
                for (i in 0 until 9) {
                    val randomChinese = idList[i]
                    listOfStringCharacters.add(randomChinese)
                }
                temporaryDict = listOfStringCharacters
            }
            temporaryDict
        }
    }

    override suspend fun addOrEditCharToDict(character: ChineseCharacter) {
        val dbModel = mapper.mapDomainChineseCharacterToDbModel(character)
        charactersDao.addOrEditCharacter(dbModel)
    }

    override suspend fun deleteCharFromDict(characterId: Int) {
        charactersDao.deleteCharFromDict(characterId)
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

    override suspend fun getNewGame(nineCharacters: List<ChineseCharacter>): Board {
        val listString = mutableListOf<String>()
        for (i in nineCharacters) {
            listString.add(i.character)
        }
        temporaryDict = listString
        return withContext(Dispatchers.Default) {
            val grid = generateNumberGrid().values.toList()[0]
            val board = mapStringGridToBoard(grid)
            translateNumbersToCharacters(board)
        }
    }

    override suspend fun saveGame(board: Board) {
        boardDao.deleteEverything()
        val boardDbModel = mapper.mapDomainBoardToDbModel(board)
        boardDao.saveGame(boardDbModel)
    }

    override suspend fun getSavedGame(): Board? {
        val boardDbModel = boardDao.getSavedGame()
        return boardDbModel?.let {
            val nineChars = mutableListOf<String>()
            for (i in it.nineChars) {
                nineChars.add(i)
            }
            temporaryDict = nineChars
            mapper.mapBoardDbModelToDomainEntity(it)
        }
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
        return withContext(Dispatchers.Default) {
            val grid = generateNumberGrid().values.toList()[0]
            val board = mapStringGridToBoard(grid)
            translateNumbersToCharacters(board)
        }
    }

    private suspend fun generateNumberGrid(): Map<String, String> {
        return SudokuGame().fillGrid()
    }

    private fun mapStringGridToBoard(stringGrid: String): Board {
        val cells = List(SudokuGame.GRID_SIZE * SudokuGame.GRID_SIZE) { i ->
            Cell(
                i / SudokuGame.GRID_SIZE,
                i % SudokuGame.GRID_SIZE,
                stringGrid[i].toString()
            )
        }
        val board = Board(cells = cells, nineChars = temporaryDict)
        return board
    }

    private fun translateNumbersToCharacters(board: Board): Board {
        for (i in board.cells) {
            if (i.value != EMPTY_CELL) {
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
            if (i.value != EMPTY_CELL) {
                number = temporaryDict.indexOf(i.value) + 1  // поменять на нужный словарь
            }
            gridString += number.toString()
        }
        return gridString
    }

    companion object {
        private val INITIAL_9_CHAR = listOf("一", "二", "三", "四", "五", "六", "七", "八", "九")
        private const val EMPTY_CELL = "0"
    }
}
