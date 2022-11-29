package com.panasetskaia.charactersudoku.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.panasetskaia.charactersudoku.domain.entities.Level

@Entity
data class RecordDbModel(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    val recordTime: Long,
    @TypeConverters(SudokuConverters::class)
    val level: Level,
    val date: String
)
