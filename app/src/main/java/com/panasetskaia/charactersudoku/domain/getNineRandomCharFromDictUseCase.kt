package com.panasetskaia.charactersudoku.domain

class getNineRandomCharFromDictUseCase(private val repository: CharacterSudokuRepository) {

    operator fun invoke(){
        repository.getNineRandomCharFromDict()
    }

}