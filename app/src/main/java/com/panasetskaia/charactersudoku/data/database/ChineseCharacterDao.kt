package com.panasetskaia.charactersudoku.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ChineseCharacterDao {

    @Query("SELECT character FROM chinesecharacterdbmodel")
    suspend fun getAllChineseAsList(): List<String>

    @Query("SELECT * FROM chinesecharacterdbmodel")
    fun getWholeDictionary(): Flow<List<ChineseCharacterDbModel>>

    @Query("DELETE FROM chinesecharacterdbmodel WHERE id=:characterId")
    suspend fun deleteCharFromDict(characterId: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addOrEditCharacter(characterModel: ChineseCharacterDbModel)

    @Query("SELECT * FROM chinesecharacterdbmodel WHERE character=:characterStr")
    suspend fun getCharacterByChinese(characterStr: String): ChineseCharacterDbModel?

}
