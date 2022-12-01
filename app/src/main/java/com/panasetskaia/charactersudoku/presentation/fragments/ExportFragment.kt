package com.panasetskaia.charactersudoku.presentation.fragments

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import com.panasetskaia.charactersudoku.R
import com.panasetskaia.charactersudoku.databinding.FragmentExportBinding
import com.panasetskaia.charactersudoku.presentation.MainActivity
import com.panasetskaia.charactersudoku.presentation.viewmodels.ChineseCharacterViewModel

class ExportFragment : Fragment() {

    private lateinit var viewModel: ChineseCharacterViewModel
    private var _binding: FragmentExportBinding? = null
    private val binding: FragmentExportBinding
        get() = _binding ?: throw RuntimeException("FragmentExportBinding is null")


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExportBinding.inflate(inflater, container, false)
        (activity as AppCompatActivity).setSupportActionBar(binding.appBar)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as MainActivity).characterViewModel
        setupMenu()
        setupListeners()
    }

    private fun setupListeners() {
        binding.toCsvButton.setOnClickListener {
            viewModel.saveDictionaryToCSV()
        }
        binding.toJsonButton.setOnClickListener {
            viewModel.saveDictionaryToJson()
        }
        binding.fromJsonButton.setOnClickListener {
            Toast.makeText(requireActivity(), "Not implemented yet", Toast.LENGTH_SHORT).show() //todo: импорт из внешнего json
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.records_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.records_to_sudoku_icon -> {
                        parentFragmentManager.popBackStack()
                        true
                    }
                    else -> true
                }
            }
        }, viewLifecycleOwner)
    }
}