package com.panasetskaia.charactersudoku.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.panasetskaia.charactersudoku.data.gameGenerator.SudokuGame
import com.panasetskaia.charactersudoku.domain.entities.Cell

@Entity
data class BoardDbModel (
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    val size: Int = SudokuGame.GRID_SIZE,
    @TypeConverters(SudokuConverters::class)
    val cells: List<Cell>
        )