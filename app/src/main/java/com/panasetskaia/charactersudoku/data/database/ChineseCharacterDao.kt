package com.panasetskaia.charactersudoku.data.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ChineseCharacterDao {

    @Query("SELECT id FROM chinesecharacterdb")
    suspend fun getAllIds(): List<Int>?

    @Query("SELECT * FROM chinesecharacterdb")
    fun getWholeDictionary(): LiveData<List<ChineseCharacterDb>>

    @Query("DELETE FROM chinesecharacterdb WHERE id=:characterId")
    suspend fun deleteCharFromDict(characterId: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addOrEditCharacter(character: ChineseCharacterDb)

    @Query("SELECT * FROM chinesecharacterdb WHERE character LIKE :characterQuery")
    fun searchForCharacter(characterQuery: String): LiveData<List<ChineseCharacterDb>>

    @Query("SELECT * FROM chinesecharacterdb WHERE id=:characterId")
    suspend fun getCharacterById(characterId: Int): ChineseCharacterDb

    @Query("SELECT character FROM chinesecharacterdb WHERE isChosen=1")
    suspend fun getSelected(): List<String>?
    // использовать для получения списка иероглифов

}
