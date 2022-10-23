package com.panasetskaia.charactersudoku.domain.usecases

import com.panasetskaia.charactersudoku.domain.CharacterSudokuRepository
import javax.inject.Inject

class DeleteCategoryUseCase @Inject constructor(private val repository: CharacterSudokuRepository) {

    suspend operator fun invoke(catName:String) {
        repository.deleteCategory(catName)
    }
}