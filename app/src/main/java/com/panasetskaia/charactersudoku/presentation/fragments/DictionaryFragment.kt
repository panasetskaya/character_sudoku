package com.panasetskaia.charactersudoku.presentation.fragments

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.view.animation.AccelerateInterpolator
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
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
import com.panasetskaia.charactersudoku.presentation.fragments.dialogFragments.RandomOrSelectDialogFragment
import com.panasetskaia.charactersudoku.presentation.viewmodels.ChineseCharacterViewModel
import com.panasetskaia.charactersudoku.presentation.viewmodels.GameViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File

class DictionaryFragment : Fragment() {

    private lateinit var characterViewModel: ChineseCharacterViewModel
    private lateinit var gameViewModel: GameViewModel
    private lateinit var listAdapter: DictionaryListAdapter
    private lateinit var itemTouchCallback: MyItemTouchCallback
    private lateinit var selectedCharacters: List<ChineseCharacter>
    private val mInterpolator = AccelerateInterpolator()
    private var isFabPlayEnabled = false
    private var filter = NO_FILTER

    private var _binding: FragmentDictionaryBinding? = null
    private val binding: FragmentDictionaryBinding
        get() = _binding ?: throw RuntimeException("FragmentDictionaryBinding is null")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        parseParams()
    }

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

    private fun parseParams() {
        val args = requireArguments()
        if (!args.containsKey(FILTER_EXTRA)) {
            throw RuntimeException("No param: filter")
        }
        filter = args.getString(FILTER_EXTRA) ?: NO_FILTER
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
                    R.id.dict_export_import_icon -> {
                        replaceWithThisFragment(ExportFragment::class.java, null)
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
                gameViewModel.setSelected(selectedCharacters)
                parentFragmentManager.popBackStack()
                val arguments = Bundle().apply {
                    putString(RandomOrSelectDialogFragment.EXTRA_MODE,
                        RandomOrSelectDialogFragment.MODE_FROM_DICT)
                }
                replaceWithThisFragment(GameFragment::class.java,null)
                addThisFragment(RandomOrSelectDialogFragment::class.java, arguments)
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.not_enough),
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        }
        binding.fabDeleteSelected.setOnClickListener {
            val arguments = Bundle().apply {
                putString(ConfirmDeletingDialogFragment.MODE_EXTRA, ConfirmDeletingDialogFragment.MODE_LIST)
            }
            parentFragmentManager.beginTransaction()
                .setReorderingAllowed(true)
                .add(R.id.fcvMain, ConfirmDeletingDialogFragment::class.java, arguments)
                .addToBackStack(null)
                .commit()
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
                if (filter== NO_FILTER) {
                    launch {
                        characterViewModel.dictionaryFlow.collectLatest {
                            if (it.size>0) {
                                binding.tvDefaultText.visibility = View.GONE
                            } else {
                                binding.tvDefaultText.visibility = View.VISIBLE
                            }
                            listAdapter.submitList(it)

                        }
                    }
                } else {
                    launch {
                        characterViewModel.getDictionaryByCategory(filter).collectLatest {
                            if (it.size>0) {
                                binding.tvDefaultText.visibility = View.GONE
                            } else {
                                binding.tvDefaultText.visibility = View.VISIBLE
                            }
                            listAdapter.submitList(it)
                        }
                    }
                }
                launch {
                    characterViewModel.selectedCharactersSharedFlow.collectLatest { selected ->
                        if (selected.size > 0) {
                            binding.fabDeleteSelected.visibility = View.VISIBLE
                            characterViewModel.setSelectedForDeleting(selected)
                        } else {
                            binding.fabDeleteSelected.visibility = View.GONE
                        }
                        selectedCharacters = selected
                        if (selected.size == 9) {
                            isFabPlayEnabled = true
                            binding.fabPlay.setImageResource(R.drawable.ic_baseline_play_circle_outline_24)
                            shakePlay()
                        } else {
                            binding.fabPlay.setImageResource(R.drawable.ic_baseline_play_circle_outline_24_gray)
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
//            repeatMode = ValueAnimator.RESTART
//            repeatCount = 1
            duration = 400
            interpolator = mInterpolator
        }

    private fun replaceWithThisFragment(fragment: Class<out Fragment>, args: Bundle?) {
        parentFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .replace(R.id.fcvMain, fragment, args)
            .addToBackStack(null)
            .commit()
    }

    private fun addThisFragment(fragment: Class<out Fragment>, args: Bundle?) {
        parentFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .add(R.id.fcvMain, fragment, args)
            .addToBackStack(null)
            .commit()
    }

    companion object {
        const val FILTER_EXTRA = "filter_extra"
        const val NO_FILTER = "no_filter"
    }
}
