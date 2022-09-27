package com.panasetskaia.charactersudoku.presentation

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.panasetskaia.charactersudoku.R

class DictionaryFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dictionary, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }



    companion object {

        @JvmStatic
        fun newInstance() = DictionaryFragment()

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.dict_toolbar_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }


}