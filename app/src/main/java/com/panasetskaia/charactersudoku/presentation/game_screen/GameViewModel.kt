package com.panasetskaia.charactersudoku.presentation.game_screen

import androidx.lifecycle.viewModelScope
import com.panasetskaia.charactersudoku.domain.SUCCESS
import com.panasetskaia.charactersudoku.domain.entities.Board
import com.panasetskaia.charactersudoku.domain.entities.ChineseCharacter
import com.panasetskaia.charactersudoku.domain.entities.Level
import com.panasetskaia.charactersudoku.domain.entities.Record
import com.panasetskaia.charactersudoku.domain.usecases.*
import com.panasetskaia.charactersudoku.presentation.base.BaseViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class GameViewModel @Inject constructor(
    private val getGameResult: GetResultUseCase,
    private val getRandomBoard: GetRandomBoard,
    private val getSavedGameUseCase: GetSavedGameUseCase,
    private val saveGameUseCase: SaveGameUseCase,
    private val getNewGameWithSel: GetNewGameUseCase,
    private val getRandomByCategory: GetRandomWithCategoryUseCase,
    private val supplyNewRecord: SupplyNewRecordUseCase,
    private val getTopFifteenRecords: GetTopFifteenRecordsUseCase,
    private val getOneCharacterByChineseUseCase: GetOneCharacterByChineseUseCase,
    private val getGameWithSelected: GetGameWithSelected
) : BaseViewModel() {

    private var selectedRow = NO_SELECTION
    private var selectedCol = NO_SELECTION
    private lateinit var currentBoard: Board
    private lateinit var nineChars: List<String>

    private val levelFlow = MutableStateFlow(Level.MEDIUM)

    private val _selectedCellFlow = MutableStateFlow(Pair(NO_SELECTION, NO_SELECTION))
    val selectedCellFlow: StateFlow<Pair<Int, Int>>
        get() = _selectedCellFlow

    private val _finalErrorFlow = MutableStateFlow(false)
    val finalErrorFlow: StateFlow<Boolean>
        get() = _finalErrorFlow

    private val _gameStateFlow = MutableSharedFlow<GameState>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val gameStateFlow: SharedFlow<GameState>
        get() = _gameStateFlow

    private val _recordsFlow = MutableSharedFlow<List<Record>>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val recordsFlow: SharedFlow<List<Record>>
        get() = _recordsFlow

    init {
        getSavedBoard()
    }

    fun handleInput(number: Int, currentTime: Long) {
        if (selectedRow == NO_SELECTION || selectedCol == NO_SELECTION) return
        if (!currentBoard.getCell(selectedRow, selectedCol).isFixed) {
            val characterValue = nineChars[number]
            currentBoard.getCell(selectedRow, selectedCol).value = characterValue
            currentBoard.getCell(selectedRow, selectedCol).isDoubtful = false
            currentBoard.timeSpent = currentTime
            updateViewModelBoard(currentBoard)
        }
        checkForSolution(currentTime)
    }

    fun markSelectedAsDoubtful(currentTime: Long) {
        val board = currentBoard
        val isCellDoubtful = board.getCell(selectedRow, selectedCol).isDoubtful
        board.getCell(selectedRow, selectedCol).isDoubtful = !isCellDoubtful
        board.timeSpent = currentTime
        updateViewModelBoard(board)
        launchRefreshedGame()
    }

    fun clearSelected(currentTime: Long) {
        if (selectedRow == NO_SELECTION || selectedCol == NO_SELECTION) return
        val board = currentBoard
        if (!board.getCell(selectedRow, selectedCol).isFixed) {
            board.getCell(selectedRow, selectedCol).value = EMPTY_CELL
        }
        board.timeSpent = currentTime
        updateViewModelBoard(board)
        launchRefreshedGame()
    }

    fun getNewRandomGame(diffLevel: Level) {
        levelFlow.value = diffLevel
        updateSelection(NO_SELECTION, NO_SELECTION)
        viewModelScope.launch {
            val randomBoard =
                getRandomBoard.invoke(diffLevel).copy(timeSpent = 0, alreadyFinished = false)
            updateViewModelBoard(randomBoard)
            _gameStateFlow.emit(REFRESHING)
        }
    }

    fun getRandomGameWithCategory(category: String, diffLevel: Level) {
        levelFlow.value = diffLevel
        updateSelection(NO_SELECTION, NO_SELECTION)
        viewModelScope.launch {
            val randomBoard = getRandomByCategory(category, diffLevel).copy(
                timeSpent = 0,
                alreadyFinished = false
            )
            updateViewModelBoard(randomBoard)
            _gameStateFlow.emit(REFRESHING)
        }
    }

    fun getGameWithSelected() {
        updateSelection(NO_SELECTION, NO_SELECTION)
        viewModelScope.launch {
            val newBoard = getGameWithSelected(levelFlow.value).copy(
                timeSpent = 0,
                alreadyFinished = false
            )
            updateViewModelBoard(newBoard)
            _gameStateFlow.emit(REFRESHING)
        }
    }

    fun setSettingsState() {
        _gameStateFlow.tryEmit(SETTING)
    }

    fun saveBoard(timeSpent: Long? = null) {
        viewModelScope.launch {
            if (timeSpent != null) {
                val board = currentBoard.copy(timeSpent = timeSpent)
                updateViewModelBoard(board)
                saveGameUseCase(board)
                getSavedBoard()
            } else {
                saveGameUseCase(currentBoard)
            }
        }
    }

    private fun getSavedBoard() {
        viewModelScope.launch {
            val savedBoard = getSavedGameUseCase()
            savedBoard?.let {
                if (it.alreadyFinished) {
                    _gameStateFlow.tryEmit(DISPLAY(it))
                } else {
                    _gameStateFlow.tryEmit(PLAYING(it))
                }
                updateViewModelBoard(it)
                updateSelection(NO_SELECTION, NO_SELECTION)
            } ?: setInitialGame()
        }
    }

    fun launchRefreshedGame() {
        _gameStateFlow.tryEmit(PLAYING(currentBoard))
    }

    fun launchOldBoard() {
        getSavedBoard()
    }

    private fun checkForSolution(currentTime: Long) {
        _finalErrorFlow.value = false
        val boardCells = currentBoard.cells
        var count = 0
        for (i in boardCells) {
            if (i.value == EMPTY_CELL) {
                count++
            }
        }
        if (count < EMPTY_CELLS_MINIMUM) {
            viewModelScope.launch {
                val gameResult = getGameResult.invoke(currentBoard)
                if (gameResult is SUCCESS) {
                    val solution =
                        gameResult.solution.copy(alreadyFinished = true, timeSpent = currentTime)
                    updateViewModelBoard(solution)
                    saveBoard()
                    _gameStateFlow.emit(PLAYING(currentBoard))
                    saveRecord(currentTime)
                    _gameStateFlow.tryEmit(WIN)
                } else {
                    _finalErrorFlow.value = true
                    _gameStateFlow.emit(PLAYING(currentBoard))
                    saveBoard()
                }
            }
        } else {
            saveBoard()
            _gameStateFlow.tryEmit(PLAYING(currentBoard))
        }
    }

    private fun updateViewModelBoard(newBoard: Board) {
        currentBoard = newBoard
        nineChars = newBoard.nineChars
    }

    fun updateSelection(row: Int, col: Int) {
        selectedRow = row
        selectedCol = col
        _selectedCellFlow.value = Pair(row, col)
    }

    fun setLevel(chosenLevel: Level) {
        levelFlow.value = chosenLevel
    }

    private fun saveRecord(recordTime: Long) {
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formattedDate = current.format(formatter)
        val newRecord = Record(0, -recordTime, levelFlow.value, formattedDate)
        viewModelScope.launch {
            supplyNewRecord(newRecord)
        }
    }

    fun getRecords() {
        viewModelScope.launch {
            _recordsFlow.tryEmit(
                getTopFifteenRecords()
            )
        }
    }

    private fun setInitialGame() {
        getNewRandomGame(Level.EASY)
        _gameStateFlow.tryEmit(SETTING)
    }

    fun getOneCharacterByChinese(chinese: String): SharedFlow<ChineseCharacter> {
        return flow<ChineseCharacter> { getOneCharacterByChineseUseCase(chinese) }.shareIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            replay = 1
        )
        //todo: проверить работу, когда заработает перезапуск игры

//        dictionaryFlow.map { wholeDictionary ->
//            var characterWeNeed = ChineseCharacter(
//                character = "",
//                pinyin = "",
//                translation = "",
//                usages = "",
//                category = "-"
//            )
//            for (i in wholeDictionary) {
//                if (i.character == chinese) {
//                    characterWeNeed = i
//                }
//            }
//            characterWeNeed
//        }.shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000), replay = 1)
    }

    companion object {
        internal const val EMPTY_CELLS_MINIMUM = 8
        private const val EMPTY_CELL = "0"
        private const val NO_SELECTION = -1
    }
}

