package com.panasetskaia.charactersudoku.presentation.adapters

import androidx.recyclerview.widget.DiffUtil
import com.panasetskaia.charactersudoku.domain.entities.ChineseCharacter

class ChineseCharDiffUtilCallback: DiffUtil.ItemCallback<ChineseCharacter>() {
    override fun areItemsTheSame(oldItem: ChineseCharacter, newItem: ChineseCharacter): Boolean {
        return oldItem.character == newItem.character
    }

    override fun areContentsTheSame(oldItem: ChineseCharacter, newItem: ChineseCharacter): Boolean {
        return oldItem == newItem
    }
}