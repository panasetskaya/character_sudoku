package com.panasetskaia.charactersudoku.presentation.viewmodels

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.panasetskaia.charactersudoku.R
import com.panasetskaia.charactersudoku.domain.SUCCESS
import com.panasetskaia.charactersudoku.domain.entities.Board
import com.panasetskaia.charactersudoku.domain.entities.ChineseCharacter
import com.panasetskaia.charactersudoku.domain.usecases.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class GameViewModel @Inject constructor(
    application: Application,
    private val getGameResult: GetResultUseCase,
    private val getRandomBoard: GetRandomBoard,
    private val getSavedGameUseCase: GetSavedGameUseCase,
    private val saveGameUseCase: SaveGameUseCase,
    private val getNewGameWithSel: GetNewGameUseCase,
) : AndroidViewModel(application) {


    private val _selectedCellFlow = MutableStateFlow(Pair(NO_SELECTION, NO_SELECTION))
    val selectedCellFlow: StateFlow<Pair<Int, Int>>
        get() = _selectedCellFlow

    private var selectedRow = NO_SELECTION
    private var selectedCol = NO_SELECTION

    private val _boardLiveData = MutableLiveData<Board>()
    val boardLiveData: LiveData<Board>
        get() = _boardLiveData

    private var currentBoard = boardLiveData.value ?: Board(-1, 9,listOf(), listOf())

    private var _nineCharactersLiveData = MutableLiveData<List<String>>()
    val nineCharactersLiveData: LiveData<List<String>>
        get() = _nineCharactersLiveData

    private var _settingsFinishedLiveData = MutableLiveData<Boolean>()
    val settingsFinishedLiveData: LiveData<Boolean>
        get() = _settingsFinishedLiveData


    init {
        getSavedBoard()
        updateSelection(NO_SELECTION, NO_SELECTION)
        _settingsFinishedLiveData.postValue(true)
    }

    fun handleInput(number: Int) {
        if (selectedRow == NO_SELECTION || selectedCol == NO_SELECTION) return
        val board = _boardLiveData.value
        board?.let { board ->
            if (!board.getCell(selectedRow, selectedCol).isFixed) {
                nineCharactersLiveData.value?.let { charList ->
                    val characterValue = charList[number]
                    board.getCell(selectedRow, selectedCol).value = characterValue
                    board.getCell(selectedRow, selectedCol).isDoubtful = false
                    _boardLiveData.postValue(board)
                }
            }
        }
        checkForSolution()
    }

    private fun updateBoard(newBoard: Board) {

    }

    fun updateSelection(row: Int, col: Int) {
        selectedRow = row
        selectedCol = col
        _selectedCellFlow.value = Pair(row,col)
    }

    fun markSelectedAsDoubtful() {
        val board = _boardLiveData.value
        board?.let {
            val isCellDoubtful = it.getCell(selectedRow, selectedCol).isDoubtful
            it.getCell(selectedRow, selectedCol).isDoubtful = !isCellDoubtful
            _boardLiveData.postValue(it)
        }
    }

    fun clearSelected() {
        if (selectedRow == NO_SELECTION || selectedCol == NO_SELECTION) return
        val board = _boardLiveData.value
        board?.let {
            if (!it.getCell(selectedRow, selectedCol).isFixed) {
                it.getCell(selectedRow, selectedCol).value = EMPTY_CELL
            }
            _boardLiveData.postValue(it)
        }
    }

    private fun checkForSolution() {
        val boardCells = boardLiveData.value?.cells
        var count = 0
        boardCells?.let { cellsList ->
            for (i in cellsList) {
                if (i.value == EMPTY_CELL) {
                    count++
                }
            }
        }
        if (count < EMPTY_CELLS_MINIMUM) {
            viewModelScope.launch {
                boardLiveData.value?.let { board ->
                    val gameResult = getGameResult.invoke(board)
                    if (gameResult is SUCCESS) {
                        Toast.makeText(
                            getApplication(),
                            getApplication<Application>().getString(R.string.game_succesful),
                            Toast.LENGTH_SHORT
                        )
                            .show()
                        _boardLiveData.postValue(gameResult.solution)
                    } else {
                        Toast.makeText(
                            getApplication(),
                            getApplication<Application>().getString(R.string.check_again),
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }
            }
        }
    }

    fun getNewRandomGame() {
        updateSelection(NO_SELECTION, NO_SELECTION)
        viewModelScope.launch {
            val randomBoard = getRandomBoard.invoke()
            Log.d("My_RES", randomBoard.nineChars.toString())
            _nineCharactersLiveData.postValue(randomBoard.nineChars)
            _boardLiveData.postValue(randomBoard)
            setSettingsState(true)

        }
    }

    fun getGameWithSelected(selected: List<ChineseCharacter>) {
        updateSelection(NO_SELECTION, NO_SELECTION)
        viewModelScope.launch {
            val listString = mutableListOf<String>()
            for (i in selected) {
                listString.add(i.character)
            }
            _nineCharactersLiveData.postValue(listString)
            val board = getNewGameWithSel(selected)
            _boardLiveData.postValue(board)
        }
    }

    fun setSettingsState(areSettingsDone: Boolean) {
        _settingsFinishedLiveData.postValue(areSettingsDone)
    }


    fun saveBoard() {
        val currentBoard = boardLiveData.value
        currentBoard?.let { board ->
            viewModelScope.launch {
                saveGameUseCase(board)
            }
        }
    }

    private fun getSavedBoard() {
        viewModelScope.launch {
            val savedBoard = getSavedGameUseCase()
            savedBoard?.let {
                _nineCharactersLiveData.postValue(it.nineChars)
                _boardLiveData.postValue(it)
                updateSelection(0, 0)
            }
            if (savedBoard == null) {
                getNewRandomGame()
            }
        }
    }

    companion object {
        internal const val EMPTY_CELLS_MINIMUM = 8
        private const val EMPTY_CELL = "0"
        private const val NO_SELECTION = -1
    }
}

