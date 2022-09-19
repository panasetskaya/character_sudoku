package com.panasetskaia.charactersudoku.domain.entities

class Cell(
    val row: Int,
    val col: Int,
    var value: Int,
    var isStartingCell: Boolean = false
)