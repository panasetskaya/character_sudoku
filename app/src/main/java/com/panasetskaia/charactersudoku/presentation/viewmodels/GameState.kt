package com.panasetskaia.charactersudoku.presentation.viewmodels
import com.panasetskaia.charactersudoku.domain.entities.Board

sealed class GameState

object WIN: GameState()

object REFRESHING: GameState()

object SETTING: GameState()

class PLAYING(board: Board): GameState() {
    val currentBoard = board
}

class DISPLAY(board: Board): GameState() {
    val oldBoard = board
}