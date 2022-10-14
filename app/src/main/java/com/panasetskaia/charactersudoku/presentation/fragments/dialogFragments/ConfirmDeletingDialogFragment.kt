package com.panasetskaia.charactersudoku.presentation.fragments.dialogFragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.panasetskaia.charactersudoku.databinding.FragmentConfirmDeletingDialogBinding
import com.panasetskaia.charactersudoku.presentation.MainActivity
import com.panasetskaia.charactersudoku.presentation.viewmodels.ChineseCharacterViewModel


class ConfirmDeletingDialogFragment : Fragment() {


    private lateinit var viewModel: ChineseCharacterViewModel

    private var characterId: Int = -1

    private var _binding: FragmentConfirmDeletingDialogBinding? = null
    private val binding: FragmentConfirmDeletingDialogBinding
        get() = _binding ?: throw RuntimeException("FragmentConfirmDeletingDialogBinding is null")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        parseParams()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentConfirmDeletingDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as MainActivity).characterViewModel
        binding.cancelButton.setOnClickListener {
            viewModel.finishDeleting(true)
            parentFragmentManager.popBackStack()
        }
        binding.deleteButton.setOnClickListener {
            viewModel.deleteCharacterFromDict(characterId)
            parentFragmentManager.popBackStack()
        }
    }

    private fun parseParams() {
        val args = requireArguments()
        if (!args.containsKey(ITEM_ID_EXTRA)) {
            throw RuntimeException("No ITEM_ID_EXTRA param")
        }
        characterId = args.getInt(ITEM_ID_EXTRA, DEFAULT_ID)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ITEM_ID_EXTRA = "item_id"
        private const val DEFAULT_ID = -1
    }
}