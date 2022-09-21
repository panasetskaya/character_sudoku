package com.panasetskaia.charactersudoku.domain.usecases

import com.panasetskaia.charactersudoku.domain.CharacterSudokuRepository
import com.panasetskaia.charactersudoku.domain.entities.ChineseCharacter

class GetNineRandomCharFromDictUseCase(private val repository: CharacterSudokuRepository) {

    operator fun invoke(): List<String>{
        return repository.getNineRandomCharFromDict()
    }

}