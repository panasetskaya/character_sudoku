package com.panasetskaia.charactersudoku.presentation.fragments.dialogFragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.panasetskaia.charactersudoku.R
import com.panasetskaia.charactersudoku.databinding.FragmentRandomOrSelectDialogBinding
import com.panasetskaia.charactersudoku.presentation.MainActivity
import com.panasetskaia.charactersudoku.presentation.fragments.DictionaryFragment
import com.panasetskaia.charactersudoku.presentation.viewmodels.GameViewModel

class RandomOrSelectDialogFragment : Fragment() {

    private lateinit var viewModel: GameViewModel

    private var _binding: FragmentRandomOrSelectDialogBinding? = null
    private val binding: FragmentRandomOrSelectDialogBinding
        get() = _binding ?: throw RuntimeException("FragmentRandomOrSelectDialogBinding is null")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRandomOrSelectDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as MainActivity).gameViewModel

        binding.randomButton.setOnClickListener {
            viewModel.setSettingsState(true)
            parentFragmentManager.popBackStack()
            viewModel.getNewRandomGame()

        }
        binding.selectCharactersButton.setOnClickListener {
            viewModel.setSettingsState(true)
            val fragment = DictionaryFragment.newInstance()
            parentFragmentManager.popBackStack()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fcvMain,fragment)
                .addToBackStack(null)
                .commit()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = RandomOrSelectDialogFragment()
    }

}