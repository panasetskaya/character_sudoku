package com.panasetskaia.charactersudoku.domain.entities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ChineseCharacter(
    var character: String,
    var transcription: String,
    var translation: String,
    var usages: String,
    var timesPlayed: Int
): Parcelable
