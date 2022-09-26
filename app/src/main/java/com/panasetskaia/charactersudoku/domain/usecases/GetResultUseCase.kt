package com.panasetskaia.charactersudoku.domain.usecases

import com.panasetskaia.charactersudoku.domain.CharacterSudokuRepository
import com.panasetskaia.charactersudoku.domain.GameResult
import com.panasetskaia.charactersudoku.domain.entities.Board

class GetResultUseCase(private val repository: CharacterSudokuRepository) {

    suspend operator fun invoke(board: Board): GameResult {
        return repository.getGameResult(board)
    }

}