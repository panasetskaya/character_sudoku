package com.panasetskaia.charactersudoku.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class CategoryDbModel(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    val categoryName: String
)
