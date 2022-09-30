package com.panasetskaia.charactersudoku.data.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ChineseCharacterDao {

    @Query("SELECT * FROM chinesecharacterdb ORDER BY RANDOM() LIMIT 9")
    suspend fun getNineRandomCharacters(): List<ChineseCharacterDb>

    @Query("SELECT * FROM chinesecharacterdb")
    fun getWholeDictionary(): LiveData<List<ChineseCharacterDb>>

    @Query("DELETE FROM chinesecharacterdb WHERE id=:characterId")
    suspend fun deleteCharFromDict(characterId: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addOrEditCharacter(character: ChineseCharacterDb)

    @Query("SELECT * FROM chinesecharacterdb WHERE character LIKE :characterQuery")
    fun searchForCharacter(characterQuery: String): LiveData<List<ChineseCharacterDb>>

}
