package com.panasetskaia.charactersudoku.domain.usecases

import com.panasetskaia.charactersudoku.domain.CharacterSudokuRepository
import javax.inject.Inject

class GetStringUseCase @Inject constructor(private val repo: CharacterSudokuRepository) {

    operator fun invoke(resId: Int): String {
        return repo.getStringResource(resId)
    }
 }