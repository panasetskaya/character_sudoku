package com.panasetskaia.charactersudoku.domain.usecases

import com.panasetskaia.charactersudoku.domain.CharacterSudokuRepository
import com.panasetskaia.charactersudoku.domain.entities.Board

class SaveGameUseCase(private val repository: CharacterSudokuRepository) {

    operator fun invoke(board: Board) {
        repository.saveGame(board)
    }
}