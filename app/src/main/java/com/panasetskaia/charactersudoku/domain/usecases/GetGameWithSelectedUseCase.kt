package com.panasetskaia.charactersudoku.domain.usecases

import com.panasetskaia.charactersudoku.domain.CharacterSudokuRepository
import com.panasetskaia.charactersudoku.domain.entities.Board
import com.panasetskaia.charactersudoku.domain.entities.Level
import javax.inject.Inject

class GetGameWithSelectedUseCase @Inject constructor(private val repository: CharacterSudokuRepository) {
    suspend operator fun invoke(): Board {
        return repository.getGameWithSelected()
    }
}