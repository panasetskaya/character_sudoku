package com.panasetskaia.charactersudoku.domain.usecases

import com.panasetskaia.charactersudoku.domain.CharacterSudokuRepository
import com.panasetskaia.charactersudoku.domain.entities.Category
import javax.inject.Inject

class AddCategoryUseCase @Inject constructor(private val repository: CharacterSudokuRepository) {
    suspend operator fun invoke(category: Category) {
        repository.addCategory(category)
    }
}