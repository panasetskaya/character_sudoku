package com.panasetskaia.charactersudoku.domain.usecases

import com.panasetskaia.charactersudoku.domain.CharacterSudokuRepository
import com.panasetskaia.charactersudoku.domain.entities.Board
import javax.inject.Inject

class GetRandomBoard @Inject constructor(private val repository: CharacterSudokuRepository) {

    suspend operator fun invoke(): Board {
        return repository.getRandomBoard()
    }

}