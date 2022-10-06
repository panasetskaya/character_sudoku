package com.panasetskaia.charactersudoku.presentation.fragments.dialogFragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.panasetskaia.charactersudoku.R
import com.panasetskaia.charactersudoku.databinding.FragmentConfirmStartGameBinding
import com.panasetskaia.charactersudoku.presentation.MainActivity
import com.panasetskaia.charactersudoku.presentation.fragments.GameFragment
import com.panasetskaia.charactersudoku.presentation.viewmodels.ChineseCharacterViewModel
import com.panasetskaia.charactersudoku.presentation.viewmodels.GameViewModel


class ConfirmStartGameFragment : Fragment() {

    private lateinit var characterViewModel: ChineseCharacterViewModel
    private lateinit var gameViewModel: GameViewModel

    private var _binding: FragmentConfirmStartGameBinding? = null
    private val binding: FragmentConfirmStartGameBinding
        get() = _binding ?: throw RuntimeException("FragmentConfirmStartGameBinding is null")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentConfirmStartGameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        gameViewModel = (activity as MainActivity).gameViewModel
        characterViewModel = (activity as MainActivity).characterViewModel
        binding.cancelButton.setOnClickListener {
            characterViewModel.finishDialog(true)
            parentFragmentManager.popBackStack()
        }
        binding.startButton.setOnClickListener {
            characterViewModel.selectedCharactersLiveData.observe(viewLifecycleOwner) { selected ->
                if (selected.size == 9) {
                    gameViewModel.getGameWithSelected(selected)
                }
                for (i in selected) {
                    characterViewModel.updatePlayedCount(i)
                }
            }
            characterViewModel.markAllUnselected()
            parentFragmentManager.popBackStack()
            val fragment = GameFragment.newInstance()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fcvMain, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = ConfirmStartGameFragment()
    }
}