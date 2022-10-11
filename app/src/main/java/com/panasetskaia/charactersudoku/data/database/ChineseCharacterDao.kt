package com.panasetskaia.charactersudoku.data.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ChineseCharacterDao {

    @Query("SELECT character FROM chinesecharacterdbmodel")
    suspend fun getAllChinese(): List<String>?

    @Query("SELECT * FROM chinesecharacterdbmodel")
    fun getWholeDictionary(): LiveData<List<ChineseCharacterDbModel>>

    @Query("DELETE FROM chinesecharacterdbmodel WHERE id=:characterId")
    suspend fun deleteCharFromDict(characterId: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addOrEditCharacter(characterModel: ChineseCharacterDbModel)

    @Query("SELECT * FROM chinesecharacterdbmodel WHERE character LIKE :characterQuery")
    fun searchForCharacter(characterQuery: String): LiveData<List<ChineseCharacterDbModel>>

    @Query("SELECT * FROM chinesecharacterdbmodel WHERE character=:characterStr")
    suspend fun getCharacterByChinese(characterStr: String): ChineseCharacterDbModel?

    @Query("SELECT character FROM chinesecharacterdbmodel WHERE isChosen=1")
    suspend fun getSelected(): List<String>?
    // использовать для получения списка иероглифов

}
