package com.panasetskaia.charactersudoku.presentation.dict_screen

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.animation.AccelerateInterpolator
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.panasetskaia.charactersudoku.R
import com.panasetskaia.charactersudoku.databinding.FragmentDictionaryBinding
import com.panasetskaia.charactersudoku.domain.entities.ChineseCharacter
import com.panasetskaia.charactersudoku.presentation.base.BaseFragment
import com.panasetskaia.charactersudoku.presentation.common_fragments.RandomOrSelectDialogFragment
import com.panasetskaia.charactersudoku.presentation.game_screen.GameFragment
import com.panasetskaia.charactersudoku.presentation.game_screen.GameViewModel
import com.panasetskaia.charactersudoku.presentation.viewmodels.ViewModelFactory
import com.panasetskaia.charactersudoku.utils.getAppComponent
import com.panasetskaia.charactersudoku.utils.replaceWithThisFragment
import com.panasetskaia.charactersudoku.utils.toast
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

class DictionaryFragment :
    BaseFragment<FragmentDictionaryBinding, ChineseCharacterViewModel>(FragmentDictionaryBinding::inflate) {

    private lateinit var listAdapter: DictionaryListAdapter
    private lateinit var itemTouchCallback: MyItemTouchCallback
    private lateinit var selectedCharacters: List<ChineseCharacter>
    private val mInterpolator = AccelerateInterpolator()
    private var isFabPlayEnabled = false
    private lateinit var searchView: SearchView

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    override val viewModel by viewModels<ChineseCharacterViewModel> { viewModelFactory }
    private val gameViewModel by viewModels<GameViewModel> { viewModelFactory }
    // todo: убери лишнюю вьюмодель

    override fun onAttach(context: Context) {
        super.onAttach(context)
        getAppComponent().inject(this)
    }

    override fun onReady(savedInstanceState: Bundle?) {
        setupMenu()
        setupFab()
        setupRecyclerView()
        collectFlows()
    }

    @SuppressLint("DiscouragedApi")
    private fun setupMenu() {
        binding.appBar.inflateMenu(R.menu.dict_toolbar_menu)
        searchView =
            binding.appBar.menu.findItem(R.id.dict_search_icon).actionView as SearchView
        val searchImgId = androidx.appcompat.R.id.search_button
        val v: ImageView = searchView.findViewById(searchImgId)
        v.setImageResource(R.drawable.ic_search_stroke_and_fill)
        searchView.maxWidth = Integer.MAX_VALUE
        setupSearch()
        binding.appBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.dict_filter_icon -> {
                    parentFragmentManager.beginTransaction()
                        .setReorderingAllowed(true)
                        .add(R.id.fcvMain, ChooseCategoryFragment::class.java, arguments)
                        .addToBackStack(null)
                        .commit()
                    //todo: поменять на bottom sheet
                    true
                }
                else -> true
            }
        }
    }

    private fun setupFab() {
        shakeAdd()
        binding.fabAdd.setOnClickListener {
            viewModel.goToSingleCharacterFragment(null)
        }
        binding.fabPlay.setOnClickListener {
            if (isFabPlayEnabled) {
                gameViewModel.setSelected(selectedCharacters)
                    //todo: можно как-то здесь по-другому отправлять выбранные? да. selected должны храниться в репозитории.
                parentFragmentManager.popBackStack()
                val arguments = Bundle().apply {
                    putString(
                        RandomOrSelectDialogFragment.EXTRA_MODE,
                        RandomOrSelectDialogFragment.MODE_FROM_DICT
                    )
                }
                replaceWithThisFragment(GameFragment::class.java, null)
                addThisFragment(RandomOrSelectDialogFragment::class.java, arguments)

                //todo: поменять навигацию
            } else {
                toast(R.string.not_enough)
            }
        }
        binding.fabDeleteSelected.setOnClickListener {

            val arguments = Bundle().apply {
                putString(
                    ConfirmDeletingDialogFragment.MODE_EXTRA,
                    ConfirmDeletingDialogFragment.MODE_LIST
                )
            }
            parentFragmentManager.beginTransaction()
                .setReorderingAllowed(true)
                .add(R.id.fcvMain, ConfirmDeletingDialogFragment::class.java, arguments)
                .addToBackStack(null)
                .commit()
            //todo: поменять навигацию
        }
    }

    private fun setupRecyclerView() {
        listAdapter = DictionaryListAdapter()
        itemTouchCallback = object : MyItemTouchCallback(
            this, listAdapter,
            viewModel
        ) {}
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
                viewModel.changeIsChosenState(it)
            }
            listAdapter.onCharacterItemClickListener = {
                viewModel.goToSingleCharacterFragment(it.id)
            }
            setupSwipeListener(binding.recyclerViewList)
        }
    }

    private fun collectFlows() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                //todo: фильтрация через вьюмодель
//                if (filter == NO_FILTER) {
                launch {
                    viewModel.dictionaryFlow.collectLatest {
                        if (it.size > 0) {
                            binding.tvDefaultText.visibility = View.GONE
                        } else {
                            binding.tvDefaultText.visibility = View.VISIBLE
                        }
                        listAdapter.submitList(it)
                    }
                }
//                } else {
//                    launch {
//                        viewModel.getDictionaryByCategory(filter).collectLatest {
//                            if (it.size > 0) {
//                                binding.tvDefaultText.visibility = View.GONE
//                            } else {
//                                binding.tvDefaultText.visibility = View.VISIBLE
//                            }
//                            listAdapter.submitList(it)
//                            setupSearch(it)
//                        }
//                    }
//                }
                launch {
                    viewModel.toastFlow.collectLatest {
                        toast(it)
                    }
                }
                launch {
                    viewModel.selectedCharactersSharedFlow.collectLatest { selected ->
                        if (selected.size > 0) {
                            binding.fabDeleteSelected.visibility = View.VISIBLE
                            viewModel.setSelectedForDeleting(selected)
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
            duration = 400
            interpolator = mInterpolator
        }

    private fun addThisFragment(fragment: Class<out Fragment>, args: Bundle?) {
        parentFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .add(R.id.fcvMain, fragment, args)
            .addToBackStack(null)
            .commit()
    }

    private fun setupSearch() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { viewModel.filterByQuery(it) }
                return false
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { viewModel.filterByQuery(it) }
                return false
            }
        })
        searchView.setOnCloseListener {
            viewModel.removeFIlters()
            true
        }
    }
}
