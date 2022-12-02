package com.panasetskaia.charactersudoku.domain.usecases

import com.panasetskaia.charactersudoku.domain.CharacterSudokuRepository
import javax.inject.Inject

class SaveDictToJsonUseCase @Inject constructor(private val repository: CharacterSudokuRepository) {

    suspend operator fun invoke(): String {
        return repository.saveDictToJson()
    }

}