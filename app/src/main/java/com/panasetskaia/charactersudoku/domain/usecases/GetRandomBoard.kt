package com.panasetskaia.charactersudoku.domain.usecases

import com.panasetskaia.charactersudoku.domain.CharacterSudokuRepository
import com.panasetskaia.charactersudoku.domain.entities.Board
import com.panasetskaia.charactersudoku.domain.entities.Level
import javax.inject.Inject

class GetRandomBoard @Inject constructor(private val repository: CharacterSudokuRepository) {

    suspend operator fun invoke(diffLevel: Level): Board {
        return repository.getRandomBoard(diffLevel)
    }

}