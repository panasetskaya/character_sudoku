package com.panasetskaia.charactersudoku.presentation.fragments.dialogFragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.panasetskaia.charactersudoku.application.SudokuApplication
import com.panasetskaia.charactersudoku.databinding.FragmentConfirmDeletingDialogBinding
import com.panasetskaia.charactersudoku.presentation.viewmodels.ChineseCharacterViewModel
import com.panasetskaia.charactersudoku.presentation.viewmodels.ViewModelFactory
import javax.inject.Inject


class ConfirmDeletingDialogFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val component by lazy {
        (requireActivity().application as SudokuApplication).component
    }

    private lateinit var viewModel: ChineseCharacterViewModel

    private var characterId: Int = -1

    private var _binding: FragmentConfirmDeletingDialogBinding? = null
    private val binding: FragmentConfirmDeletingDialogBinding
        get() = _binding ?: throw RuntimeException("FragmentConfirmDeletingDialogBinding is null")

    override fun onAttach(context: Context) {
        component.inject(this)
        super.onAttach(context)
    }

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
        viewModel = ViewModelProvider(this, viewModelFactory)[ChineseCharacterViewModel::class.java]
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
        characterId = args.getInt(ITEM_ID_EXTRA, -1)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {

        private const val ITEM_ID_EXTRA = "item_id"

        fun newInstance(itemId: Int): ConfirmDeletingDialogFragment {
            return ConfirmDeletingDialogFragment().apply {
                arguments = Bundle().apply {
                    putInt(ITEM_ID_EXTRA, itemId)
                }
            }
        }
    }
}