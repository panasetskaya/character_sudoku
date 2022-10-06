package com.panasetskaia.charactersudoku.presentation.viewmodels

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.panasetskaia.charactersudoku.R
import com.panasetskaia.charactersudoku.data.repository.CharacterSudokuRepositoryImpl
import com.panasetskaia.charactersudoku.domain.SUCCESS
import com.panasetskaia.charactersudoku.domain.entities.Board
import com.panasetskaia.charactersudoku.domain.usecases.GetNineRandomCharFromDictUseCase
import com.panasetskaia.charactersudoku.domain.usecases.GetResultUseCase
import com.panasetskaia.charactersudoku.domain.usecases.GetSavedGameUseCase
import com.panasetskaia.charactersudoku.domain.usecases.SaveGameUseCase
import kotlinx.coroutines.launch

class GameViewModel(application: Application) : AndroidViewModel(application) {

    val repository = CharacterSudokuRepositoryImpl()
    val getGameResult = GetResultUseCase(repository)
    val getNineRandomCharFromDict = GetNineRandomCharFromDictUseCase(repository)
    val getSavedGameUseCase = GetSavedGameUseCase(repository)
    val saveGameUseCase = SaveGameUseCase(repository)

    private var selectedRow = -1
    private var selectedCol = -1

    private val _selectedCellLiveData = MutableLiveData<Pair<Int, Int>>()
    val selectedCellLiveData: LiveData<Pair<Int, Int>>
        get() = _selectedCellLiveData

    private val _boardLiveData = MutableLiveData<Board>()
    val boardLiveData: LiveData<Board>
        get() = _boardLiveData

    private var _nineCharactersLiveData = MutableLiveData<List<String>>()
    val nineCharactersLiveData: LiveData<List<String>>
        get() = _nineCharactersLiveData

    private var _settingsFinishedLiveData = MutableLiveData<Boolean>()
    val settingsFinishedLiveData: LiveData<Boolean>
        get() = _settingsFinishedLiveData


    init {
        getNewRandomGame()
        _selectedCellLiveData.postValue(Pair(selectedRow, selectedCol))
        _settingsFinishedLiveData.postValue(true)
    }

    fun handleInput(number: Int) {
        if (selectedRow == -1 || selectedCol == -1) return
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

    fun updateSelection(row: Int, col: Int) {
        selectedRow = row
        selectedCol = col
        _selectedCellLiveData.postValue(Pair(row, col))
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
        if (selectedRow == -1 || selectedCol == -1) return
        val board = _boardLiveData.value
        board?.let {
            if (!it.getCell(selectedRow, selectedCol).isFixed) {
                it.getCell(selectedRow, selectedCol).value = "0"
            }
            _boardLiveData.postValue(it)
        }
    }

    private fun checkForSolution() {
        val boardCells = boardLiveData.value?.cells
        var count = 0
        boardCells?.let { cellsList ->
            for (i in cellsList) {
                if (i.value == "0") {
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
        // todo: у нас здесь рэндом пока!
        selectedRow = -1
        selectedCol = -1
        _selectedCellLiveData.postValue(Pair(-1, -1))
        viewModelScope.launch {
            val nineChars = getNineRandomCharFromDict.invoke()
            _nineCharactersLiveData.postValue(nineChars)
            if (nineChars.size < 9) {
                Toast.makeText(
                    getApplication(),
                    getApplication<Application>().getString(R.string.dict_is_empty),
                    Toast.LENGTH_SHORT
                )
                    .show()
            } else {
                val translatedBoard = repository.getNewGameTestFun()
                _boardLiveData.postValue(translatedBoard)
            }
        }
    }

    fun getGameWithSelected() {

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

    fun getSavedBoard() {
        viewModelScope.launch {
            val savedBoard = getSavedGameUseCase()
            _boardLiveData.postValue(savedBoard)
        }
    }

    companion object {
        internal const val EMPTY_CELLS_MINIMUM = 8
    }
}

