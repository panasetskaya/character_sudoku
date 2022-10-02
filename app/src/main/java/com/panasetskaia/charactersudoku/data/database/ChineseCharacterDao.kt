package com.panasetskaia.charactersudoku.data.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ChineseCharacterDao {

    @Query("SELECT character FROM chinesecharacterdb")
    suspend fun getAllChinese(): List<String>?

    @Query("SELECT * FROM chinesecharacterdb")
    fun getWholeDictionary(): LiveData<List<ChineseCharacterDb>>

    @Query("DELETE FROM chinesecharacterdb WHERE character=:characterStr")
    suspend fun deleteCharFromDict(characterStr: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addOrEditCharacter(character: ChineseCharacterDb)

    @Query("SELECT * FROM chinesecharacterdb WHERE character LIKE :characterQuery")
    fun searchForCharacter(characterQuery: String): LiveData<List<ChineseCharacterDb>>

    @Query("SELECT * FROM chinesecharacterdb WHERE character=:characterStr")
    suspend fun getCharacterByChinese(characterStr: String): ChineseCharacterDb

    @Query("SELECT character FROM chinesecharacterdb WHERE isChosen=1")
    suspend fun getSelected(): List<String>?
    // использовать для получения списка иероглифов

}
