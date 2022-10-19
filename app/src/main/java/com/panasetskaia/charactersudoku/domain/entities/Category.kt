package com.panasetskaia.charactersudoku.domain.entities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Category(
    var id: Int = 0,
    val categoryName: String
): Parcelable
