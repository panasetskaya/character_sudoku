package com.panasetskaia.charactersudoku.domain.usecases

import com.panasetskaia.charactersudoku.domain.CharacterSudokuRepository
import com.panasetskaia.charactersudoku.domain.entities.Board

class GetSolutionUseCase(private val repository: CharacterSudokuRepository) {
    operator suspend fun invoke(gridString: String): Board?{
        return repository.getSolution(gridString)
    }
}