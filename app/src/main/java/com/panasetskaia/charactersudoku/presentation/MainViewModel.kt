package com.panasetskaia.charactersudoku.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.panasetskaia.charactersudoku.data.repository.CharacterSudokuRepositoryImpl
import com.panasetskaia.charactersudoku.domain.entities.Board
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

    private val _nineCharactersLiveData = MutableLiveData<List<String>>()
    val nineCharactersLiveData: MutableLiveData<List<String>>
        get() = _nineCharactersLiveData


    init {
        getNewGame()
        _selectedCellLiveData.postValue(Pair(selectedRow, selectedCol))
    }

    fun handleInput(number: Int) {
        if (selectedRow == -1 || selectedCol == -1) return
        val board = _boardLiveData.value
        if (board != null) {
            if (!board.getCell(selectedRow, selectedCol).isFixed) {
                val characterValue = nineCharactersLiveData.value?.get(number)
                characterValue?.let {
                    board.getCell(selectedRow, selectedCol).value = it
                    _boardLiveData.postValue(board)
                }
            }
        }
    }

    fun updateSelection(row: Int, col: Int) {
        selectedRow = row
        selectedCol = col
        _selectedCellLiveData.postValue(Pair(row, col))
    }

    private fun getNewGame() {
        val charactersList = getNineRandomCharacters()
        _nineCharactersLiveData.postValue(charactersList)
        viewModelScope.launch {
            val board = repository.getNewNumberGameTestFun()
            for (i in board.cells) {
                if (i.value != "0") {
                    i.isFixed = true
                    val index = i.value.toInt() - 1
                    i.value = charactersList[index]
                }
            }
            _boardLiveData.postValue(board)
        }
    }

    private fun getNineRandomCharacters(): List<String> {
        return repository.getNineRandomCharFromDict()
    }

    override fun onCleared() {
        super.onCleared()
        repository.cancelScope()
    }
}

