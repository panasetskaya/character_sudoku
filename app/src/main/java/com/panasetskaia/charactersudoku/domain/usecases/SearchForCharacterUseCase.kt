package com.panasetskaia.charactersudoku.domain.usecases

import androidx.lifecycle.LiveData
import com.panasetskaia.charactersudoku.domain.CharacterSudokuRepository
import com.panasetskaia.charactersudoku.domain.entities.ChineseCharacter

class SearchForCharacterUseCase(private val repository: CharacterSudokuRepository) {

    operator fun invoke(character: String): LiveData<List<ChineseCharacter>> {
        return repository.searchForCharacter(character)
    }
}