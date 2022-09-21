package com.panasetskaia.charactersudoku.domain

import com.panasetskaia.charactersudoku.domain.entities.Board
import com.panasetskaia.charactersudoku.domain.entities.ChineseCharacter

interface CharacterSudokuRepository {

    fun getNineRandomCharFromDict(): List<String>

    fun addCharToDict(character: ChineseCharacter)

    fun deleteCharFromDict(character: ChineseCharacter)

    fun editCharinDict(character: ChineseCharacter)

    fun searchForCharacter(character: String): ChineseCharacter

    fun getWholeDictionary(): List<ChineseCharacter>

    fun getNewGame(nineCharacters: List<ChineseCharacter>): Board

    fun saveGame(board: Board)

    fun getSavedGame(): Board

    suspend fun getSolution(gridString: String): Board?

}