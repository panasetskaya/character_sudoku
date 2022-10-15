package com.panasetskaia.charactersudoku.domain.usecases

import com.panasetskaia.charactersudoku.domain.CharacterSudokuRepository
import com.panasetskaia.charactersudoku.domain.entities.ChineseCharacter
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetWholeDictionaryUseCase @Inject constructor(private val repository: CharacterSudokuRepository) {
    operator fun invoke(): Flow<List<ChineseCharacter>> {
        return repository.getWholeDictionary()
    }
}