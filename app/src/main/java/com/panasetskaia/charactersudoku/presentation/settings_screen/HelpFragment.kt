package com.panasetskaia.charactersudoku.presentation.settings_screen

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import com.panasetskaia.charactersudoku.R
import com.panasetskaia.charactersudoku.databinding.FragmentHelpBinding
import com.panasetskaia.charactersudoku.presentation.dict_screen.DictionaryFragment
import com.panasetskaia.charactersudoku.presentation.game_screen.GameFragment

class HelpFragment : Fragment() {

    private var _binding: FragmentHelpBinding? = null
    private val binding: FragmentHelpBinding
        get() = _binding ?: throw RuntimeException("FragmentHelpBinding is null")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHelpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}