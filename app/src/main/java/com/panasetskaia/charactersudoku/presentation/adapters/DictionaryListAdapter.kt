package com.panasetskaia.charactersudoku.presentation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.panasetskaia.charactersudoku.databinding.CharacterItemNotChosenBinding
import com.panasetskaia.charactersudoku.domain.entities.ChineseCharacter

class DictionaryListAdapter: ListAdapter<ChineseCharacter, ChineseCharViewHolder>(ChineseCharDiffUtilCallback()) {

    var onCharacterItemClickListener: ((ChineseCharacter) -> Unit)? = null
    var onCharacterItemLongClickListener: ((ChineseCharacter) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChineseCharViewHolder {
        val binding = CharacterItemNotChosenBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChineseCharViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChineseCharViewHolder, position: Int) {
        val item = getItem(position)
        val binding = holder.binding
        binding.root.setOnLongClickListener {
            onCharacterItemLongClickListener?.invoke(item)
            true
        }
        binding.root.setOnClickListener {
            onCharacterItemClickListener?.invoke(item)
            true
        }
        binding.root.isChecked = item.isChosen
        with(binding) {
            tvHowOften.text = if (item.timesPlayed>=1) {item.timesPlayed.toString() } else ""
            tvCharacterChinese.text = item.character
            tvPinyin.text = item.pinyin
            val translationCut = if (item.translation.length<=21) item.translation else item.translation.substring(0..21) + "..."
            tvTranslation.text =  translationCut
        }
    }
}

//todo: проблема - при редактировании иероглифа либо выделении его он прыгает в конец списка