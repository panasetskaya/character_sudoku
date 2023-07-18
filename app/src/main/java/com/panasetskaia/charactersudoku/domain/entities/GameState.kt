package com.panasetskaia.charactersudoku.domain.entities

sealed class GameState

object WIN: GameState()

object REFRESHING: GameState()

class PLAYING(board: Board): GameState() {
    val currentBoard = board
}

class DISPLAY(board: Board): GameState() {
    val oldBoard = board
}