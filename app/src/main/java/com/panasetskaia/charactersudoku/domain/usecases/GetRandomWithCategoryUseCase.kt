package com.panasetskaia.charactersudoku.domain.usecases

import com.panasetskaia.charactersudoku.domain.CharacterSudokuRepository
import com.panasetskaia.charactersudoku.domain.entities.Board
import com.panasetskaia.charactersudoku.domain.entities.Level
import javax.inject.Inject

class GetRandomWithCategoryUseCase @Inject constructor(private val repository: CharacterSudokuRepository) {
    suspend operator fun invoke(category: String, diffLevel: Level): Board {
        return repository.getRandomWithCategory(category, diffLevel)
    }
}