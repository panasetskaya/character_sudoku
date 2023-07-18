package com.panasetskaia.charactersudoku.data.database.board

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.panasetskaia.charactersudoku.data.database.SudokuConverters
import com.panasetskaia.charactersudoku.data.gameGenerator.SudokuGame
import com.panasetskaia.charactersudoku.domain.entities.Cell
import com.panasetskaia.charactersudoku.domain.entities.Level

@Entity
data class BoardDbModel (
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    val size: Int = SudokuGame.GRID_SIZE,
    @TypeConverters(SudokuConverters::class)
    val cells: List<Cell>,
    val nineChars: List<String>,
    var timeSpent: Long = 0,
    var alreadyFinished: Boolean = false,
    val level: Level = Level.EASY
        )