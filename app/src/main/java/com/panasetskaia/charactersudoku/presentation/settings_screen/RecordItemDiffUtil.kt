package com.panasetskaia.charactersudoku.presentation.settings_screen

import androidx.recyclerview.widget.DiffUtil
import com.panasetskaia.charactersudoku.domain.entities.Record

class RecordItemDiffUtil : DiffUtil.ItemCallback<Record>() {
    override fun areItemsTheSame(oldItem: Record, newItem: Record): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Record, newItem: Record): Boolean {
        return oldItem == newItem
    }
}