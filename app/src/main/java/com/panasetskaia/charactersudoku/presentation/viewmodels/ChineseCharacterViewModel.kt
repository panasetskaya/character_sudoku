package com.panasetskaia.charactersudoku.presentation.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
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

    fun deleteCharacterFromDict(chineseCharacter: ChineseCharacter) {
        viewModelScope.launch {
            deleteCharacter(chineseCharacter)
        }
    }

    fun addOrEditCharacter(chineseCharacter: ChineseCharacter) {
        viewModelScope.launch {
            addCharacterToDict(chineseCharacter)
        }
    }

    fun changeIsChosenState(chineseCharacter: ChineseCharacter) {
        val newChChar = ChineseCharacter(
            chineseCharacter.character,
            chineseCharacter.pinyin,
            chineseCharacter.translation,
            chineseCharacter.usages,
            chineseCharacter.timesPlayed,
            !chineseCharacter.isChosen,
        )
        viewModelScope.launch {
            addCharacterToDict(newChChar)
        }
    }
}