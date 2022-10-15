package com.panasetskaia.charactersudoku.presentation.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.panasetskaia.charactersudoku.domain.entities.ChineseCharacter
import com.panasetskaia.charactersudoku.domain.usecases.AddOrEditCharacterUseCase
import com.panasetskaia.charactersudoku.domain.usecases.DeleteCharacterFromDictUseCase
import com.panasetskaia.charactersudoku.domain.usecases.GetWholeDictionaryUseCase
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.launch
import javax.inject.Inject

class ChineseCharacterViewModel @Inject constructor(
    application: Application,
    private val addCharacterToDict: AddOrEditCharacterUseCase,
    private val deleteCharacter: DeleteCharacterFromDictUseCase,
    private val getWholeDict: GetWholeDictionaryUseCase
) : AndroidViewModel(application) {

    private val _dictionaryFlow = MutableSharedFlow<List<ChineseCharacter>>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val dictionaryFlow: SharedFlow<List<ChineseCharacter>>
    get() = _dictionaryFlow

    private var _isDialogHiddenStateFlow = MutableStateFlow(true)
    val isDialogHiddenStateFlow: StateFlow<Boolean>
        get() = _isDialogHiddenStateFlow

    val selectedCharactersSharedFlow = dictionaryFlow.map { wholeDictionary ->
        val selectedCharacters = mutableListOf<ChineseCharacter>()
        for (i in wholeDictionary) {
            if (i.isChosen) {
                selectedCharacters.add(i)
            }
        }
        selectedCharacters.toList()
    }.shareIn(viewModelScope, WhileSubscribed(5000), replay = 1)

    private fun updateDictionary() {
        viewModelScope.launch {
            _dictionaryFlow.emitAll(
                getWholeDict()
            )
        }
    }

    init {
        updateDictionary()
    }

    fun deleteCharacterFromDict(chineseCharacterId: Int) {
        viewModelScope.launch {
            deleteCharacter(chineseCharacterId)
        }
        finishDeleting(true)
    }

    fun addOrEditCharacter(chineseCharacter: ChineseCharacter) {
        viewModelScope.launch {
            addCharacterToDict(chineseCharacter)
        }
    }

    fun changeIsChosenState(chineseCharacter: ChineseCharacter) {
        val newChineseCharacter = chineseCharacter.copy(isChosen = !chineseCharacter.isChosen)
        viewModelScope.launch {
            addCharacterToDict(newChineseCharacter)
        }
    }

    fun finishDeleting(isDialogHidden: Boolean) {
        _isDialogHiddenStateFlow.value = isDialogHidden
    }

    fun markAllUnselected() {
        viewModelScope.launch {
            dictionaryFlow.collect {
                for (i in it) {
                    if (i.isChosen) {
                        val newChineseCharacter = i.copy(isChosen = false)
                        addCharacterToDict(newChineseCharacter)
                    }
                }
                _dictionaryFlow.emitAll(
                    getWholeDict()
                )
            }
        }
    }
}