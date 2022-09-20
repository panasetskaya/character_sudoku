package com.panasetskaia.charactersudoku.domain.entities

class Cell(
    val row: Int,
    val col: Int,
    var value: String,
    var isFixed: Boolean = false
)