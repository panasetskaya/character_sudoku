package com.panasetskaia.charactersudoku.presentation.fragments.dialogFragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.panasetskaia.charactersudoku.R

class RandomOrSelectDialogFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_random_or_select_dialog, container, false)
    }

    companion object {
        fun newInstance() = RandomOrSelectDialogFragment()
    }

}