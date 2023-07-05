package com.panasetskaia.charactersudoku.presentation.settings_screen

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.panasetskaia.charactersudoku.R
import com.panasetskaia.charactersudoku.databinding.FragmentRecordsBinding
import com.panasetskaia.charactersudoku.databinding.FragmentSettingsBinding
import com.panasetskaia.charactersudoku.utils.replaceWithThisFragment


class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding: FragmentSettingsBinding
        get() = _binding ?: throw RuntimeException("FragmentSettingsBinding is null")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setListeners()
    }

    private fun setListeners() {
        with(binding) {
            buttonTopResults.setOnClickListener {
                replaceWithThisFragment(RecordsFragment::class.java, null)
            }
            buttonExport.setOnClickListener {
                replaceWithThisFragment(ExportFragment::class.java, null)
            }
            buttonHelp.setOnClickListener {
                replaceWithThisFragment(HelpFragment::class.java, null)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}