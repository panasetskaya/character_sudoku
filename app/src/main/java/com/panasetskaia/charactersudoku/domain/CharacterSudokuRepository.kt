package com.panasetskaia.charactersudoku.domain

import com.panasetskaia.charactersudoku.domain.entities.*
import kotlinx.coroutines.flow.Flow

interface CharacterSudokuRepository {

    /**
     * Common:
     */
    fun getStringResource(resId: Int): String


    /**
     * Game functions:
     */
    suspend fun getRandomBoard(diffLevel: Level)
    suspend fun getRandomWithCategory(category: String, diffLevel: Level)
    suspend fun getSavedGame()
    suspend fun getGameWithSelected(diffLevel: Level)
    suspend fun saveGame(board: Board)
    suspend fun getGameResult(board: Board): GameResult
    suspend fun getGameState(): Flow<GameState>

    /**
     * Records (top results) functions:
     */
    suspend fun getAllRecords(): List<Record>
    suspend fun supplyNewRecord(record: Record)

    /**
     * Dictionary characters functions:
     */
    suspend fun addOrEditCharToDict(character: ChineseCharacter)
    suspend fun deleteCharFromDict(characterId: Int)
    fun getWholeDictionary(): Flow<List<ChineseCharacter>>

    /**
     * Dictionary categories functions:
     */
    fun getAllCategories(): Flow<List<Category>>
    suspend fun deleteCategory(catName: String)
    suspend fun addCategory(category: Category)

    /**
     * Export and import functions:
     */
    suspend fun saveDictToCSV(): String
    suspend fun saveDictToJson(): String
    suspend fun getCharacterByChinese(chinese: String): ChineseCharacter?

}