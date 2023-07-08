package com.panasetskaia.charactersudoku.data.repository

import android.app.Application
import android.os.Environment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.panasetskaia.charactersudoku.data.database.board.BoardDao
import com.panasetskaia.charactersudoku.data.database.dictionary.CategoryDbModel
import com.panasetskaia.charactersudoku.data.database.dictionary.ChineseCharacterDao
import com.panasetskaia.charactersudoku.data.database.records.RecordsDao
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
import java.io.File
import javax.inject.Inject


class CharacterSudokuRepositoryImpl @Inject constructor(
    private val application: Application,
    private val mapper: SudokuMapper,
    private val charactersDao: ChineseCharacterDao,
    private val boardDao: BoardDao,
    private val recordsDao: RecordsDao
) : CharacterSudokuRepository {

    private var temporaryDict = INITIAL_9_CHAR

    override suspend fun getRandomBoard(diffLevel: Level): Board {
        temporaryDict = INITIAL_9_CHAR
        val wholeList = charactersDao.getAllChineseAsList().toSet().shuffled()
        if (wholeList.size >= 9) {
            val randomCharacters = mutableListOf<String>()
            for (i in 0 until 9) {
                val randomChinese = wholeList[i]
                randomCharacters.add(randomChinese)
            }
            return getNewGameWithStrings(randomCharacters, diffLevel)
        } else {
            val missingSize = 9 - wholeList.size
            val adding = INITIAL_9_CHAR.subList(0, missingSize)
            val randomCharacters = wholeList + adding
            return getNewGameWithStrings(randomCharacters, diffLevel)
        }
    }

    override suspend fun getRandomWithCategory(category: String, diffLevel: Level): Board {
        temporaryDict = INITIAL_9_CHAR
        val listForCategory = charactersDao.getChineseByCategory(category).toSet().shuffled()
        if (listForCategory.size >= 9) {
            val randomCharacters = mutableListOf<String>()
            for (i in 0 until 9) {
                val randomChinese = listForCategory[i]
                randomCharacters.add(randomChinese)
            }
            return getNewGameWithStrings(randomCharacters, diffLevel)
        } else {
            val missingSize = 9 - listForCategory.size
            val adding = INITIAL_9_CHAR.subList(0, missingSize)
            val randomCharacters = listForCategory + adding
            return getNewGameWithStrings(randomCharacters, diffLevel)
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
            if (!charactersDao.categoryExists(NO_CAT)) {
                charactersDao.addOrEditCategory(CategoryDbModel(0, NO_CAT))
            }
            val entityList = mutableListOf<ChineseCharacter>()
            for (i in dbModelList) {
                val entity = mapper.mapDbChineseCharacterToDomainEntity(i)
                entityList.add(entity)
            }
            entityList
        }
    }

    override suspend fun getNewGame(
        nineCharacters: List<ChineseCharacter>,
        diffLevel: Level
    ): Board {
        val listString = mutableListOf<String>()
        for (i in nineCharacters) {
            listString.add(i.character)
        }
        temporaryDict = listString
        return withContext(Dispatchers.Default) {
            val grid = generateNumberGrid(diffLevel).values.toList()[0]
            val board = mapStringGridToBoard(grid)
            translateNumbersToCharacters(board)
        }
    }

    private suspend fun getNewGameWithStrings(
        nineCharacters: List<String>,
        diffLevel: Level
    ): Board {
        temporaryDict = nineCharacters
        return withContext(Dispatchers.Default) {
            val grid = generateNumberGrid(diffLevel).values.toList()[0]
            val board = mapStringGridToBoard(grid)
            translateNumbersToCharacters(board)
        }
    }

    override suspend fun saveGame(board: Board) {
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

    override suspend fun deleteCategory(catName: String) {
        charactersDao.deleteCategory(catName)
        charactersDao.getWholeDictionary().map {
            for (i in it) {
                if (i.category == catName) {
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

    override suspend fun getAllRecords(): List<Record> {
        val recordsFromDB = recordsDao.getTopFifteen()
        val records = mutableListOf<Record>()
        recordsFromDB.forEach {
            val record = mapper.mapDbModelToDomainRecord(it)
            records.add(record)
        }
        return records
    }

    override suspend fun supplyNewRecord(record: Record) {
        val recordsFromDB = recordsDao.getTopFifteen()
        if (recordsFromDB.size < 14) {
            recordsDao.saveNewRecord(mapper.mapDomainEntityToRecordDbModel(record))
        } else {
            val getsToTop = recordsFromDB.any {
                it.recordTime >= record.recordTime
            }
            if (getsToTop) {
                recordsDao.saveNewRecord(mapper.mapDomainEntityToRecordDbModel(record))
            }
        }
    }

    override suspend fun saveDictToCSV(): String {
        val dataDB = charactersDao.getAllDictAsList()
        val entityList = mutableListOf<ChineseCharacter>()
        for (i in dataDB) {
            val entity = mapper.mapDbChineseCharacterToDomainEntity(i)
            entityList.add(entity)
        }
        return saveFile(entityList, TO_CSV)
    }

    override suspend fun saveDictToJson(): String {
        val dataDB = charactersDao.getAllDictAsList()
        val entityList = mutableListOf<ChineseCharacter>()
        for (i in dataDB) {
            val entity = mapper.mapDbChineseCharacterToDomainEntity(i)
            entityList.add(entity)
        }
        return saveFile(entityList, TO_JSON)
    }

    override suspend fun getCharacterByChinese(chinese: String): ChineseCharacter? {
        val dbModel = charactersDao.getCharacterByChinese(chinese)
        return dbModel?.let { mapper.mapDbChineseCharacterToDomainEntity(it) }
    }

    private suspend fun generateNumberGrid(diffLevel: Level): Map<String, String> {
        return SudokuGame().fillGrid(diffLevel)
    }

    private fun mapStringGridToBoard(stringGrid: String): Board {
        val cells = List(SudokuGame.GRID_SIZE * SudokuGame.GRID_SIZE) { i ->
            Cell(
                i / SudokuGame.GRID_SIZE,
                i % SudokuGame.GRID_SIZE,
                stringGrid[i].toString()
            )
        }
        return Board(cells = cells, nineChars = temporaryDict)
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

    private fun isExternalStorageReadOnly(): Boolean {
        val extStorageState = Environment.getExternalStorageState()
        return Environment.MEDIA_MOUNTED_READ_ONLY == extStorageState
    }

    private fun isExternalStorageAvailable(): Boolean {
        val extStorageState = Environment.getExternalStorageState()
        return Environment.MEDIA_MOUNTED == extStorageState
    }

    private fun saveFile(myData: List<ChineseCharacter>, method: String): String {
        val path = if (isExternalStorageAvailable() && !isExternalStorageReadOnly()) {
            application.getExternalFilesDir(null)
        } else {
            application.filesDir
        }
        val exportDir = File(path, "")
        if (!exportDir.exists()) {
            exportDir.mkdirs()
        }
        when (method) {
            TO_CSV -> {
                val filename = CSV_FILE_NAME
                val file = File(exportDir, filename)
                var sb = ""
                var afterFirst = false
                for (character in myData) {
                    if (!afterFirst) {
                        sb += CSV_HEADERS
                    }
                    afterFirst = true
                    sb += "${character.character},${character.pinyin},${character.translation},${character.usages},${character.category}\n"
                }
                try {
                    file.writeText(sb)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return file.path
            }
            TO_JSON -> {
                val filename = JSON_FILE_NAME
                val file = File(exportDir, filename)
                val gson = Gson()
                val typeToken = object : TypeToken<List<ChineseCharacter>>() {}.type
                val jsonString = gson.toJson(myData, typeToken)
                try {
                    file.writeText(jsonString)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return file.path
            }
            else -> return ""
        }
    }




    companion object {
        private val INITIAL_9_CHAR = listOf("一", "二", "三", "四", "五", "六", "七", "八", "九")
        private const val EMPTY_CELL = "0"
        private const val NO_CAT = "-"
        private const val TO_JSON = "json"
        private const val TO_CSV = "csv"
        private const val CSV_FILE_NAME = "mandarindoku_dict.csv"
        private const val JSON_FILE_NAME = "mandarindoku_dict.json"
        private const val CSV_HEADERS = "Character,Pinyin,Translation,Usages,Category\n"
    }
}
