package com.panasetskaia.charactersudoku.domain.usecases

import com.panasetskaia.charactersudoku.domain.CharacterSudokuRepository
import com.panasetskaia.charactersudoku.domain.entities.Board
import com.panasetskaia.charactersudoku.domain.entities.ChineseCharacter
import javax.inject.Inject

class GetNewGameUseCase @Inject constructor(private val repository: CharacterSudokuRepository) {

    suspend operator  fun invoke(nineCharacters: List<ChineseCharacter>): Board {
        return repository.getNewGame(nineCharacters)
    }
}