package com.panasetskaia.charactersudoku.domain.usecases

import com.panasetskaia.charactersudoku.domain.CharacterSudokuRepository
import com.panasetskaia.charactersudoku.domain.entities.Board
import javax.inject.Inject

class SaveGameUseCase @Inject constructor(private val repository: CharacterSudokuRepository) {

    suspend operator fun invoke(board: Board) {
        repository.saveGame(board)
    }
}