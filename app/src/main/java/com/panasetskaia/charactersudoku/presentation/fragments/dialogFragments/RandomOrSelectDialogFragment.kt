package com.panasetskaia.charactersudoku.presentation.fragments.dialogFragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.panasetskaia.charactersudoku.R
import com.panasetskaia.charactersudoku.databinding.FragmentRandomOrSelectDialogBinding
import com.panasetskaia.charactersudoku.domain.entities.Level
import com.panasetskaia.charactersudoku.presentation.MainActivity
import com.panasetskaia.charactersudoku.presentation.adapters.SpinnerAdapter
import com.panasetskaia.charactersudoku.presentation.fragments.DictionaryFragment
import com.panasetskaia.charactersudoku.presentation.viewmodels.ChineseCharacterViewModel
import com.panasetskaia.charactersudoku.presentation.viewmodels.GameViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class RandomOrSelectDialogFragment : Fragment() {

    private lateinit var gameViewModel: GameViewModel
    private lateinit var charViewModel: ChineseCharacterViewModel
    private var categoriesAmount = 0

    private var _binding: FragmentRandomOrSelectDialogBinding? = null
    private val binding: FragmentRandomOrSelectDialogBinding
        get() = _binding ?: throw RuntimeException("FragmentRandomOrSelectDialogBinding is null")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRandomOrSelectDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        gameViewModel = (activity as MainActivity).gameViewModel
        charViewModel = (activity as MainActivity).characterViewModel
        binding.selectCatGroup.visibility = View.GONE
        binding.randomButton.setOnClickListener {
            collectCategories()
            val lvl = getLevel()
            if (categoriesAmount <= 1) {
                parentFragmentManager.popBackStack()
                gameViewModel.getNewRandomGame(lvl)
            } else {
                binding.selectCatGroup.visibility = View.VISIBLE
                binding.okButton.setOnClickListener {
                    val category = binding.spinnerChooseCat.selectedItem.toString()
                    if (category == SpinnerAdapter.NO_CAT) {
                        parentFragmentManager.popBackStack()
                        gameViewModel.getNewRandomGame(lvl)
                    } else {
                        parentFragmentManager.popBackStack()
                        gameViewModel.getRandomGameWithCategory(category,lvl)
                    }
                }
            }
        }
        binding.selectCharactersButton.setOnClickListener {
            val lvl = getLevel()
            gameViewModel.setSettingsState(true)
            gameViewModel.setLevel(lvl)
            parentFragmentManager.popBackStack()
            val args = Bundle().apply {
                putString(
                    DictionaryFragment.FILTER_EXTRA,
                    DictionaryFragment.NO_FILTER
                )
            }
            parentFragmentManager.commit {
                setReorderingAllowed(true)
                replace(R.id.fcvMain, DictionaryFragment::class.java, args)
                addToBackStack(null)
            }
        }
    }

    private fun collectCategories() {

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                charViewModel.categoriesFlow.collectLatest { categories ->
                    val listOfCategories = mutableListOf<String>()
                    for (i in categories) {
                        listOfCategories.add(i.categoryName)
                    }
                    categoriesAmount = listOfCategories.size
                    val adapterForSpinner = SpinnerAdapter(
                        this@RandomOrSelectDialogFragment,
                        R.layout.category_spinner_item,
                        listOfCategories,
                        charViewModel
                    )
                    binding.spinnerChooseCat.adapter = adapterForSpinner
                }
            }
        }
    }

    private fun getLevel(): Level {
        return when (binding.radiogroup.checkedRadioButtonId) {
            binding.radioEasy.id -> {
                Level.EASY
            }
            binding.radioMedium.id -> {
                Level.MEDIUM
            }
            binding.radioHard.id -> {
                Level.HARD
            } else -> Level.MEDIUM
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}