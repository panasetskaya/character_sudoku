package com.panasetskaia.charactersudoku.presentation.fragments.dialogFragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.panasetskaia.charactersudoku.R
import com.panasetskaia.charactersudoku.databinding.FragmentConfirmRefreshBinding
import com.panasetskaia.charactersudoku.presentation.MainActivity
import com.panasetskaia.charactersudoku.presentation.viewmodels.GameViewModel

class ConfirmRefreshFragment : Fragment() {

    private lateinit var viewModel: GameViewModel

    private var _binding: FragmentConfirmRefreshBinding? = null
    private val binding: FragmentConfirmRefreshBinding
        get() = _binding ?: throw RuntimeException("FragmentConfirmRefreshBinding is null")


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentConfirmRefreshBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as MainActivity).gameViewModel
        binding.cancelButton.setOnClickListener {
            viewModel.setSettingsState(true)
            parentFragmentManager.popBackStack()
        }
        binding.yesButton.setOnClickListener {
            parentFragmentManager.popBackStack()
            val fragment = RandomOrSelectDialogFragment.newInstance()
            parentFragmentManager.beginTransaction()
                .add(R.id.gameContainerView, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = ConfirmRefreshFragment()
    }
}