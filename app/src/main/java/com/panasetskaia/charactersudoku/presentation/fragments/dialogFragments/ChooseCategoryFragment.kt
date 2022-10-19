package com.panasetskaia.charactersudoku.presentation.fragments.dialogFragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.panasetskaia.charactersudoku.databinding.FragmentChooseCategoryBinding
import com.panasetskaia.charactersudoku.presentation.MainActivity
import com.panasetskaia.charactersudoku.presentation.viewmodels.ChineseCharacterViewModel


class ChooseCategoryFragment : Fragment() {

    private lateinit var viewModel: ChineseCharacterViewModel

    private var _binding: FragmentChooseCategoryBinding? = null
    private val binding: FragmentChooseCategoryBinding
        get() = _binding ?: throw RuntimeException("FragmentChooseCategoryBinding is null")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChooseCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as MainActivity).characterViewModel
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        viewModel.finishDeleting(true)
    }
}