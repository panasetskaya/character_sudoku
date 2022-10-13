package com.panasetskaia.charactersudoku.domain.usecases

import androidx.lifecycle.LiveData
import com.panasetskaia.charactersudoku.domain.CharacterSudokuRepository
import com.panasetskaia.charactersudoku.domain.entities.ChineseCharacter
import javax.inject.Inject

class GetWholeDictionaryUseCase @Inject constructor(private val repository: CharacterSudokuRepository) {
    operator fun invoke(): LiveData<List<ChineseCharacter>> {
        return repository.getWholeDictionary()
    }
}