package com.panasetskaia.charactersudoku.data.repository

import com.panasetskaia.charactersudoku.data.database.BoardDao
import com.panasetskaia.charactersudoku.data.database.ChineseCharacterDao
import com.panasetskaia.charactersudoku.data.gameGenerator.SudokuGame
import com.panasetskaia.charactersudoku.domain.CharacterSudokuRepository
import com.panasetskaia.charactersudoku.domain.FAILED
import com.panasetskaia.charactersudoku.domain.GameResult
import com.panasetskaia.charactersudoku.domain.SUCCESS
import com.panasetskaia.charactersudoku.domain.entities.Board
import com.panasetskaia.charactersudoku.domain.entities.Cell
import com.panasetskaia.charactersudoku.domain.entities.ChineseCharacter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CharacterSudokuRepositoryImpl @Inject constructor(
    private val mapper: SudokuMapper,
    private val charactersDao: ChineseCharacterDao,
    private val boardDao: BoardDao
) : CharacterSudokuRepository {

    private var temporaryDict = INITIAL_9_CHAR

    override suspend fun getRandomBoard(): Board {
        temporaryDict = INITIAL_9_CHAR
        val wholeList = charactersDao.getAllChineseAsList().shuffled()
        return if (wholeList.size >= 9) {
            val randomCharacters = mutableListOf<String>()
            for (i in 0 until 9) {
                val randomChinese = wholeList[i]
                randomCharacters.add(randomChinese)
            }
            getNewGameWithStrings(randomCharacters)
        } else {
            val grid = generateNumberGrid().values.toList()[0]
            val board = mapStringGridToBoard(grid)
            translateNumbersToCharacters(board)
        }
    }

    override suspend fun addOrEditCharToDict(character: ChineseCharacter) {
        val dbModel = mapper.mapDomainChineseCharacterToDbModel(character)
        charactersDao.addOrEditCharacter(dbModel)
    }

    override suspend fun deleteCharFromDict(characterId: Int) {
        charactersDao.deleteCharFromDict(characterId)
    }


    override fun getWholeDictionary(): Flow<List<ChineseCharacter>> {
        return charactersDao.getWholeDictionary().map { dbModelList ->
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

    private suspend fun getNewGameWithStrings(nineCharacters: List<String>): Board {
        temporaryDict = nineCharacters
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
            val numberBoard = mapStringGridToBoard(solution)
            val solutionBoard = translateNumbersToCharacters(numberBoard)
            return SUCCESS(solutionBoard)
        } else return FAILED
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
