package com.panasetskaia.charactersudoku.data.database

import androidx.room.*

@Dao
@TypeConverters(SudokuConverters::class)
interface BoardDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveGame(boardDbModel: BoardDbModel)

    @Query("SELECT * FROM BoardDbModel ORDER BY id DESC LIMIT 1")
    suspend fun getSavedGame(): BoardDbModel?

    @Query("DELETE FROM BoardDbModel")
    suspend fun deleteEverything()

}
