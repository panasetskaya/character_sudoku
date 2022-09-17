package com.panasetskaia.charactersudoku.domain

enum class GameLevel (val numberOfProvidedDigits: Int) {
    JUNIOR(25),
    MID(20),
    SENIOR(17);
}