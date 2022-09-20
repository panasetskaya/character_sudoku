package com.panasetskaia.charactersudoku.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.panasetskaia.charactersudoku.data.repository.CharacterSudokuRepositoryImpl
import com.panasetskaia.charactersudoku.domain.entities.Board
import com.panasetskaia.charactersudoku.domain.entities.Cell
import kotlinx.coroutines.launch


class MainViewModel : ViewModel() {

    val repository = CharacterSudokuRepositoryImpl()
    private var selectedRow = -1
    private var selectedCol = -1

    private val _selectedCellLiveData = MutableLiveData<Pair<Int, Int>>()
    val selectedCellLiveData: LiveData<Pair<Int, Int>>
        get() = _selectedCellLiveData

    private val _boardLiveData = MutableLiveData<Board>()
    val boardLiveData: LiveData<Board>
        get() = _boardLiveData


    init {
        getNewGame()
        _selectedCellLiveData.postValue(Pair(selectedRow, selectedCol))
    }

    fun handleInput(number: Int) {
        if (selectedRow == -1 || selectedCol == -1) return
        val board = _boardLiveData.value
        if (board!=null) {
            board.getCell(selectedRow,selectedCol).value = number.toString()
            _boardLiveData.postValue(board)
        }
    }

    fun updateSelection(row: Int, col: Int) {
        selectedRow = row
        selectedCol = col
        _selectedCellLiveData.postValue(Pair(row, col))
    }

    private fun getNewGame() {
        viewModelScope.launch {
            val board = repository.getNewNumberGameTestFun()
            _boardLiveData.postValue(board)
        }
    }

    override fun onCleared() {
        super.onCleared()
        repository.cancelScope()
    }

    //TODO: 1) resolve left top corner box being empty issue; 2) make starting cells not changeable
}

