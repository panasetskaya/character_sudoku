package com.panasetskaia.charactersudoku.domain.usecases

import com.panasetskaia.charactersudoku.domain.CharacterSudokuRepository
import com.panasetskaia.charactersudoku.domain.entities.Level
import javax.inject.Inject

class SetLevelUseCase @Inject constructor(private val repo: CharacterSudokuRepository) {
    suspend operator fun invoke(lvl: Level) {
        repo.selLevel(lvl)
    }
}