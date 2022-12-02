package com.panasetskaia.charactersudoku.presentation.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.panasetskaia.charactersudoku.R
import com.panasetskaia.charactersudoku.databinding.FragmentExportBinding
import com.panasetskaia.charactersudoku.presentation.MainActivity
import com.panasetskaia.charactersudoku.presentation.viewmodels.ChineseCharacterViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File

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
        viewModel.pathLiveData.observe(viewLifecycleOwner) {
            Log.d("MYMYMY", it)
            if (it!="") {
                startFileShareIntent(it)
            }
        }
    }

    private fun setupListeners() {
        binding.toCsvButton.setOnClickListener {
            viewModel.saveDictionaryToCSV()
        }
        binding.toJsonButton.setOnClickListener {
            viewModel.saveDictionaryToJson()
        }
        binding.fromJsonButton.setOnClickListener {
            Toast.makeText(requireActivity(), "Not implemented yet", Toast.LENGTH_SHORT)
                .show() //todo: импорт из внешнего json
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

    private fun startFileShareIntent(filePath: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "*/*"
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            putExtra(
                Intent.EXTRA_SUBJECT,
                "Sharing file from Mandarindoku"
            )
            putExtra(
                Intent.EXTRA_TEXT,
                "Sharing file from Mandarindoku"
            )
            val fileURI = FileProvider.getUriForFile(
                requireContext(), requireActivity().application.packageName + ".provider",
                File(filePath)
            )
            putExtra(Intent.EXTRA_STREAM, fileURI)
        }
        startActivity(shareIntent)
    }
}