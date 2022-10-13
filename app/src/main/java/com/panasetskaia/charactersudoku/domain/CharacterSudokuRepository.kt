package com.panasetskaia.charactersudoku.domain

import androidx.lifecycle.LiveData
import com.panasetskaia.charactersudoku.domain.entities.Board
import com.panasetskaia.charactersudoku.domain.entities.ChineseCharacter

interface CharacterSudokuRepository {

    suspend fun getRandomBoard(): Board

    suspend fun addOrEditCharToDict(character: ChineseCharacter)

    suspend fun deleteCharFromDict(characterId: Int)

    fun getWholeDictionary(): LiveData<List<ChineseCharacter>>

    suspend fun getNewGame(nineCharacters: List<ChineseCharacter>): Board

    suspend fun saveGame(board: Board)

    suspend fun getSavedGame(): Board?

    suspend fun getGameResult(board: Board): GameResult

}

//todo: LiveData Переписать на Flow