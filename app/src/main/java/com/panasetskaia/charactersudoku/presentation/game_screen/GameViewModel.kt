package com.panasetskaia.charactersudoku.presentation.game_screen

import androidx.lifecycle.viewModelScope
import com.panasetskaia.charactersudoku.R
import com.panasetskaia.charactersudoku.domain.SUCCESS
import com.panasetskaia.charactersudoku.domain.entities.*
import com.panasetskaia.charactersudoku.domain.usecases.*
import com.panasetskaia.charactersudoku.presentation.base.BaseViewModel
import com.panasetskaia.charactersudoku.presentation.root.MainActivity
import com.panasetskaia.charactersudoku.utils.Event
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.trySendBlocking
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
    private val getRandomByCategory: GetRandomWithCategoryUseCase,
    private val supplyNewRecord: SupplyNewRecordUseCase,
    private val getOneCharacterByChineseUseCase: GetOneCharacterByChineseUseCase,
    private val getAllCategories: GetAllCategoriesUseCase,
    private val deleteCategory: DeleteCategoryUseCase,
    private val getGameStateFlow: GetGameStateUseCase,
    private val getStringUseCase: GetStringUseCase
) : BaseViewModel() {

    private var selectedRow = NO_SELECTION
    private var selectedCol = NO_SELECTION
    private lateinit var currentBoardCache: Board
    private lateinit var nineCharsCache: List<String>

    private val levelFlow = MutableStateFlow(Level.EASY)

    private val _toastFlow = MutableStateFlow<Event<String>?>(null)
    val toastFlow: StateFlow<Event<String>?>
        get() = _toastFlow

    private val _selectedCellFlow = MutableStateFlow(Pair(NO_SELECTION, NO_SELECTION))
    val selectedCellFlow: StateFlow<Pair<Int, Int>>
        get() = _selectedCellFlow

    private val _gameStateFlow = MutableSharedFlow<GameState>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val gameStateFlow: SharedFlow<GameState>
        get() = _gameStateFlow

    private val _categoriesFlow = MutableSharedFlow<List<String>>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val categoriesFlow: SharedFlow<List<String>>
        get() = _categoriesFlow

    init {
        getSavedBoard()
        collectGameState()
        updateCategories()
    }

    fun handleInput(number: Int, currentTime: Long) {
        if (selectedRow == NO_SELECTION || selectedCol == NO_SELECTION) return
        if (!currentBoardCache.getCell(selectedRow, selectedCol).isFixed) {
            val characterValue = nineCharsCache[number]
            currentBoardCache.getCell(selectedRow, selectedCol).value = characterValue
            currentBoardCache.getCell(selectedRow, selectedCol).isDoubtful = false
            currentBoardCache.timeSpent = currentTime
            updateViewModelBoard(currentBoardCache)
        }
        checkForSolution(currentTime)
    }

    fun markSelectedAsDoubtful(currentTime: Long) {
        val board = currentBoardCache
        val isCellDoubtful = board.getCell(selectedRow, selectedCol).isDoubtful
        board.getCell(selectedRow, selectedCol).isDoubtful = !isCellDoubtful
        board.timeSpent = currentTime
        updateViewModelBoard(board)
        launchRefreshedGame()
    }

    fun clearSelected(currentTime: Long) {
        if (selectedRow == NO_SELECTION || selectedCol == NO_SELECTION) return
        val board = currentBoardCache
        if (!board.getCell(selectedRow, selectedCol).isFixed) {
            board.getCell(selectedRow, selectedCol).value = EMPTY_CELL
        }
        board.timeSpent = currentTime
        updateViewModelBoard(board)
        launchRefreshedGame()
    }

    fun getNewRandomGame(diffLevel: Level) {
        updateSelection(NO_SELECTION, NO_SELECTION)
        viewModelScope.launch {
            _gameStateFlow.emit(REFRESHING)
            getRandomBoard(diffLevel)
        }
    }

    fun getRandomGameWithCategory(category: String, diffLevel: Level) {
        updateSelection(NO_SELECTION, NO_SELECTION)
        viewModelScope.launch {
            _gameStateFlow.emit(REFRESHING)
            getRandomByCategory(category, diffLevel)
        }
    }

//    fun getGameWithSelected(lvl: Level) {
//        updateSelection(NO_SELECTION, NO_SELECTION)
//        viewModelScope.launch {
//            _gameStateFlow.emit(REFRESHING)
//            getGameWithSelectedUseCase(lvl)
//        }
//    }

    fun saveBoard(timeSpent: Long? = null) {
        viewModelScope.launch {
            if (timeSpent != null) {
                val board = currentBoardCache.copy(timeSpent = timeSpent)
                updateViewModelBoard(board)
                saveGameUseCase(board)
                getSavedBoard()
            } else {
                saveGameUseCase(currentBoardCache)
            }
        }
    }

    private fun collectGameState() {
        viewModelScope.launch {
            getGameStateFlow().collectLatest {
                when (it) {
                    is WIN -> {
                        _gameStateFlow.emit(it)
                        updateSelection(NO_SELECTION, NO_SELECTION)
                    }
                    is DISPLAY -> {
                        _gameStateFlow.emit(it)
                        updateViewModelBoard(it.oldBoard)
                        updateSelection(NO_SELECTION, NO_SELECTION)
                    }
                    is PLAYING -> {
                        _gameStateFlow.emit(it)
                        updateViewModelBoard(it.currentBoard)
                        levelFlow.value = it.currentBoard.level

                    }
                    is REFRESHING -> {
                        _gameStateFlow.emit(it)
                        updateSelection(NO_SELECTION, NO_SELECTION)
                    }
                }
            }
        }
    }

    private fun getSavedBoard() {
        viewModelScope.launch {
            _gameStateFlow.emit(REFRESHING)
            getSavedGameUseCase()
        }
    }

    fun launchRefreshedGame() {
        _gameStateFlow.tryEmit(PLAYING(currentBoardCache))
    }

    fun launchOldBoard() {
        getSavedBoard()
    }

    private fun updateCategories() {
        viewModelScope.launch {
            getAllCategories().collectLatest {
                val listOfCategories = mutableListOf<String>()
                for (i in it) {
                    listOfCategories.add(i.categoryName)
                }
                _categoriesFlow.emit(listOfCategories)
            }

        }
    }

    private fun checkForSolution(currentTime: Long) {
        val boardCells = currentBoardCache.cells
        var count = 0
        for (i in boardCells) {
            if (i.value == EMPTY_CELL) {
                count++
            }
        }
        if (count < EMPTY_CELLS_MINIMUM) {
            viewModelScope.launch {
                val gameResult = getGameResult.invoke(currentBoardCache)
                if (gameResult is SUCCESS) {
                    val solution =
                        gameResult.solution.copy(alreadyFinished = true, timeSpent = currentTime)
                    updateViewModelBoard(solution)
                    saveBoard()
                    _gameStateFlow.emit(PLAYING(currentBoardCache))
                    saveRecord(currentTime)
                    _gameStateFlow.tryEmit(WIN)
                } else {
                    _toastFlow.value = Event(getStringUseCase(R.string.check_again))
                    _gameStateFlow.emit(PLAYING(currentBoardCache))
                    saveBoard()
                }
            }
        } else {
            saveBoard()
            _gameStateFlow.tryEmit(PLAYING(currentBoardCache))
        }
    }

    private fun updateViewModelBoard(newBoard: Board) {
        currentBoardCache = newBoard
        nineCharsCache = newBoard.nineChars
    }

    fun updateSelection(row: Int, col: Int) {
        selectedRow = row
        selectedCol = col
        _selectedCellFlow.value = Pair(row, col)
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

    fun getOneCharacterByChinese(chinese: String) {
        viewModelScope.launch {
            val char = getOneCharacterByChineseUseCase(chinese)
            char?.let {
                if (it.pinyin.isNotEmpty() || it.translation.isNotEmpty()) {
                    _toastFlow.value = Event("${it.character} [ ${it.pinyin.trim()} ] ${it.translation}")
                } else {
                    _toastFlow.value = Event(it.character)
                }

            }
        }
    }

    fun goToDictionary(activity: MainActivity) {
        activity.switchToDict()
    }

    override fun deleteThisCategory(cat: String) {
        viewModelScope.launch {
            deleteCategory(cat)
        }
    }

    companion object {
        internal const val EMPTY_CELLS_MINIMUM = 8
        private const val EMPTY_CELL = "0"
        private const val NO_SELECTION = -1
    }
}

