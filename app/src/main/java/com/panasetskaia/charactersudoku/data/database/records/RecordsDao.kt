package com.panasetskaia.charactersudoku.data.database.records

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RecordsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveNewRecord(recordDbModel: RecordDbModel)

    @Query("SELECT * FROM RecordDbModel ORDER BY recordTime ASC LIMIT 15")
    suspend fun getTopFifteen(): List<RecordDbModel>

}