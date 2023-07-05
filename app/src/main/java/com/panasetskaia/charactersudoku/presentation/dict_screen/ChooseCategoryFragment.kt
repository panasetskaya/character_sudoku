package com.panasetskaia.charactersudoku.presentation.dict_screen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.panasetskaia.charactersudoku.R
import com.panasetskaia.charactersudoku.databinding.FragmentChooseCategoryBinding
import com.panasetskaia.charactersudoku.presentation.MainActivity
import com.panasetskaia.charactersudoku.presentation.game_screen.GameViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ChooseCategoryFragment : Fragment() {

    private lateinit var charViewModel: ChineseCharacterViewModel
    private lateinit var gameViewModel: GameViewModel
    private lateinit var adapterForSpinner: SpinnerAdapter

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
        charViewModel = (activity as MainActivity).characterViewModel
        gameViewModel = (activity as MainActivity).gameViewModel
        collectCategories()
        binding.okButton.setOnClickListener {
            val category = binding.spinnerChooseCat.selectedItem.toString()
            if (category== SpinnerAdapter.NO_CAT) {
                val arguments = Bundle().apply {
                    putString(
                        DictionaryFragment.FILTER_EXTRA,
                        DictionaryFragment.NO_FILTER
                    )
                }
                parentFragmentManager.popBackStack()
                replaceWithThisFragment(DictionaryFragment::class.java, arguments)
            } else {
                val arguments = Bundle().apply {
                    putString(
                        DictionaryFragment.FILTER_EXTRA,
                        category
                    )
                }
                parentFragmentManager.popBackStack()
                replaceWithThisFragment(DictionaryFragment::class.java, arguments)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        charViewModel.finishDeleting(true)
    }

    private fun collectCategories() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    charViewModel.categoriesFlow.collectLatest { categories ->
                        val listOfCategories = mutableListOf<String>()
                        for (i in categories) {
                            listOfCategories.add(i.categoryName)
                        }
                        adapterForSpinner = SpinnerAdapter(
                            this@ChooseCategoryFragment,
                            R.layout.category_spinner_item,
                            listOfCategories,
                            charViewModel
                        )
                        binding.spinnerChooseCat.adapter = adapterForSpinner
                    }
                }
            }
        }
    }

    private fun replaceWithThisFragment(fragment: Class<out Fragment>, args: Bundle) {
        parentFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .replace(R.id.fcvMain, fragment, args)
            .addToBackStack(null)
            .commit()
    }
}