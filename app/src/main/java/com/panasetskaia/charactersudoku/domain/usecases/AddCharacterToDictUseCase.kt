package com.panasetskaia.charactersudoku.domain.usecases

import com.panasetskaia.charactersudoku.domain.CharacterSudokuRepository
import com.panasetskaia.charactersudoku.domain.ChineseCharacter

class AddCharacterToDictUseCase(private val repository: CharacterSudokuRepository) {

    operator fun invoke(character: ChineseCharacter){
        repository.addCharToDict(character)
    }

}