package com.panasetskaia.charactersudoku.domain.entities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Category(
    var id: Int = 0,
    val categoryName: String = NO_CAT
): Parcelable {
    companion object {
        const val NO_CAT = "no category"
    }
}


