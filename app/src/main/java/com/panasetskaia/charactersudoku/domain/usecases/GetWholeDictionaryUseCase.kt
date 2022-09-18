package com.panasetskaia.charactersudoku.domain.usecases

import com.panasetskaia.charactersudoku.domain.CharacterSudokuRepository
import com.panasetskaia.charactersudoku.domain.ChineseCharacter

class GetWholeDictionaryUseCase(private val repository: CharacterSudokuRepository) {
    operator fun invoke(): List<ChineseCharacter> {
        return repository.getWholeDictionary()
    }
}