package com.panasetskaia.charactersudoku.presentation.dict_screen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.panasetskaia.charactersudoku.databinding.FragmentConfirmDeletingDialogBinding
import com.panasetskaia.charactersudoku.presentation.MainActivity

class ConfirmDeletingDialogFragment : Fragment() {

    private lateinit var viewModel: ChineseCharacterViewModel

    private var characterId: Int = -1
    private var mode = MODE_SINGLE

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
    ): View {
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
        if (mode == MODE_SINGLE) {
            launchSingleMode()
        } else {
            launchListMode()
        }

    }

    private fun launchSingleMode() {
        binding.deleteButton.setOnClickListener {
            viewModel.deleteCharacterFromDict(characterId)
            parentFragmentManager.popBackStack()
        }
    }

    private fun launchListMode() {
        binding.deleteButton.setOnClickListener {
            viewModel.deleteSelected()
            parentFragmentManager.popBackStack()
        }
    }

    private fun parseParams() {
        val args = requireArguments()
        if (!args.containsKey(MODE_EXTRA)) {
            throw RuntimeException("No mode extra!")
        } else {
            mode = args.getString(MODE_EXTRA) ?: MODE_SINGLE
        }
        if (args.containsKey(ITEM_ID_EXTRA)) {
            characterId = args.getInt(ITEM_ID_EXTRA, DEFAULT_ID)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        viewModel.finishDeleting(true)
    }

    companion object {
        const val MODE_EXTRA = "mode"
        const val MODE_SINGLE = "single"
        const val MODE_LIST = "list"
        const val ITEM_ID_EXTRA = "item_id"
        private const val DEFAULT_ID = -1
    }
}