package com.panasetskaia.charactersudoku.domain

import com.panasetskaia.charactersudoku.domain.entities.Board

sealed class GameResult

class SUCCESS (solutionBoard: Board): GameResult() {
    val solution = solutionBoard
}

object FAILED : GameResult()