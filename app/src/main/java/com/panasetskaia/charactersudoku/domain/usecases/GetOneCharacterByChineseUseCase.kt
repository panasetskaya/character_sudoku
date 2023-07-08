package com.panasetskaia.charactersudoku.domain.usecases

import com.panasetskaia.charactersudoku.domain.CharacterSudokuRepository
import com.panasetskaia.charactersudoku.domain.entities.ChineseCharacter
import javax.inject.Inject

class GetOneCharacterByChineseUseCase @Inject constructor(private val repository: CharacterSudokuRepository) {
    suspend operator fun invoke(chinese: String): ChineseCharacter? {
        return repository.getCharacterByChinese(chinese)
    }
}