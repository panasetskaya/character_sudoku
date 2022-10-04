package com.panasetskaia.charactersudoku.domain.usecases

import com.panasetskaia.charactersudoku.domain.CharacterSudokuRepository
import com.panasetskaia.charactersudoku.domain.entities.ChineseCharacter

class AddOrEditCharacterUseCase(private val repository: CharacterSudokuRepository) {

    suspend operator fun invoke(character: ChineseCharacter){
        repository.addOrEditCharToDict(character)
    }
}