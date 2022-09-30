package com.panasetskaia.charactersudoku.domain.usecases

import com.panasetskaia.charactersudoku.domain.CharacterSudokuRepository

class GetNineRandomCharFromDictUseCase(private val repository: CharacterSudokuRepository) {

    suspend operator fun invoke(): List<String> {
        return repository.getNineRandomCharFromDict()
    }

}