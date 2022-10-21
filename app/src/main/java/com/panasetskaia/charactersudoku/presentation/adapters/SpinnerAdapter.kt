package com.panasetskaia.charactersudoku.presentation.adapters


import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.panasetskaia.charactersudoku.R

class SpinnerAdapter(
    private val mContext: Fragment,
    private val mLayoutResourceId: Int,
    val categories: List<String>
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
            val cityAutoCompleteView = convertView!!.findViewById<View>(R.id.tv_cat) as TextView
            cityAutoCompleteView.text = category
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return convertView!!
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        if (convertView == null) {
            val inflater = (mContext as Fragment).layoutInflater
            convertView = inflater.inflate(mLayoutResourceId, parent, false)
        }
        try {
            val category: String = getItem(position)
            val cityAutoCompleteView = convertView!!.findViewById<View>(R.id.tv_cat) as TextView
            cityAutoCompleteView.text = category
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return convertView!!
    }
}
