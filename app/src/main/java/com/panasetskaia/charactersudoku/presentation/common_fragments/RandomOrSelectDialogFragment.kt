package com.panasetskaia.charactersudoku.presentation.common_fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.panasetskaia.charactersudoku.R
import com.panasetskaia.charactersudoku.databinding.FragmentRandomOrSelectDialogBinding
import com.panasetskaia.charactersudoku.domain.entities.Level
import com.panasetskaia.charactersudoku.presentation.MainActivity
import com.panasetskaia.charactersudoku.presentation.dict_screen.SpinnerAdapter
import com.panasetskaia.charactersudoku.presentation.dict_screen.DictionaryFragment
import com.panasetskaia.charactersudoku.presentation.game_screen.GameFragment
import com.panasetskaia.charactersudoku.presentation.dict_screen.ChineseCharacterViewModel
import com.panasetskaia.charactersudoku.presentation.game_screen.GameViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class RandomOrSelectDialogFragment : Fragment() {

    private lateinit var gameViewModel: GameViewModel
    private lateinit var charViewModel: ChineseCharacterViewModel
    private var categoriesAmount = 0
    private var mode = MODE_FROM_GAME

    private var _binding: FragmentRandomOrSelectDialogBinding? = null
    private val binding: FragmentRandomOrSelectDialogBinding
        get() = _binding ?: throw RuntimeException("FragmentRandomOrSelectDialogBinding is null")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        parseParams()
    }

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
        when (mode) {
            MODE_FROM_GAME -> launchModeFromGame()
            else -> launchModeFromDict()
        }

    }

    private fun parseParams() {
        val args = requireArguments()
        if (!args.containsKey(EXTRA_MODE)) {
            throw RuntimeException("No param: Mode")
        }
        mode = args.getString(EXTRA_MODE) ?: MODE_FROM_GAME
    }

    private fun launchModeFromGame() {
        binding.selectCatGroup.visibility = View.GONE
        binding.randomButton.setOnClickListener {
            binding.randomSelectGroup.visibility = View.GONE
            collectCategories()
            if (categoriesAmount <= 1) {
                binding.selectLvlGroup.visibility = View.VISIBLE
                binding.okButton.setOnClickListener {
                    val lvl = getLevel()
                    parentFragmentManager.popBackStack()
                    gameViewModel.getNewRandomGame(lvl)
                }
            } else {
                binding.selectCatGroup.visibility = View.VISIBLE
                binding.okButton.setOnClickListener {
                    val lvl = getLevel()
                    val category = binding.spinnerChooseCat.selectedItem.toString()
                    if (category == SpinnerAdapter.NO_CAT) {
                        parentFragmentManager.popBackStack()
                        gameViewModel.getNewRandomGame(lvl)
                    } else {
                        parentFragmentManager.popBackStack()
                        gameViewModel.getRandomGameWithCategory(category, lvl)
                    }
                }
            }
        }
        binding.selectCharactersButton.setOnClickListener {
            parentFragmentManager.popBackStack()
            parentFragmentManager.commit {
                setReorderingAllowed(true)
                replace(R.id.fcvMain, DictionaryFragment::class.java, null)
                addToBackStack(null)
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            gameViewModel.launchOldBoard()
            parentFragmentManager.popBackStack()
        }
    }

    private fun launchModeFromDict() {
        binding.randomSelectGroup.visibility = View.GONE
        binding.spinnerChooseCat.visibility = View.GONE
        binding.okButton.setOnClickListener {
            val lvl = getLevel()
            gameViewModel.setLevel(lvl)
            gameViewModel.getGameWithSelected(Level.EASY)
            charViewModel.markAllUnselected()
            parentFragmentManager.popBackStack()
            parentFragmentManager.beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fcvMain, GameFragment::class.java, null)
                .addToBackStack(null)
                .commit()
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
            }
            else -> Level.MEDIUM
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

    companion object {
        const val EXTRA_MODE = "extra_mode"
        const val MODE_FROM_GAME = "from_game"
        const val MODE_FROM_DICT = "from_dict"
    }
}