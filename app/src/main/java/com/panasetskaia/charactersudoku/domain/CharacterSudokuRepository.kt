package com.panasetskaia.charactersudoku.domain

import com.panasetskaia.charactersudoku.domain.entities.Board
import com.panasetskaia.charactersudoku.domain.entities.Category
import com.panasetskaia.charactersudoku.domain.entities.ChineseCharacter
import kotlinx.coroutines.flow.Flow

interface CharacterSudokuRepository {

    suspend fun getRandomBoard(): Board

    suspend fun getRandomWithCategory(category: String): Board

    suspend fun addOrEditCharToDict(character: ChineseCharacter)

    suspend fun deleteCharFromDict(characterId: Int)

    fun getWholeDictionary(): Flow<List<ChineseCharacter>>

    suspend fun getNewGame(nineCharacters: List<ChineseCharacter>): Board

    suspend fun saveGame(board: Board)

    suspend fun getSavedGame(): Board?

    suspend fun getGameResult(board: Board): GameResult

    fun getAllCategories(): Flow<List<Category>>

    suspend fun deleteCategory(catName:String)

    suspend fun addCategory(category: Category)

}