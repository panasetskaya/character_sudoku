package com.panasetskaia.charactersudoku.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ChineseCharacterDb (
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var character: String,
    var transcription: String,
    var translation: String,
    var usages: String,
    var timesPlayed: Int,
    var isChosen: Boolean = false
)