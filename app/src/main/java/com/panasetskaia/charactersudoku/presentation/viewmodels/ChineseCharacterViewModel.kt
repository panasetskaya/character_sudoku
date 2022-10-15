package com.panasetskaia.charactersudoku.presentation.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.panasetskaia.charactersudoku.domain.entities.ChineseCharacter
import com.panasetskaia.charactersudoku.domain.usecases.AddOrEditCharacterUseCase
import com.panasetskaia.charactersudoku.domain.usecases.DeleteCharacterFromDictUseCase
import com.panasetskaia.charactersudoku.domain.usecases.GetWholeDictionaryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class ChineseCharacterViewModel @Inject constructor(
    application: Application,
    private val addCharacterToDict: AddOrEditCharacterUseCase,
    private val deleteCharacter: DeleteCharacterFromDictUseCase,
    private val getWholeDict: GetWholeDictionaryUseCase
) : AndroidViewModel(application) {

    val dictionaryLiveData = getWholeDict()

//    private var _isDialogHiddenLiveData = MutableLiveData<Boolean>()
//    val isDialogHiddenLiveData: LiveData<Boolean>
//        get() = _isDialogHiddenLiveData

    private var _isDialogHiddenStateFlow = MutableStateFlow(true)
    val isDialogHiddenStateFlow: StateFlow<Boolean>
        get() = _isDialogHiddenStateFlow

    val selectedCharactersLiveData = Transformations.map(dictionaryLiveData) { wholeDictionary ->
        val selectedCharacters = mutableListOf<ChineseCharacter>()
        for (i in wholeDictionary) {
            if (i.isChosen) {
                selectedCharacters.add(i)
            }
        }
        selectedCharacters.toList()
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
        val dictionary = dictionaryLiveData.value
        dictionary?.let { dictList ->
            viewModelScope.launch {
                for (i in dictList) {
                    if (i.isChosen) {
                        val newChineseCharacter = i.copy(isChosen = false)
                        addCharacterToDict(newChineseCharacter)
                    }
                }
            }
        }
    }
}