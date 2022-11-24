package com.panasetskaia.charactersudoku.presentation.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.ListAdapter
import com.panasetskaia.charactersudoku.R
import com.panasetskaia.charactersudoku.databinding.CharacterItemChosenBinding
import com.panasetskaia.charactersudoku.databinding.CharacterItemNotChosenBinding
import com.panasetskaia.charactersudoku.domain.entities.ChineseCharacter
import com.panasetskaia.charactersudoku.presentation.fragments.DictionaryFragment

class DictionaryListAdapter (val context: DictionaryFragment):
    ListAdapter<ChineseCharacter, ChineseCharViewHolder>(ChineseCharDiffUtilCallback()) {

    var onCharacterItemClickListener: ((ChineseCharacter, TextView) -> Unit)? = null
    var onCharacterItemLongClickListener: ((ChineseCharacter) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChineseCharViewHolder {
        val binding = when (viewType) {
            CHOSEN -> CharacterItemChosenBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            NOT_CHOSEN -> CharacterItemNotChosenBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            else -> throw RuntimeException("No such viewType: $viewType!")
        }
        return ChineseCharViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChineseCharViewHolder, position: Int) {
        val item = getItem(position)
        val binding = holder.binding
        binding.root.setOnLongClickListener {
            onCharacterItemLongClickListener?.invoke(item)
            true
        }
        when (binding) {
            is CharacterItemChosenBinding -> {
                with(binding) {
                    ViewCompat.setTransitionName(tvCharacterChinese, context.getString(R.string.big_char))
                    tvCharacterChinese.text = item.character
                    tvPinyin.text = item.pinyin
                    val translationCut =
                        if (item.translation.length <= 21) item.translation else item.translation.substring(
                            0..21
                        ) + "..."
                    tvTranslation.text = translationCut
                    root.setOnClickListener {
                        onCharacterItemClickListener?.invoke(item,tvCharacterChinese)
                        true
                    }
                }
            }
            is CharacterItemNotChosenBinding -> {
                with(binding) {
                    ViewCompat.setTransitionName(tvCharacterChinese, context.getString(R.string.big_char))
                    tvCharacterChinese.text = item.character
                    tvPinyin.text = item.pinyin
                    val translationCut =
                        if (item.translation.length <= 21) item.translation else item.translation.substring(
                            0..21
                        ) + "..."
                    tvTranslation.text = translationCut
                    root.setOnClickListener {
                        onCharacterItemClickListener?.invoke(item,tvCharacterChinese)
                        true
                    }
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        return if (item.isChosen) CHOSEN
        else NOT_CHOSEN
    }

    companion object {
        const val CHOSEN = 1
        const val NOT_CHOSEN = 0
        const val MAX_POOL_SIZE = 15
    }
}