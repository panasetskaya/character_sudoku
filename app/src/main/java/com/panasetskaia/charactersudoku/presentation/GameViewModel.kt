package com.panasetskaia.charactersudoku.presentation

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.*
import com.panasetskaia.charactersudoku.data.repository.CharacterSudokuRepositoryImpl
import com.panasetskaia.charactersudoku.domain.entities.Board
import com.panasetskaia.charactersudoku.domain.usecases.GetNineRandomCharFromDictUseCase
import com.panasetskaia.charactersudoku.domain.usecases.GetSolutionUseCase
import kotlinx.coroutines.launch


class GameViewModel(application: Application) : AndroidViewModel(application) {

    val repository = CharacterSudokuRepositoryImpl()
    val getSolution = GetSolutionUseCase(repository)
    val getNineRandomCharFromDict = GetNineRandomCharFromDictUseCase(repository)

    private var selectedRow = -1
    private var selectedCol = -1

    private val _selectedCellLiveData = MutableLiveData<Pair<Int, Int>>()
    val selectedCellLiveData: LiveData<Pair<Int, Int>>
        get() = _selectedCellLiveData

    private val _boardLiveData = MutableLiveData<Board>()
    val boardLiveData: LiveData<Board>
        get() = _boardLiveData

    private var _nineCharacters = listOf<String>()
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

    fun checkForSolution() {
        val boardCells = boardLiveData.value?.cells
        var count = 0
        boardCells?.let { cellsList ->
            for (i in cellsList) {
                if (i.value=="0"){
                    count++
                }
            }
        }
        if (count<6) {
            val gridString = translateCharactersToNumbers()
            viewModelScope.launch {
                val solution = getSolution(gridString)
                if (solution != null) {
                    Toast.makeText(getApplication(), "Ура, игра завершена!", Toast.LENGTH_SHORT)
                        .show()
                    _boardLiveData.postValue(translateNumbersToCharacters(solution))
                } else {
                    Toast.makeText(getApplication(), "Проверьте все еще раз!", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun getNewGame() {
        _nineCharacters = getNineRandomCharacters()
        viewModelScope.launch {
            val translatedBoard = translateNumbersToCharacters(
                repository.getNewNumberGameTestFun()
            )
            _boardLiveData.postValue(translatedBoard)
        }
    }

    private fun getNineRandomCharacters(): List<String> {
        return getNineRandomCharFromDict()
    }

    override fun onCleared() {
        super.onCleared()
        repository.cancelScope() // todo: как-то по-другому надо его отменять, не здесь
    }

    private fun translateNumbersToCharacters(board: Board): Board {
        for (i in board.cells) {
            if (i.value != "0") {
                i.isFixed = true
                val index = i.value.toInt() - 1
                i.value = nineCharacters[index]
            }
        }
        return board
    }

    private fun translateCharactersToNumbers(): String {
        val boardCells = boardLiveData.value?.cells
        var gridString = ""
        boardCells?.let { cellsList ->
            for (i in cellsList) {
                var number = 0
                if (i.value != "0") {
                    number = nineCharacters.indexOf(i.value) + 1
                }
                gridString += number.toString()
            }
        }
        return gridString
    }

    //todo: перенести логику из translateNumbersToCharacters, translateCharactersToNumbers,
    // checkForSolution в репозиторий (проверка решения - это логика игры все-таки,
    // а не отображения). Пусть репозиторий возвращает GameResult

    //todo: добавить возможность выделения сомнительных решений цветом(например, черным)
    // с помощью долго нажатия на клетку - поле isDoubtful в классе Cell

    //todo: таймер! с паузой игры

    //todo: сохранение текущей Board в базу данных
}

