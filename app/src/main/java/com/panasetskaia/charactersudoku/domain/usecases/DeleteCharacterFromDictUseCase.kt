package com.panasetskaia.charactersudoku.domain.usecases

import com.panasetskaia.charactersudoku.domain.CharacterSudokuRepository
import com.panasetskaia.charactersudoku.domain.ChineseCharacter

class DeleteCharacterFromDictUseCase(private val repository: CharacterSudokuRepository) {

    operator fun invoke(character: ChineseCharacter) {
        repository.deleteCharFromDict(character)
    }
}