package com.panasetskaia.charactersudoku.presentation.settings_screen

import androidx.lifecycle.viewModelScope
import com.panasetskaia.charactersudoku.domain.entities.Record
import com.panasetskaia.charactersudoku.domain.usecases.GetTopFifteenRecordsUseCase
import com.panasetskaia.charactersudoku.presentation.base.BaseViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class RecordsViewModel @Inject constructor(
    private val getTopFifteenRecords: GetTopFifteenRecordsUseCase
    ): BaseViewModel() {

    private val _recordsFlow = MutableSharedFlow<List<Record>>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val recordsFlow: SharedFlow<List<Record>>
        get() = _recordsFlow


    fun getRecords() {
        viewModelScope.launch {
            _recordsFlow.tryEmit(
                getTopFifteenRecords()
            )
        }
    }

    override fun deleteThisCategory(cat: String) {
    }
}