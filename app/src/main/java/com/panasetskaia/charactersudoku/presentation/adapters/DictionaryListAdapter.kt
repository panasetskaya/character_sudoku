package com.panasetskaia.charactersudoku.presentation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.panasetskaia.charactersudoku.databinding.CharacterItemNotChosenBinding
import com.panasetskaia.charactersudoku.domain.entities.ChineseCharacter

class DictionaryListAdapter: ListAdapter<ChineseCharacter, ChineseCharViewHolder>(ChineseCharDiffUtilCallback()) {

    var onCharacterItemClickListener: ((ChineseCharacter) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChineseCharViewHolder {
        val binding = CharacterItemNotChosenBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChineseCharViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChineseCharViewHolder, position: Int) {
        val item = getItem(position)
        val binding = holder.binding
        binding.root.setOnLongClickListener {
            item.isChosen = !item.isChosen
            true
        }
        binding.root.setOnClickListener {
            onCharacterItemClickListener?.invoke(item)
            true
        }
        binding.root.isChecked = item.isChosen
        with(binding) {
            tvHowOften.text = item.timesPlayed.toString()
            tvCharacterChinese.text = item.character
            tvPinyin.text = item.pinyin
            tvTranslation.text = item.translation
        }
    }

    companion object {
        const val MAX_POOL_SIZE = 15
    }

}