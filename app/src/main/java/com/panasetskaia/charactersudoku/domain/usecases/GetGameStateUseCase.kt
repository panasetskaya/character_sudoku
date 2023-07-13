package com.panasetskaia.charactersudoku.domain.usecases

import com.panasetskaia.charactersudoku.domain.CharacterSudokuRepository
import com.panasetskaia.charactersudoku.domain.entities.GameState
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetGameStateUseCase @Inject constructor(private val repository: CharacterSudokuRepository) {
    suspend operator fun invoke(): Flow<GameState> {
        return repository.getGameState()
    }
}