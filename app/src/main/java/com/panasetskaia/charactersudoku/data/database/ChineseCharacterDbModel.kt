package com.panasetskaia.charactersudoku.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ChineseCharacterDbModel (
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    var character: String,
    var transcription: String,
    var translation: String,
    var usages: String,
    var isChosen: Boolean = false
)