package com.panasetskaia.charactersudoku.presentation.dict_screen

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.ListAdapter
import com.panasetskaia.charactersudoku.R
import com.panasetskaia.charactersudoku.databinding.CharacterItemChosenBinding
import com.panasetskaia.charactersudoku.databinding.CharacterItemNotChosenBinding
import com.panasetskaia.charactersudoku.domain.entities.ChineseCharacter

class DictionaryListAdapter :
    ListAdapter<ChineseCharacter, ChineseCharViewHolder>(ChineseCharDiffUtilCallback()) {

    var onCharacterItemClickListener: ((ChineseCharacter) -> Unit)? = null
    var onCharacterItemLongClickListener: ((ChineseCharacter) -> Unit)? = null
    private var foundItemPosition: Int? = null

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
        binding.root.setOnClickListener {
            onCharacterItemClickListener?.invoke(item)
            true
        }

        when (binding) {
            is CharacterItemChosenBinding -> {
                with(binding) {
                    tvCharacterChinese.text = item.character
                    tvPinyin.text = item.pinyin
                    tvTranslation.text = item.translation
                }
            }
            is CharacterItemNotChosenBinding -> {
                with(binding) {
                    tvCharacterChinese.text = item.character
                    tvPinyin.text = item.pinyin
                    tvTranslation.text = item.translation
                }
            }
        }
        if (foundItemPosition!=null && foundItemPosition==position) {
            binding.root.animation =
                AnimationUtils.loadAnimation(holder.itemView.context, R.anim.scroll_anim)
        }
        foundItemPosition=null

    }

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        return if (item.isChosen) CHOSEN
        else NOT_CHOSEN
    }

    fun setFoundItemPosition(p: Int) {
        foundItemPosition = p
    }

    companion object {
        const val CHOSEN = 1
        const val NOT_CHOSEN = 0
        const val MAX_POOL_SIZE = 15
    }
}