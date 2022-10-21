package com.panasetskaia.charactersudoku.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class CategoryDbModel(
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    val categoryName: String = NO_CAT
) {
    companion object {
        const val NO_CAT = "no category"
    }
}
