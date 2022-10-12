package com.panasetskaia.charactersudoku.presentation.fragments

import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import com.panasetskaia.charactersudoku.R
import com.panasetskaia.charactersudoku.databinding.FragmentHelpBinding


class HelpFragment : Fragment() {

    private var _binding: FragmentHelpBinding? = null
    private val binding: FragmentHelpBinding
        get() = _binding ?: throw RuntimeException("FragmentHelpBinding is null")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHelpBinding.inflate(inflater, container, false)
        (activity as AppCompatActivity).setSupportActionBar(binding.appBar)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMenu()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.help_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.help_dict_icon -> {
                        parentFragmentManager.popBackStack()
                        val fragment = DictionaryFragment.newInstance()
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fcvMain, fragment)
                            .addToBackStack(null)
                            .commit()
                        true
                    }
                    R.id.help_sudoku_icon -> {
                        parentFragmentManager.popBackStack()
                        val fragment = GameFragment.newInstance()
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fcvMain, fragment)
                            .addToBackStack(null)
                            .commit()
                        true
                    }
                    else -> true
                }
            }
        }, viewLifecycleOwner)
    }

    companion object {
        @JvmStatic
        fun newInstance() = HelpFragment()
    }
}