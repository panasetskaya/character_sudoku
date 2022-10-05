package com.panasetskaia.charactersudoku.domain.usecases

import com.panasetskaia.charactersudoku.domain.CharacterSudokuRepository
import com.panasetskaia.charactersudoku.domain.entities.ChineseCharacter

class DeleteCharacterFromDictUseCase(private val repository: CharacterSudokuRepository) {

    suspend operator fun invoke(characterId: Int) {
        repository.deleteCharFromDict(characterId)
    }
}