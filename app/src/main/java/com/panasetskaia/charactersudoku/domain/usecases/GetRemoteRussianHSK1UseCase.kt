package com.panasetskaia.charactersudoku.domain.usecases

import com.panasetskaia.charactersudoku.domain.CharacterSudokuRepository
import javax.inject.Inject

class GetRemoteRussianHSK1UseCase @Inject constructor(private val repository: CharacterSudokuRepository) {

    operator fun invoke() {
        repository.getRemoteRussianHSK1Dict()
    }
}