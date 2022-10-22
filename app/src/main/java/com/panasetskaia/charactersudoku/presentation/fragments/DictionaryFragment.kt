package com.panasetskaia.charactersudoku.presentation.fragments

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.*
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.panasetskaia.charactersudoku.R
import com.panasetskaia.charactersudoku.databinding.FragmentDictionaryBinding
import com.panasetskaia.charactersudoku.domain.entities.ChineseCharacter
import com.panasetskaia.charactersudoku.presentation.MainActivity
import com.panasetskaia.charactersudoku.presentation.adapters.DictionaryListAdapter
import com.panasetskaia.charactersudoku.presentation.adapters.MyItemTouchCallback
import com.panasetskaia.charactersudoku.presentation.fragments.dialogFragments.ChooseCategoryFragment
import com.panasetskaia.charactersudoku.presentation.fragments.dialogFragments.ConfirmDeletingDialogFragment
import com.panasetskaia.charactersudoku.presentation.viewmodels.ChineseCharacterViewModel
import com.panasetskaia.charactersudoku.presentation.viewmodels.GameViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DictionaryFragment : Fragment() {

    private lateinit var characterViewModel: ChineseCharacterViewModel
    private lateinit var gameViewModel: GameViewModel
    private lateinit var listAdapter: DictionaryListAdapter
    private lateinit var itemTouchCallback: MyItemTouchCallback
    private lateinit var selectedCharacters: List<ChineseCharacter>
    private val linearInterpolator = LinearInterpolator()
    private var isFabPlayEnabled = false

    private var _binding: FragmentDictionaryBinding? = null
    private val binding: FragmentDictionaryBinding
        get() = _binding ?: throw RuntimeException("FragmentDictionaryBinding is null")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDictionaryBinding.inflate(inflater, container, false)
        (activity as AppCompatActivity).setSupportActionBar(binding.appBar)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        characterViewModel = (activity as MainActivity).characterViewModel
        gameViewModel = (activity as MainActivity).gameViewModel
        setupMenu()
        setupFab()
        setupRecyclerView()
        collectFlows()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.dict_toolbar_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.sudoku_icon -> {
                        parentFragmentManager.popBackStack()
                        replaceWithThisFragment(GameFragment::class.java, null)
                        true
                    }
                    R.id.dict_help_icon -> {
                        replaceWithThisFragment(HelpFragment::class.java, null)
                        true
                    }
                    R.id.dict_filter_icon -> {
                        parentFragmentManager.beginTransaction()
                            .setReorderingAllowed(true)
                            .add(R.id.fcvMain, ChooseCategoryFragment::class.java, arguments)
                            .addToBackStack(null)
                            .commit()
                        true
                    }
                    else -> true
                }
            }
        }, viewLifecycleOwner)
    }

    private fun setupFab() {
        shakeAdd()
        binding.fabAdd.setOnClickListener {
            val arguments = Bundle().apply {
                putInt(
                    SingleCharacterFragment.EXTRA_CHINESE_ID,
                    SingleCharacterFragment.NEW_CHAR_ID
                )
                putString(
                    SingleCharacterFragment.EXTRA_MODE,
                    SingleCharacterFragment.MODE_ADD
                )
            }
            replaceWithThisFragment(SingleCharacterFragment::class.java, arguments)
        }
        binding.fabPlay.setOnClickListener {
            if (isFabPlayEnabled) {
                gameViewModel.getGameWithSelected(selectedCharacters)
                characterViewModel.markAllUnselected()
                parentFragmentManager.popBackStack()
                replaceWithThisFragment(GameFragment::class.java, null)
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.not_enough),
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        }
    }

    private fun setupRecyclerView() {
        listAdapter = DictionaryListAdapter()
        itemTouchCallback = object : MyItemTouchCallback(this, listAdapter, characterViewModel) {}
        listAdapter.stateRestorationPolicy =
            RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        with(binding.recyclerViewList) {
            adapter = listAdapter
            recycledViewPool.setMaxRecycledViews(
                DictionaryListAdapter.CHOSEN,
                DictionaryListAdapter.MAX_POOL_SIZE
            )
            recycledViewPool.setMaxRecycledViews(
                DictionaryListAdapter.NOT_CHOSEN,
                DictionaryListAdapter.MAX_POOL_SIZE
            )
            listAdapter.onCharacterItemLongClickListener = {
                characterViewModel.changeIsChosenState(it)
            }
            listAdapter.onCharacterItemClickListener = {
                val arguments = Bundle().apply {
                    putInt(
                        SingleCharacterFragment.EXTRA_CHINESE_ID,
                        it.id
                    )
                    putString(
                        SingleCharacterFragment.EXTRA_MODE,
                        SingleCharacterFragment.MODE_EDIT
                    )
                }
                replaceWithThisFragment(SingleCharacterFragment::class.java, arguments)
            }
            setupSwipeListener(binding.recyclerViewList)
        }
    }

    private fun collectFlows() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    characterViewModel.dictionaryFlow.collectLatest {
                        listAdapter.submitList(it)
                    }
                }
                launch {
                    characterViewModel.selectedCharactersSharedFlow.collectLatest { selected ->
                        if (selected.size == 9) {
                            isFabPlayEnabled = true
                            selectedCharacters = selected
                            shakePlay()
                        } else {
                            isFabPlayEnabled = false
                        }
                    }
                }
            }
        }
    }

    private fun setupSwipeListener(rv: RecyclerView) {
        val itemTouchHelper = ItemTouchHelper(itemTouchCallback)
        itemTouchHelper.attachToRecyclerView(rv)
    }

    private fun shakeAdd() {
        AnimatorSet().apply {
            play(shakeAnimator(binding.fabAdd, "rotation", 0f))
            start()
        }
    }

    private fun shakePlay() {
        AnimatorSet().apply {
            play(shakeAnimator(binding.fabPlay, "rotation", 0f))
            start()
        }
    }

    private fun shakeAnimator(shake: View, propertyName: String, finalvalue: Float) =
        ObjectAnimator.ofFloat(shake, propertyName, -270f, finalvalue).apply {
            repeatMode = ValueAnimator.RESTART
            repeatCount = 1
            duration = 400
            interpolator = linearInterpolator
        }

    private fun replaceWithThisFragment(fragment: Class<out Fragment>, args: Bundle?) {
        parentFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .replace(R.id.fcvMain, fragment, args)
            .addToBackStack(null)
            .commit()
    }
}
