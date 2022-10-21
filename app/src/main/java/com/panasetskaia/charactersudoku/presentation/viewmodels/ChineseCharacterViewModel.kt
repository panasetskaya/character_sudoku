package com.panasetskaia.charactersudoku.presentation.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
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
    private val deleteCategory: DeleteCategoryUseCase
) : AndroidViewModel(application) {

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

    fun getOneCharacterById(id: Int): SharedFlow<ChineseCharacter> {
        return dictionaryFlow.map { wholeDictionary ->
            var characterWeNeed = ChineseCharacter(
                character = "",
                pinyin = "",
                translation = "",
                usages = "")
            for (i in wholeDictionary) {
                if (i.id==id) {
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
}

//todo: первую строку рэндомно - предустановка в датабазу
//todo: удаление категории по уму - очистка категории на дефолтную у всех иерогов
//todo: и добавление категории с проверкой дублирования!
//todo: кнопку выбора фильтра с переходом на фрагмент (и кнопку сброса фильтра, видмую, если выбрана категория)
//todo: запуск рэндома с категории (если слов не хватает??? тост-уведомление или добавить цифры?)

