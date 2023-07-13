package com.panasetskaia.charactersudoku.presentation.dict_screen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.panasetskaia.charactersudoku.R
import com.panasetskaia.charactersudoku.domain.entities.Category
import com.panasetskaia.charactersudoku.domain.entities.ChineseCharacter
import com.panasetskaia.charactersudoku.domain.usecases.*
import com.panasetskaia.charactersudoku.presentation.base.BaseViewModel
import com.panasetskaia.charactersudoku.utils.myLog
import com.panasetskaia.charactersudoku.utils.simplifyPinyin
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.launch
import javax.inject.Inject

class ChineseCharacterViewModel @Inject constructor(
    private val addCharacterToDict: AddOrEditCharacterUseCase,
    private val deleteCharacter: DeleteCharacterFromDictUseCase,
    private val getWholeDict: GetWholeDictionaryUseCase,
    private val getAllCategories: GetAllCategoriesUseCase,
    private val addCategory: AddCategoryUseCase,
    private val deleteCategory: DeleteCategoryUseCase,
    private val saveDictToCSV: SaveDictToCSVUseCase,
    private val saveDictToJson: SaveDictToJsonUseCase,
) : BaseViewModel() {

    private var innerDictCache = listOf<ChineseCharacter>()

    private var selectedCache = listOf<ChineseCharacter>()

    private val toastEventChannel = Channel<Int>(Channel.BUFFERED)
    val toastFlow: Flow<Int>
        get() = toastEventChannel.receiveAsFlow()

    private val _pathLiveData = MutableLiveData<String>()
    val pathLiveData: LiveData<String>
        get() = _pathLiveData

    private val _dictionaryFlow = MutableSharedFlow<List<ChineseCharacter>>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val dictionaryFlow: SharedFlow<List<ChineseCharacter>>
        get() = _dictionaryFlow

    private val _categoriesFlow = MutableSharedFlow<List<String>>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val categoriesFlow: SharedFlow<List<String>>
        get() = _categoriesFlow

    val selectedCharactersSharedFlow = dictionaryFlow.map { wholeDictionary ->
        val selectedCharacters = mutableListOf<ChineseCharacter>()
        for (i in wholeDictionary) {
            if (i.isChosen) {
                selectedCharacters.add(i)
            }
        }
        selectedCache = selectedCharacters
        selectedCache
    }.shareIn(viewModelScope, WhileSubscribed(5000), replay = 1)

    private fun updateDictionary() {
        viewModelScope.launch {
            getWholeDict().collectLatest {
                innerDictCache = it
                _dictionaryFlow.emit(it)
            }
        }
    }

    fun removeFIlters() {
        updateDictionary()
    }

    private fun updateCategories() {
        viewModelScope.launch {
            getAllCategories().collectLatest {
                val listOfCategories = mutableListOf<String>()
                for (i in it) {
                    listOfCategories.add(i.categoryName)
                }
                _categoriesFlow.emit(listOfCategories)
            }

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
    }

    fun addOrEditCharacter(chineseCharacter: ChineseCharacter) {
        viewModelScope.launch {
            addCharacterToDict(chineseCharacter)
        }
    }

    fun addNewCategory(categoryName: String) {
        viewModelScope.launch {
            addCategory(Category(categoryName = categoryName))
            updateCategories()
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

    fun goToSingleCharacterFragment(id: Int?) {
        if (id==null) {
            navigate(DictionaryFragmentDirections.actionDictionaryFragmentToSingleCharacterFragment(SingleCharacterFragment.MODE_ADD, SingleCharacterFragment.NEW_CHAR_ID))
        } else {
            navigate(DictionaryFragmentDirections.actionDictionaryFragmentToSingleCharacterFragment(SingleCharacterFragment.MODE_EDIT, id))
        }
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

    override fun deleteThisCategory(category: String) {
        viewModelScope.launch {
            deleteCategory(category)
        }
    }

    fun deleteSelected() {
        viewModelScope.launch {
            for (i in selectedCache) {
                deleteCharacter(i.id)
            }
        }
    }

    fun setSelectedForDeleting(newSelected: List<ChineseCharacter>) {
        selectedCache = newSelected
    }

    fun saveDictionaryToCSV() {
        viewModelScope.launch {
            val path = saveDictToCSV()
            myLog("path is $path")
            _pathLiveData.postValue(path)
        }
    }

    fun saveDictionaryToJson() {
        viewModelScope.launch {
            val path = saveDictToJson()
            myLog("path is $path")
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

    fun filterByQuery(query: String) {
        val thereIs = innerDictCache.any { it.character == query || it.pinyin.contains(query)}
        if (thereIs) {
            val newList = innerDictCache.filter {
                it.character==query || it.pinyin.simplifyPinyin().contains(query)
            }
            _dictionaryFlow.tryEmit(newList)
        } else {
            toastEventChannel.trySendBlocking(R.string.not_found)
        }
    }

    fun showByCategory(selectedCategory: String?) {
        if (selectedCategory==null) {
            removeFIlters()
        } else {
            val newList = innerDictCache.filter { it.category==selectedCategory }
            _dictionaryFlow.tryEmit(newList)
        }
    }

    fun startGameWithSelected(lvl: Int) {
        navigate(DictionaryFragmentDirections.actionDictionaryFragmentToGameFragment(lvl))
    }
}
