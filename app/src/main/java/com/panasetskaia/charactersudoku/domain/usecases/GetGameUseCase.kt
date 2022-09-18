package com.panasetskaia.charactersudoku.domain.usecases

import com.panasetskaia.charactersudoku.domain.CharacterSudokuRepository

class GetGameUseCase(private val repository: CharacterSudokuRepository) {

    operator fun invoke(): Map<String, String> {
        return repository.getGame()
    }
}