package com.panasetskaia.charactersudoku.presentation.game_screen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.panasetskaia.charactersudoku.R
import com.panasetskaia.charactersudoku.databinding.FragmentConfirmRefreshBinding
import com.panasetskaia.charactersudoku.presentation.MainActivity
import com.panasetskaia.charactersudoku.presentation.common_fragments.RandomOrSelectDialogFragment

class ConfirmRefreshFragment : Fragment() {

    private lateinit var viewModel: GameViewModel

    private var _binding: FragmentConfirmRefreshBinding? = null
    private val binding: FragmentConfirmRefreshBinding
        get() = _binding ?: throw RuntimeException("FragmentConfirmRefreshBinding is null")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConfirmRefreshBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as MainActivity).gameViewModel
        binding.cancelButton.setOnClickListener {
            viewModel.launchOldBoard()
            parentFragmentManager.popBackStack()
        }
        binding.yesButton.setOnClickListener {
            parentFragmentManager.popBackStack()
            val args = Bundle().apply {
                putString(
                    RandomOrSelectDialogFragment.EXTRA_MODE,
                    RandomOrSelectDialogFragment.MODE_FROM_GAME
                )
            }
            parentFragmentManager.commit {
                setReorderingAllowed(true)
                add(R.id.gameContainerView, RandomOrSelectDialogFragment::class.java, args)
                addToBackStack(null)
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            viewModel.launchOldBoard()
            parentFragmentManager.popBackStack()
        }
    }

    override fun onStop() {
        super.onStop()
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            isEnabled = false
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}