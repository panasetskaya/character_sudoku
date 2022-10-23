package com.panasetskaia.charactersudoku.data.repository

import com.panasetskaia.charactersudoku.data.database.BoardDao
import com.panasetskaia.charactersudoku.data.database.ChineseCharacterDao
import com.panasetskaia.charactersudoku.data.database.ChineseCharacterDbModel
import com.panasetskaia.charactersudoku.data.gameGenerator.SudokuGame
import com.panasetskaia.charactersudoku.domain.CharacterSudokuRepository
import com.panasetskaia.charactersudoku.domain.FAILED
import com.panasetskaia.charactersudoku.domain.GameResult
import com.panasetskaia.charactersudoku.domain.SUCCESS
import com.panasetskaia.charactersudoku.domain.entities.*
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
        if (wholeList.size >= 9) {
            val randomCharacters = mutableListOf<String>()
            for (i in 0 until 9) {
                val randomChinese = wholeList[i]
                randomCharacters.add(randomChinese)
            }
            return getNewGameWithStrings(randomCharacters)
        } else {
            val missingSize = 9 - wholeList.size
            val adding = INITIAL_9_CHAR.subList(0,missingSize)
            val randomCharacters = wholeList + adding
            return getNewGameWithStrings(randomCharacters)
        }
    }

    override suspend fun getRandomWithCategory(category: String): Board {
        temporaryDict = INITIAL_9_CHAR
        val listForCategory = charactersDao.getChineseByCategory(category).shuffled()
        if (listForCategory.size >= 9) {
            val randomCharacters = mutableListOf<String>()
            for (i in 0 until 9) {
                val randomChinese = listForCategory[i]
                randomCharacters.add(randomChinese)
            }
            return getNewGameWithStrings(randomCharacters)
        } else {
            val missingSize = 9 - listForCategory.size
            val adding = INITIAL_9_CHAR.subList(0,missingSize)
            val randomCharacters = listForCategory + adding
            return getNewGameWithStrings(randomCharacters)
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
        return if (solution != null) {
            val numberBoard = mapStringGridToBoard(solution)
            val solutionBoard = translateNumbersToCharacters(numberBoard)
            SUCCESS(solutionBoard)
        } else FAILED
    }

    override fun getAllCategories(): Flow<List<Category>> {
        return charactersDao.getAllCategories().map {
            val entityList = mutableListOf<Category>()
            for (i in it) {
                val entity = mapper.mapDbModelToDomainCategory(i)
                entityList.add(entity)
            }
            entityList
        }
    }

    override suspend fun deleteCategory(catName:String) {
        charactersDao.deleteCategory(catName)
        charactersDao.getWholeDictionary().map {
            for (i in it) {
                if (i.category==catName) {
                    val replaceChar = i.copy(category = NO_CAT)
                    charactersDao.addOrEditCharacter(replaceChar)
                }
            }
        }
    }

    override suspend fun addCategory(category: Category) {
        if (!charactersDao.categoryExists(category.categoryName)) {
            charactersDao.addOrEditCategory(mapper.mapDomainCategoryToDbModel(category))
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
        private const val NO_CAT = "no category"
    }
}
