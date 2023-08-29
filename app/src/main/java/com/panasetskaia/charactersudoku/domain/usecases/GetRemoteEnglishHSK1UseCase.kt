package com.panasetskaia.charactersudoku.domain.usecases

import com.panasetskaia.charactersudoku.domain.CharacterSudokuRepository
import javax.inject.Inject

class GetRemoteEnglishHSK1UseCase @Inject constructor(private val repository: CharacterSudokuRepository) {
    operator fun invoke() {
        repository.getRemoteEnglishHSK1Dict()
    }
}