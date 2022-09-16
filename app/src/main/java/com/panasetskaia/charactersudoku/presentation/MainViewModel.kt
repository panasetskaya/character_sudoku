package com.panasetskaia.charactersudoku.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.panasetskaia.charactersudoku.domain.Level
import com.panasetskaia.charactersudoku.domain.SudokuGame
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainViewModel: ViewModel() {

    fun getGame(level: Level) {
        viewModelScope.launch(Dispatchers.Default) {
            val map = SudokuGame().fillGrid(level)
            Log.d("MY_TAG",map.toString())
        }
    }

}
