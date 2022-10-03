package com.panasetskaia.charactersudoku.domain.entities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ChineseCharacter(
    var id: Int = UNDEFINED_ID,
    var character: String,
    var pinyin: String,
    var translation: String,
    var usages: String,
    var timesPlayed: Int = 0,
    var isChosen: Boolean = false
): Parcelable {

    companion object {
        const val UNDEFINED_ID = 0
    }
}