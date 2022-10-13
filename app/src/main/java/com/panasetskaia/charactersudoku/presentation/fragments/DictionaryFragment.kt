package com.panasetskaia.charactersudoku.presentation.fragments

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.animation.LinearInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.panasetskaia.charactersudoku.R
import com.panasetskaia.charactersudoku.application.SudokuApplication
import com.panasetskaia.charactersudoku.databinding.FragmentDictionaryBinding
import com.panasetskaia.charactersudoku.domain.entities.ChineseCharacter
import com.panasetskaia.charactersudoku.presentation.MainActivity
import com.panasetskaia.charactersudoku.presentation.adapters.DictionaryListAdapter
import com.panasetskaia.charactersudoku.presentation.adapters.MyItemTouchCallback
import com.panasetskaia.charactersudoku.presentation.viewmodels.ChineseCharacterViewModel
import com.panasetskaia.charactersudoku.presentation.viewmodels.GameViewModel
import com.panasetskaia.charactersudoku.presentation.viewmodels.ViewModelFactory
import javax.inject.Inject

class DictionaryFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val component by lazy {
        (requireActivity().application as SudokuApplication).component
    }

    private lateinit var characterViewModel: ChineseCharacterViewModel
    private lateinit var gameViewModel: GameViewModel
    private lateinit var listAdapter: DictionaryListAdapter
    private lateinit var itemTouchCallback: MyItemTouchCallback
    private lateinit var selectedCharacters: List<ChineseCharacter>

    private val linearInterpolator = LinearInterpolator()

    private var _binding: FragmentDictionaryBinding? = null
    private val binding: FragmentDictionaryBinding
        get() = _binding ?: throw RuntimeException("FragmentDictionaryBinding is null")

    override fun onAttach(context: Context) {
        component.inject(this)
        super.onAttach(context)
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
        characterViewModel = ViewModelProvider(this,viewModelFactory)[ChineseCharacterViewModel::class.java]
        gameViewModel = ViewModelProvider(this,viewModelFactory)[GameViewModel::class.java]

        setupMenu()
        setupFab()
        setupRecyclerView()
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
                val fragment = SingleCharacterFragment.newInstanceEditCharacter(it)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fcvMain, fragment)
                    .addToBackStack(null)
                    .commit()
            }
            setupSwipeListener(binding.recyclerViewList)
        }
        characterViewModel.dictionaryLiveData.observe(viewLifecycleOwner) {
            listAdapter.submitList(it)
        }
    }

    private fun setupSwipeListener(rv: RecyclerView) {
        val itemTouchHelper = ItemTouchHelper(itemTouchCallback)
        itemTouchHelper.attachToRecyclerView(rv)
    }

    private fun setupFab() {
        shakeAdd()
        characterViewModel.selectedCharactersLiveData.observe(viewLifecycleOwner) { selected ->
            if (selected.size==9) {
                binding.fabPlay.isEnabled = true
                selectedCharacters = selected
                shakePlay()
            } else {
                binding.fabPlay.isEnabled = false
            }
        }
        binding.fabAdd.setOnClickListener {
            val fragment = SingleCharacterFragment.newInstanceAddCharacter()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fcvMain, fragment)
                .addToBackStack(null)
                .commit()
        }
        binding.fabPlay.setOnClickListener {
            gameViewModel.getGameWithSelected(selectedCharacters)
            characterViewModel.markAllUnselected()
            parentFragmentManager.popBackStack()
            val fragment = GameFragment.newInstance()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fcvMain, fragment)
                .addToBackStack(null)
                .commit()
        }
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
                        val fragment = GameFragment.newInstance()
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fcvMain, fragment)
                            .addToBackStack(null)
                            .commit()
                        true
                    }
                    R.id.dict_help_icon -> {
                        val fragment = HelpFragment.newInstance()
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fcvMain, fragment)
                            .addToBackStack(null)
                            .commit()
                        true
                    }
                    else -> true
                }
            }
        }, viewLifecycleOwner)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun shakeAdd(){
        AnimatorSet().apply {
            play(shakeAnimator(binding.fabAdd, "rotation", 0f),)
            start()
        }
    }

    private fun shakePlay() {
        AnimatorSet().apply {
            play(shakeAnimator(binding.fabPlay, "rotation", 0f),)
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

    companion object {
        fun newInstance() = DictionaryFragment()
    }

}
