package com.panasetskaia.charactersudoku.domain

class AddCharacterToDictUseCase(private val repository: CharacterSudokuRepository) {

    operator fun invoke(character: ChineseCharacter){
        repository.addCharToDict(character)
    }

}