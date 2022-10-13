package com.panasetskaia.charactersudoku.presentation.fragments.dialogFragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.panasetskaia.charactersudoku.R
import com.panasetskaia.charactersudoku.application.SudokuApplication
import com.panasetskaia.charactersudoku.databinding.FragmentConfirmRefreshBinding
import com.panasetskaia.charactersudoku.presentation.viewmodels.GameViewModel
import com.panasetskaia.charactersudoku.presentation.viewmodels.ViewModelFactory
import javax.inject.Inject

class ConfirmRefreshFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val component by lazy {
        (requireActivity().application as SudokuApplication).component
    }

    private lateinit var viewModel: GameViewModel

    private var _binding: FragmentConfirmRefreshBinding? = null
    private val binding: FragmentConfirmRefreshBinding
        get() = _binding ?: throw RuntimeException("FragmentConfirmRefreshBinding is null")

    override fun onAttach(context: Context) {
        component.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentConfirmRefreshBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this, viewModelFactory)[GameViewModel::class.java]
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