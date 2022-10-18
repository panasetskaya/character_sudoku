package com.panasetskaia.charactersudoku.domain.entities

sealed class GameResult

class SUCCESS (solutionBoard: Board): GameResult() {
    val solution = solutionBoard
}

object FAILED : GameResult()