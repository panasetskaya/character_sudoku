package com.panasetskaia.charactersudoku.presentation.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.panasetskaia.charactersudoku.R
import com.panasetskaia.charactersudoku.databinding.RecordItemBinding
import com.panasetskaia.charactersudoku.domain.entities.Level
import com.panasetskaia.charactersudoku.domain.entities.Record
import com.panasetskaia.charactersudoku.utils.formatToTime

class RecordListAdapter(private val activity: Activity) :
    ListAdapter<Record, RecordListAdapter.RecordViewHolder>(RecordItemDiffUtil()) {

    class RecordViewHolder(val binding: RecordItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
        val binding = RecordItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RecordViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        val item = getItem(position)
        with(holder.binding) {
            tvRecordDate.text = item.date
            tvRecordLevel.text = when (item.level) {
                Level.EASY -> activity.getString(R.string.lvl1)
                Level.MEDIUM -> activity.getString(R.string.lvl2)
                Level.HARD -> activity.getString(R.string.lvl3)
            }
            tvRecordTime.text = item.recordTime.formatToTime(activity)
        }
    }
}