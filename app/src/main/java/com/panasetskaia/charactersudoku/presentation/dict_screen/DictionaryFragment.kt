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
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.panasetskaia.charactersudoku.R
import com.panasetskaia.charactersudoku.databinding.BottomSheetChooseCategoryBinding
import com.panasetskaia.charactersudoku.databinding.BottomSheetChooseLevelBinding
import com.panasetskaia.charactersudoku.databinding.BottomSheetConfirmDeleteBinding
import com.panasetskaia.charactersudoku.databinding.FragmentDictionaryBinding
import com.panasetskaia.charactersudoku.domain.entities.ChineseCharacter
import com.panasetskaia.charactersudoku.domain.entities.Level
import com.panasetskaia.charactersudoku.presentation.base.BaseFragment
import com.panasetskaia.charactersudoku.presentation.root.MainActivity
import com.panasetskaia.charactersudoku.presentation.viewmodels.ViewModelFactory
import com.panasetskaia.charactersudoku.utils.getAppComponent
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
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var spinnerByCategoryAdapter: SpinnerAdapter

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    override val viewModel by viewModels<ChineseCharacterViewModel> { viewModelFactory }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        getAppComponent().inject(this)
    }

    override fun onReady(savedInstanceState: Bundle?) {
        bottomSheetDialog = BottomSheetDialog(requireContext())
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
                    showChooseCategoryBottomDialog()
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
                showChooseLevelBottomDialog()
            } else {
                toast(R.string.not_enough)
            }
        }
        binding.fabDeleteSelected.setOnClickListener {
            showConfirmDeleteBottomDialog(null)
        }
    }

    private fun setupRecyclerView() {
        listAdapter = DictionaryListAdapter()
        itemTouchCallback = object : MyItemTouchCallback(
            listAdapter,
            viewModel
        ) {}
        itemTouchCallback.onCharacterItemSwipeListener = {
            showConfirmDeleteBottomDialog(it)
        }
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
        setNewListForCategoriesSpinner(
            resources.getStringArray(R.array.default_cat_array).toList()
        )
    }

    private fun collectFlows() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
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

                launch {
                    viewModel.categoriesFlow.collectLatest { categories ->
                        spinnerByCategoryAdapter = SpinnerAdapter(
                            this@DictionaryFragment,
                            R.layout.category_spinner_item,
                            categories,
                            viewModel
                        )
                    }
                }
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
            false
        }
    }

    private fun showChooseCategoryBottomDialog() {
        val bottomSheetBinding = BottomSheetChooseCategoryBinding.inflate(layoutInflater)
        with(bottomSheetBinding) {
            bottomSheetDialog.setContentView(root)
            spinnerbyCategory.adapter = spinnerByCategoryAdapter
            applyFiltersButton.setOnClickListener {
                val selectedCategory =
                    if (spinnerbyCategory.selectedItemPosition == 0) {
                        null
                    } else {
                        spinnerbyCategory.selectedItem as String?
                    }
                viewModel.showByCategory(selectedCategory)
                bottomSheetDialog.dismiss()
            }
        }
        bottomSheetDialog.show()
    }

    private fun showChooseLevelBottomDialog() {
        val bottomSheetBinding = BottomSheetChooseLevelBinding.inflate(layoutInflater)
        with(bottomSheetBinding) {
            bottomSheetDialog.setContentView(root)
            applyButton.setOnClickListener {
                val lvl = getLevel(this)
                viewModel.startGameWithSelected(lvl, requireActivity() as MainActivity)
                bottomSheetDialog.dismiss()
            }
        }
        bottomSheetDialog.show()
    }

    private fun showConfirmDeleteBottomDialog(id: Int?) {
        val bottomSheetBinding = BottomSheetConfirmDeleteBinding.inflate(layoutInflater)
        with(bottomSheetBinding) {
            bottomSheetDialog.setContentView(root)
            cancelButton.setOnClickListener {
                bottomSheetDialog.dismiss()
            }
            confirmButton.setOnClickListener {
                if (id != null) {
                    viewModel.deleteCharacterFromDict(id)
                } else {
                    viewModel.deleteSelected()
                }
                bottomSheetDialog.dismiss()
            }
        }
        bottomSheetDialog.show()
    }

    private fun setNewListForCategoriesSpinner(
        list: List<String?>
    ) {
        val listToSubmit = list.filterNotNull()
        spinnerByCategoryAdapter = SpinnerAdapter(
            this,
            R.layout.category_spinner_item,
            listToSubmit,
            viewModel
        )
    }

    private fun getLevel(b: BottomSheetChooseLevelBinding): Level {
        return when (b.radiogroup.checkedRadioButtonId) {
            b.radioEasy.id -> {
                Level.EASY
            }
            b.radioMedium.id -> {
                Level.MEDIUM
            }
            b.radioHard.id -> {
                Level.HARD
            }
            else -> Level.EASY
        }
    }
}
