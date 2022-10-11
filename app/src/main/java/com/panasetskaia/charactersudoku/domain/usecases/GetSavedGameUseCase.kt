package com.panasetskaia.charactersudoku.domain.usecases

import com.panasetskaia.charactersudoku.domain.CharacterSudokuRepository
import com.panasetskaia.charactersudoku.domain.entities.Board

class GetSavedGameUseCase(private val repository: CharacterSudokuRepository) {

    suspend operator  fun invoke(): Board? {
        return repository.getSavedGame()
    }
}