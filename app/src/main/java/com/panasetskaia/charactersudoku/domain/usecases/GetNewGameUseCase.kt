package com.panasetskaia.charactersudoku.domain.usecases

import com.panasetskaia.charactersudoku.domain.CharacterSudokuRepository
import com.panasetskaia.charactersudoku.domain.entities.Board
import com.panasetskaia.charactersudoku.domain.entities.ChineseCharacter

class GetNewGameUseCase(private val repository: CharacterSudokuRepository) {

    operator fun invoke(nineCharacters: List<ChineseCharacter>): Board {
        return repository.getNewGame(nineCharacters)
    }
}