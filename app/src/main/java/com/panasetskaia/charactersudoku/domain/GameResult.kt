package com.panasetskaia.charactersudoku.domain

import com.panasetskaia.charactersudoku.domain.entities.Board

sealed class GameResult

class SUCCESS (private val solutionBoard: Board): GameResult() {
    val solution = solutionBoard
}
class FAILED: GameResult()