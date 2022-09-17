package com.panasetskaia.charactersudoku.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainViewModel: ViewModel() {

    fun getGame() {
        viewModelScope.launch(Dispatchers.Default) {

            Log.d("MY_TAG","null")
        }
    }

}
