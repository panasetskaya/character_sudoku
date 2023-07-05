package com.panasetskaia.charactersudoku.presentation.dict_screen

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.panasetskaia.charactersudoku.domain.entities.Category
import com.panasetskaia.charactersudoku.domain.entities.ChineseCharacter
import com.panasetskaia.charactersudoku.domain.usecases.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.launch
import javax.inject.Inject

class ChineseCharacterViewModel @Inject constructor(
    application: Application,
    private val addCharacterToDict: AddOrEditCharacterUseCase,
    private val deleteCharacter: DeleteCharacterFromDictUseCase,
    private val getWholeDict: GetWholeDictionaryUseCase,
    private val getAllCategories: GetAllCategoriesUseCase,
    private val addCategory: AddCategoryUseCase,
    private val deleteCategory: DeleteCategoryUseCase,
    private val saveDictToCSV: SaveDictToCSVUseCase,
    private val saveDictToJson: SaveDictToJsonUseCase,
) : AndroidViewModel(application) {

    private lateinit var selected: List<ChineseCharacter>

    private val _pathLiveData = MutableLiveData<String>()
    val pathLiveData: LiveData<String>
        get() = _pathLiveData

    private val _dictionaryFlow = MutableSharedFlow<List<ChineseCharacter>>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val dictionaryFlow: SharedFlow<List<ChineseCharacter>>
        get() = _dictionaryFlow

    private val _categoriesFlow = MutableSharedFlow<List<Category>>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val categoriesFlow: SharedFlow<List<Category>>
        get() = _categoriesFlow

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

    private fun updateCategories() {
        viewModelScope.launch {
            _categoriesFlow.emitAll(
                getAllCategories()
            )
        }
    }

    init {
        updateDictionary()
        updateCategories()
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

    fun addNewCategory(categoryName: String) {
        viewModelScope.launch {
            addCategory(Category(categoryName = categoryName))
            _categoriesFlow.emitAll(
                getAllCategories()
            )
            _dictionaryFlow.emitAll(
                getWholeDict()
            )
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

    fun getDictionaryByCategory(filter: String): SharedFlow<List<ChineseCharacter>> {
        return dictionaryFlow.map { wholeDictionary ->
            val selectedCharacters = mutableListOf<ChineseCharacter>()
            for (i in wholeDictionary) {
                if (i.category==filter) {
                    selectedCharacters.add(i)
                }
            }
            selectedCharacters.toList()
        }.shareIn(viewModelScope, WhileSubscribed(5000), replay = 1)
    }

    fun getOneCharacterById(id: Int): SharedFlow<ChineseCharacter> {
        return dictionaryFlow.map { wholeDictionary ->
            var characterWeNeed = ChineseCharacter(
                character = "",
                pinyin = "",
                translation = "",
                usages = "",
                category = "-"
            )
            for (i in wholeDictionary) {
                if (i.id == id) {
                    characterWeNeed = i
                }
            }
            characterWeNeed
        }.shareIn(viewModelScope, WhileSubscribed(5000), replay = 1)
    }

    fun getOneCharacterByChinese(chinese: String): SharedFlow<ChineseCharacter> {
        return dictionaryFlow.map { wholeDictionary ->
            var characterWeNeed = ChineseCharacter(
                character = "",
                pinyin = "",
                translation = "",
                usages = "",
                category = "-"
            )
            for (i in wholeDictionary) {
                if (i.character == chinese) {
                    characterWeNeed = i
                }
            }
            characterWeNeed
        }.shareIn(viewModelScope, WhileSubscribed(5000), replay = 1)
    }

    fun deleteThisCategory(category: String) {
        viewModelScope.launch {
            deleteCategory(category)
        }
    }

    fun deleteSelected() {
        viewModelScope.launch {
            for (i in selected) {
                deleteCharacter(i.id)
            }
        }
        finishDeleting(true)
    }

    fun setSelectedForDeleting(newSelected: List<ChineseCharacter>) {
        selected = newSelected
    }

    fun saveDictionaryToCSV() {
        viewModelScope.launch {
            val path = saveDictToCSV()
            Log.d("MYMYMY", "path is $path")
            _pathLiveData.postValue(path)
        }
    }

    fun saveDictionaryToJson() {
        viewModelScope.launch {
            val path = saveDictToJson()
            Log.d("MYMYMY", "path is $path")
            _pathLiveData.postValue(path)
        }
    }

    fun parseExternalDict(newDict: List<ChineseCharacter>) {
        viewModelScope.launch {
            for (i in newDict) {
                addCharacterToDict(i.copy(id=0))
            }
        }
    }
}
