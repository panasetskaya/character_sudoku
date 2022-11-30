package com.panasetskaia.charactersudoku.domain.entities

data class Record(
    val id: Int = 0,
    val recordTime: Long,
    val level: Level,
    val date: String
)
