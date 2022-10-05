package com.panasetskaia.charactersudoku.presentation.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.panasetskaia.charactersudoku.data.repository.CharacterSudokuRepositoryImpl
import com.panasetskaia.charactersudoku.domain.entities.ChineseCharacter
import com.panasetskaia.charactersudoku.domain.usecases.AddOrEditCharacterUseCase
import com.panasetskaia.charactersudoku.domain.usecases.DeleteCharacterFromDictUseCase
import com.panasetskaia.charactersudoku.domain.usecases.GetWholeDictionaryUseCase
import com.panasetskaia.charactersudoku.domain.usecases.SearchForCharacterUseCase
import kotlinx.coroutines.launch

class ChineseCharacterViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CharacterSudokuRepositoryImpl()
    private val addCharacterToDict = AddOrEditCharacterUseCase(repository)
    private val deleteCharacter = DeleteCharacterFromDictUseCase(repository)
    private val getWholeDict = GetWholeDictionaryUseCase(repository)
    private val searchForCharacter = SearchForCharacterUseCase(repository) //todo: подключить поиск

    val dictionaryLiveData = getWholeDict()

    private var _isDialogHiddenLiveData = MutableLiveData<Boolean>()
    val isDialogHiddenLiveData: LiveData<Boolean>
        get() = _isDialogHiddenLiveData

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
        _isDialogHiddenLiveData.postValue(isDialogHidden)
    }
}