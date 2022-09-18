package com.panasetskaia.charactersudoku.presentation

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.panasetskaia.charactersudoku.data.SudokuGame
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainViewModel: ViewModel() {

    private val _selectedCellLiveData = MutableLiveData<Pair<Int, Int>>()
    val selectedCellLiveData: MutableLiveData<Pair<Int, Int>>
        get() = _selectedCellLiveData

    private var selectedRow = -1
    private var selectedCol = -1

    init {
        selectedCellLiveData.postValue(Pair(selectedRow, selectedCol))
    }

    fun updateSelectedCell(row: Int, col: Int) {
        selectedRow = row
        selectedCol = col
        selectedCellLiveData.postValue(Pair(row, col))
    }

    fun getGame() {
        viewModelScope.launch(Dispatchers.Default) {
            val map2 = SudokuGame().fillGrid()
            Log.d("MY_TAG",map2.toString())
            val map3 = SudokuGame().fillGrid()
            Log.d("MY_TAG",map3.toString())
            val map4 = SudokuGame().fillGrid()
            Log.d("MY_TAG",map4.toString())
        }
    }

}
