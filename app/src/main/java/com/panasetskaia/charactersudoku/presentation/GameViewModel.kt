package com.panasetskaia.charactersudoku.presentation

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.panasetskaia.charactersudoku.data.repository.CharacterSudokuRepositoryImpl
import com.panasetskaia.charactersudoku.domain.SUCCESS
import com.panasetskaia.charactersudoku.domain.entities.Board
import com.panasetskaia.charactersudoku.domain.usecases.GetNineRandomCharFromDictUseCase
import com.panasetskaia.charactersudoku.domain.usecases.GetResultUseCase
import kotlinx.coroutines.launch


class GameViewModel(application: Application) : AndroidViewModel(application) {

    val repository = CharacterSudokuRepositoryImpl()
    val getGameResult = GetResultUseCase(repository)
    val getNineRandomCharFromDict = GetNineRandomCharFromDictUseCase(repository)

    private var selectedRow = -1
    private var selectedCol = -1

    private val _selectedCellLiveData = MutableLiveData<Pair<Int, Int>>()
    val selectedCellLiveData: LiveData<Pair<Int, Int>>
        get() = _selectedCellLiveData

    private val _boardLiveData = MutableLiveData<Board>()
    val boardLiveData: LiveData<Board>
        get() = _boardLiveData

    private var _nineCharacters: List<String> = listOf()
    val nineCharacters: List<String>
        get() = _nineCharacters


    init {
        getNewGame()
        _selectedCellLiveData.postValue(Pair(selectedRow, selectedCol))
    }

    fun handleInput(number: Int) {
        if (selectedRow == -1 || selectedCol == -1) return
        val board = _boardLiveData.value
        if (board != null) {
            if (!board.getCell(selectedRow, selectedCol).isFixed) {
                val characterValue = nineCharacters[number]
                board.getCell(selectedRow, selectedCol).value = characterValue
                board.getCell(selectedRow, selectedCol).isDoubtful = false
                _boardLiveData.postValue(board)
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
        if (board != null) {
            val isCellDoubtful = board.getCell(selectedRow, selectedCol).isDoubtful
            board.getCell(selectedRow, selectedCol).isDoubtful = !isCellDoubtful
            _boardLiveData.postValue(board)
        }
    }

    fun checkForSolution() {
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
                    val gameResult = getGameResult(board)
                    if (gameResult is SUCCESS) {
                        Toast.makeText(getApplication(), "Ура, игра завершена!", Toast.LENGTH_SHORT)
                            .show()
                        _boardLiveData.postValue(gameResult.solution)
                    } else {
                        Toast.makeText(
                            getApplication(),
                            "Проверьте все еще раз!",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }
            }
        }
    }

    private fun getNewGame() {
        val tryToGetharacters = getNineRandomCharacters()
        if (tryToGetharacters != null) {
            _nineCharacters = tryToGetharacters
        } else {
            Toast.makeText(
                getApplication(),
                "В словаре пока пусто. Добавьте иероглифов!",
                Toast.LENGTH_SHORT
            )
                .show()
        }
        viewModelScope.launch {
            val translatedBoard = repository.getNewNumberGameTestFun()
            _boardLiveData.postValue(translatedBoard)
        }
    }

    private fun getNineRandomCharacters(): List<String>? {
        return getNineRandomCharFromDict()
    }

    override fun onCleared() {
        super.onCleared()
        repository.cancelScope() // todo: как-то по-другому надо его отменять, не здесь
    }

    companion object {
        internal const val EMPTY_CELLS_MINIMUM = 6
    }

    //todo: сохранение текущей Board в базу данных
}

