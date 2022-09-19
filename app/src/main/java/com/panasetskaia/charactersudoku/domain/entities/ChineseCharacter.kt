package com.panasetskaia.charactersudoku.domain.entities

data class ChineseCharacter(
    var id: Int,
    var character: String,
    var transcription: String,
    var translation: String,
    var usages: String,
    var timesPlayed: Int
)
