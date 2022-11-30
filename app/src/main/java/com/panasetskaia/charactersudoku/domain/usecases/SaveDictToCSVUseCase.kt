package com.panasetskaia.charactersudoku.domain.usecases

import com.panasetskaia.charactersudoku.domain.CharacterSudokuRepository
import javax.inject.Inject

class SaveDictToCSVUseCase @Inject constructor(private val repository: CharacterSudokuRepository) {

    suspend operator fun invoke() {
        repository.saveDictToCSV()
    }
}