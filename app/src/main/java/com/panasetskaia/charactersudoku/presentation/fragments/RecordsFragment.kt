package com.panasetskaia.charactersudoku.presentation.fragments

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import com.panasetskaia.charactersudoku.R
import com.panasetskaia.charactersudoku.databinding.FragmentRecordsBinding

class RecordsFragment : Fragment() {

    private var _binding: FragmentRecordsBinding? = null
    private val binding: FragmentRecordsBinding
        get() = _binding ?: throw RuntimeException("FragmentRecordsBinding is null")


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecordsBinding.inflate(inflater, container, false)
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
                        val arguments = Bundle().apply {
                            putString(
                                DictionaryFragment.FILTER_EXTRA,
                                DictionaryFragment.NO_FILTER
                            )
                        }
                        parentFragmentManager.popBackStack()
                        replaceWithThisFragment(DictionaryFragment::class.java,arguments)
                        true
                    }
                    R.id.help_sudoku_icon -> {
                        parentFragmentManager.popBackStack()
                        replaceWithThisFragment(GameFragment::class.java, null)
                        true
                    }
                    else -> true
                }
            }
        }, viewLifecycleOwner)
    }

    private fun replaceWithThisFragment(fragment: Class<out Fragment>, args: Bundle?) {
        parentFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .replace(R.id.fcvMain, fragment, args)
            .addToBackStack(null)
            .commit()
    }
}