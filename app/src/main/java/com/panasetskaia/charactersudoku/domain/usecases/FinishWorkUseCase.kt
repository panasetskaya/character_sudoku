package com.panasetskaia.charactersudoku.domain.usecases

import com.panasetskaia.charactersudoku.domain.CharacterSudokuRepository
import javax.inject.Inject

class FinishWorkUseCase @Inject constructor(private val repository: CharacterSudokuRepository) {

    operator fun invoke() {
        repository.finish()
    }

}