package com.panasetskaia.charactersudoku.data.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import com.panasetskaia.charactersudoku.domain.entities.ChineseCharacter

@Dao
interface ChineseCharacterDao {

    suspend fun howManyCharacters() //todo: Int? Сразу рэндом 9 штук?

    fun getWholeDictionary(): LiveData<List<ChineseCharacter>>  /// todo:

    suspend fun deleteCharFromDict() //todo:

    suspend fun editCharinDict() ///todo:

    suspend fun addCharToDict(character: ChineseCharacter) /// todo:

    suspend fun searchForCharacter(character: String): ChineseCharacter?   /// todo:

}
