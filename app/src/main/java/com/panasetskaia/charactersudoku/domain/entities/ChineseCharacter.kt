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
    var isChosen: Boolean = false,
    var category: String? = NO_CAT
): Parcelable {

    companion object {
        const val UNDEFINED_ID = 0
        const val NO_CAT = "no category"
    }
}