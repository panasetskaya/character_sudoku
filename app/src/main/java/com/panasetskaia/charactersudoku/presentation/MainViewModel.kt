package com.panasetskaia.charactersudoku.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.panasetskaia.charactersudoku.domain.SudokuGame
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainViewModel: ViewModel() {

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
