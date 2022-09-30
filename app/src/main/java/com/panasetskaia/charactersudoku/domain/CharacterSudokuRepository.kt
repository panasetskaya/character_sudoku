package com.panasetskaia.charactersudoku.domain

import androidx.lifecycle.LiveData
import com.panasetskaia.charactersudoku.domain.entities.Board
import com.panasetskaia.charactersudoku.domain.entities.ChineseCharacter

interface CharacterSudokuRepository {

    suspend fun getNineRandomCharFromDict(): List<String>

    suspend fun addOrEditCharToDict(character: ChineseCharacter)

    suspend fun deleteCharFromDict(character: ChineseCharacter)

    fun searchForCharacter(character: String): LiveData<List<ChineseCharacter>>

    fun getWholeDictionary(): LiveData<List<ChineseCharacter>>


    fun getNewGame(nineCharacters: List<ChineseCharacter>): Board

    fun saveGame(board: Board)

    fun getSavedGame(): Board


    suspend fun getGameResult(board: Board): GameResult

}

//todo: LiveData Переписать на Flow