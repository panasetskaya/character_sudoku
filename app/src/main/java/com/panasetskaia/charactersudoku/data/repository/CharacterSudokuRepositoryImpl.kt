package com.panasetskaia.charactersudoku.data.repository

import android.app.Application
import android.os.Environment
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.panasetskaia.charactersudoku.data.database.board.BoardDao
import com.panasetskaia.charactersudoku.data.database.dictionary.CategoryDbModel
import com.panasetskaia.charactersudoku.data.database.dictionary.ChineseCharacterDao
import com.panasetskaia.charactersudoku.data.database.dictionary.ChineseCharacterDbModel
import com.panasetskaia.charactersudoku.data.database.records.RecordsDao
import com.panasetskaia.charactersudoku.data.gameGenerator.SudokuGame
import com.panasetskaia.charactersudoku.data.remote.RemoteRepo
import com.panasetskaia.charactersudoku.domain.CharacterSudokuRepository
import com.panasetskaia.charactersudoku.domain.FAILED
import com.panasetskaia.charactersudoku.domain.GameResult
import com.panasetskaia.charactersudoku.domain.SUCCESS
import com.panasetskaia.charactersudoku.domain.entities.*
import com.panasetskaia.charactersudoku.utils.myLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.util.*
import javax.inject.Inject


class CharacterSudokuRepositoryImpl @Inject constructor(
    private val application: Application,
    private val mapper: SudokuMapper,
    private val charactersDao: ChineseCharacterDao,
    private val boardDao: BoardDao,
    private val recordsDao: RecordsDao,
    private val remoteRepo: RemoteRepo
) : CharacterSudokuRepository {

    private var temporaryDict = INITIAL_9_CHAR

    private val _gameStateFlow = MutableSharedFlow<GameState>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    private val gameStateFlow: SharedFlow<GameState>
        get() = _gameStateFlow


    /**
     * Common functions:
     */
    override fun getStringResource(resId: Int): String {
        return application.getString(resId)
    }



    /**
     * Game functions:
     */

    override suspend fun getGameState(): Flow<GameState> {
        return gameStateFlow
    }



    override suspend fun getRandomBoard(diffLevel: Level) {
        try {
            temporaryDict = INITIAL_9_CHAR
            val wholeList = charactersDao.getAllChineseAsList().toSet().shuffled()
            val board = produceBoardOfBigList(wholeList, diffLevel)
            delay(800) //for refreshing animation
            _gameStateFlow.emit(PLAYING(board))
        } catch (e: Exception) {
            myLog("repository -> getRandomBoard() -> exception: ${e.message}")
        }
    }

    override suspend fun getRandomWithCategory(category: String, diffLevel: Level) {
        try {
            temporaryDict = INITIAL_9_CHAR
            val listForCategory = charactersDao.getChineseByCategory(category).toSet().shuffled()
            val board = produceBoardOfBigList(listForCategory, diffLevel)
            delay(800) //for refreshing animation
            _gameStateFlow.emit(PLAYING(board))
        } catch (e: Exception) {
            myLog("repository -> getRandomWithCategory() -> exception: ${e.message}")
        }
    }

    private suspend fun produceBoardOfBigList(list: List<String>, diffLevel: Level): Board {
        return if (list.size >= 9) {
            val randomCharacters = mutableListOf<String>()
            for (i in 0 until 9) {
                val randomChinese = list[i]
                randomCharacters.add(randomChinese)
            }
            getNewGameWithNineStrings(randomCharacters, diffLevel)
        } else {
            val missingSize = 9 - list.size
            val adding = INITIAL_9_CHAR.subList(0, missingSize)
            val randomCharacters = list + adding
            getNewGameWithNineStrings(randomCharacters, diffLevel)
        }
    }

    override suspend fun getSavedGame() {
        try {

            val boardDbModel = boardDao.getSavedGame()
            if (boardDbModel != null) {
                val nineChars = mutableListOf<String>()
                for (i in boardDbModel.nineChars) {
                    nineChars.add(i)
                }
                temporaryDict = nineChars
                val board = mapper.mapBoardDbModelToDomainEntity(boardDbModel)
                if (!board.alreadyFinished) {
                    _gameStateFlow.emit(PLAYING(board))
                } else {
                    _gameStateFlow.emit(DISPLAY(board))
                }

            } else {
                getRandomBoard(Level.EASY)
            }
        } catch (e: Exception) {
            myLog("repository -> getSavedGame() -> exception: ${e.message}")
        }

    }

    override suspend fun getGameWithSelected(diffLevel: Level) {

        try {
            _gameStateFlow.emit(REFRESHING)
            val selectedList = charactersDao.getSelectedCharacters()
            if (selectedList.size == 9) {
                markAllUnselected()
                val listAsStrings = getStringsFromSelectedCharacters(selectedList)
                val board = getNewGameWithNineStrings(listAsStrings, diffLevel)
                delay(800) //for refreshing animation
                _gameStateFlow.emit(PLAYING(board))
            } else {
                getSavedGame()
                myLog("repository -> getGameWithSelected() -> the number of selected is not nine")
            }
        } catch (e: Exception) {
            myLog("repository -> getGameWithSelected() -> exception: ${e.message}")
        }
    }

    override suspend fun getGameResult(board: Board): GameResult {
        val stringGrid = translateCharactersToNumbers(board)
        val solution = SudokuGame().getSolution(stringGrid)
        return if (solution != null) {
            val numberBoard = mapNumberGridWithTemporaryDictToBoard(solution, board.level)
            val solutionBoard = translateNumbersToCharacters(numberBoard)
            SUCCESS(solutionBoard)
        } else FAILED
    }

    override suspend fun saveGame(board: Board) {
        val boardDbModel = mapper.mapDomainBoardToDbModel(board)
        boardDao.saveGame(boardDbModel)
    }

    private suspend fun getNewGameWithNineStrings(
        nineCharacters: List<String>,
        diffLevel: Level
    ): Board {
        temporaryDict = nineCharacters
        return withContext(Dispatchers.Default) {
            val grid = generateNumberGrid(diffLevel).values.toList()[0]
            val board = mapNumberGridWithTemporaryDictToBoard(grid, diffLevel)
            translateNumbersToCharacters(board)
        }
    }

    private suspend fun generateNumberGrid(diffLevel: Level): Map<String, String> {
        return SudokuGame().fillGrid(diffLevel)
    }

    private fun mapNumberGridWithTemporaryDictToBoard(stringGrid: String, diffLevel: Level): Board {
        val cells = List(SudokuGame.GRID_SIZE * SudokuGame.GRID_SIZE) { i ->
            Cell(
                i / SudokuGame.GRID_SIZE,
                i % SudokuGame.GRID_SIZE,
                stringGrid[i].toString()
            )
        }
        return Board(cells = cells, nineChars = temporaryDict, level = diffLevel)
    }

    private fun translateNumbersToCharacters(board: Board): Board {
        for (i in board.cells) {
            if (i.value != EMPTY_CELL) {
                i.isFixed = true
                val index = i.value.toInt() - 1
                i.value = temporaryDict[index]
            }
        }
        return board
    }

    private fun translateCharactersToNumbers(board: Board): String {
        var gridString = ""
        for (i in board.cells) {
            var number = 0
            if (i.value != EMPTY_CELL) {
                number = temporaryDict.indexOf(i.value) + 1
            }
            gridString += number.toString()
        }
        return gridString
    }

    private fun getStringsFromSelectedCharacters(list: List<ChineseCharacterDbModel>): List<String> {
        val result = mutableListOf<String>()
        for (i in list) {
            val s = i.character
            result.add(s)
        }
        return result
    }


    /**
     * Records (top results) functions:
     */

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


    /**
     * Dictionary characters functions:
     */

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

    private suspend fun markAllUnselected() {
        try {
            val wholeDict = charactersDao.getAllDictAsList()
            for (i in wholeDict) {
                if (i.isChosen) {
                    val newChineseCharacter = i.copy(isChosen = false)
                    charactersDao.addOrEditCharacter(newChineseCharacter)
                }
            }
        } catch (e: Exception) {
            myLog("repo -> markAllUnselected: $e + ${e.message}")
        }
    }

    override suspend fun getCharacterByChinese(chinese: String): ChineseCharacter? {
        val dbModel = charactersDao.getCharacterByChinese(chinese)
        return dbModel?.let { mapper.mapDbChineseCharacterToDomainEntity(it) }
    }


    /**
     * Dictionary categories functions:
     */

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


    /**
     * Export and import functions:
     */

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
                try {
                    printWithCSVPrinter(file, myData)
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

    private fun isExternalStorageReadOnly(): Boolean {
        val extStorageState = Environment.getExternalStorageState()
        return Environment.MEDIA_MOUNTED_READ_ONLY == extStorageState
    }

    private fun isExternalStorageAvailable(): Boolean {
        val extStorageState = Environment.getExternalStorageState()
        return Environment.MEDIA_MOUNTED == extStorageState
    }

    private fun getSCVCellFilledWith(s: String): String {
        return if (s!="") s else "-"
    }

    private fun printWithCSVPrinter(file: File, myData: List<ChineseCharacter>) {
        val fileWriter = FileWriter(file)
        val writer = BufferedWriter(fileWriter)
        val csvPrinter = CSVPrinter(writer, CSVFormat.DEFAULT
            .withHeader("Character","Pinyin","Translation","Usages","Category"))
        for (character in myData) {
            val characterData = listOf(
                character.character,
                getSCVCellFilledWith(character.pinyin),
                getSCVCellFilledWith(character.translation),
                getSCVCellFilledWith(character.usages),
                getSCVCellFilledWith(character.category)
            )

            csvPrinter.printRecord(characterData)
        }
        csvPrinter.flush()
        csvPrinter.close()
    }

    override fun getRemoteEnglishHSK1Dict() {
        remoteRepo.getEnglishHSK1()
    }

    override fun getRemoteRussianHSK1Dict() {
        remoteRepo.getRussianHSK1()
    }


    companion object {
        private val INITIAL_9_CHAR = listOf("一", "二", "三", "四", "五", "六", "七", "八", "九")
        private const val EMPTY_CELL = "0"
        private const val NO_CAT = "-"
        private const val TO_JSON = "json"
        private const val TO_CSV = "csv"
        private const val CSV_FILE_NAME = "mandarindoku_dict.csv"
        private const val JSON_FILE_NAME = "mandarindoku_dict.json"
    }
}
