package com.panasetskaia.charactersudoku.presentation.dict_screen


import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.panasetskaia.charactersudoku.R
import com.panasetskaia.charactersudoku.presentation.game_screen.RandomOrSelectDialogFragment

class SpinnerAdapter(
    private val mContext: Fragment,
    private val mLayoutResourceId: Int,
    val categories: List<String>,
    private val viewModel: ChineseCharacterViewModel
): ArrayAdapter<String>(mContext.requireContext(), mLayoutResourceId, categories) {
    override fun getCount(): Int {
        return categories.size
    }
    override fun getItem(position: Int): String {
        return categories[position]
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        if (convertView == null) {
            val inflater = (mContext as Fragment).layoutInflater
            convertView = inflater.inflate(mLayoutResourceId, parent, false)
        }
        try {
            val category: String = getItem(position)
            val categoryTextView = convertView!!.findViewById<View>(R.id.tv_cat) as TextView
            if (category!= NO_CAT) {
                categoryTextView.text = category
            } else {
                categoryTextView.text = mContext.getString(R.string.default_category)
            }
            if (mContext !is SingleCharacterFragment) {
                categoryTextView.setTextColor(mContext.resources.getColor(R.color.boardLineColor))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return convertView!!
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val layout = if (mContext is RandomOrSelectDialogFragment) {
            mLayoutResourceId
        } else R.layout.category_spinner_item_dropdown
        var convertView = convertView
        if (convertView == null) {
            val inflater = (mContext as Fragment).layoutInflater
            convertView = inflater.inflate(layout, parent, false)
        }
        try {
            val category: String = getItem(position)
            val categoryTextView = convertView!!.findViewById<View>(R.id.tv_cat) as TextView
            categoryTextView.text = category
            if (mContext !is RandomOrSelectDialogFragment) {
                val deleteButton = convertView.findViewById<View>(R.id.deleteCategoryButton) as ImageView
                if (category!= NO_CAT) {
                    deleteButton.setOnClickListener {
                        viewModel.deleteThisCategory(category)
                    }
                } else {
                    deleteButton.visibility = View.GONE
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return convertView!!
    }

    companion object {
        const val NO_CAT = "-"
    }
}
