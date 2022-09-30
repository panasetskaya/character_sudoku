package com.panasetskaia.charactersudoku.domain.usecases

import androidx.lifecycle.LiveData
import com.panasetskaia.charactersudoku.domain.CharacterSudokuRepository
import com.panasetskaia.charactersudoku.domain.entities.ChineseCharacter

class GetWholeDictionaryUseCase(private val repository: CharacterSudokuRepository) {
    operator fun invoke(): LiveData<List<ChineseCharacter>> {
        return repository.getWholeDictionary()
    }
}