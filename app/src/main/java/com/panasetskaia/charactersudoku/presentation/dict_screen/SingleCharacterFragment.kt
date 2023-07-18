package com.panasetskaia.charactersudoku.presentation.dict_screen

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.AdapterView
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import com.panasetskaia.charactersudoku.R
import com.panasetskaia.charactersudoku.databinding.FragmentSingleCharacterBinding
import com.panasetskaia.charactersudoku.domain.entities.ChineseCharacter
import com.panasetskaia.charactersudoku.presentation.base.BaseFragment
import com.panasetskaia.charactersudoku.presentation.viewmodels.ViewModelFactory
import com.panasetskaia.charactersudoku.utils.getAppComponent
import com.panasetskaia.charactersudoku.utils.toast
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

class SingleCharacterFragment :
    BaseFragment<FragmentSingleCharacterBinding, ChineseCharacterViewModel>(
        FragmentSingleCharacterBinding::inflate
    ), AdapterView.OnItemSelectedListener {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    override val viewModel by viewModels<ChineseCharacterViewModel> { viewModelFactory }

    private var chineseCharacterId = NEW_CHAR_ID
    private var newCategory = INITIAL_CAT

    private val navArgs by navArgs<SingleCharacterFragmentArgs>()

    private lateinit var adapterForSpinner: SpinnerAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        getAppComponent().inject(this)
    }

    override fun onReady(savedInstanceState: Bundle?) {
        binding.spinnerCat.onItemSelectedListener = this
        collectCategories()
        setupMenu()
        parseParams()
    }

    private fun parseParams() {
        val modeArg = navArgs.mode
        if (modeArg == MODE_ADD) {
            launchModeAdd()
        }
        if (modeArg == MODE_EDIT) {
            chineseCharacterId = navArgs.characterId
            launchModeEdit()
        }
    }

    private fun launchModeEdit() {
        binding.addCat.setOnClickListener {
            binding.newCatGroup.isVisible = true
            binding.confirmCat.setOnClickListener {
                binding.etCategory.text?.let {
                    val newCat = it.toString().trim()
                    if (newCat != "") {
                        viewModel.addNewCategory(newCat)
                        addCharacter(newCat)
                    }
                }
                binding.newCatGroup.isVisible = false
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.getOneCharacterById(chineseCharacterId).collectLatest {
                        with(binding) {
                            etCharacter.setText(it.character)
                            etPinyin.setText(it.pinyin)
                            etTranslation.setText(it.translation)
                            etUsages.setText(it.usages)
                            setSpinnerSelection(it.category)
                            tvBigCharacter.text = it.character
                            tvBigCharacter.animate().apply {
                                translationX(70f)
                                duration = 500
                                interpolator = OvershootInterpolator()
                            }
                        }
                    }
                }


            }
        }
    }

    private fun launchModeAdd() {
        binding.tvBigCharacter.visibility = View.GONE
        binding.addCat.setOnClickListener {
            binding.newCatGroup.isVisible = true
            binding.confirmCat.setOnClickListener {
                binding.etCategory.text?.let {
                    val newCat = it.toString().trim()
                    if (newCat != "") {
                        newCategory = newCat
                        viewModel.addNewCategory(newCat)
                    }
                }
                binding.newCatGroup.isVisible = false
            }
        }
    }

    private fun setupMenu() {

        binding.appBar.inflateMenu(R.menu.single_character_menu)
        binding.appBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.add_icon -> {
                    val chineseChar = binding.etCharacter.text.toString()
                    if (chineseChar.length == 1) {
                        val category = binding.spinnerCat.selectedItem.toString()
                        addCharacter(category)
                        toast(R.string.added)
                        viewModel.navigateBack()
                    } else if (chineseChar.length < MIN_LENGTH) {
                        toast(R.string.no_char)
                    } else {
                        toast(R.string.too_many)
                    }
                    true
                }
                else -> true
            }
        }
    }

    private fun collectCategories() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.categoriesFlow.collectLatest { categories ->
                        adapterForSpinner = SpinnerAdapter(
                            this@SingleCharacterFragment,
                            R.layout.category_spinner_item,
                            categories,
                            viewModel
                        )
                        binding.spinnerCat.adapter = adapterForSpinner
                        if (newCategory!=INITIAL_CAT) {
                            setSpinnerSelection(newCategory)
                        }
                    }
                }
            }
        }
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
    }

    private fun addCharacter(cat: String) {
        val chinese = binding.etCharacter.text.toString()
        val pinyin = binding.etPinyin.text.toString()
        val translation = binding.etTranslation.text.toString()
        val usages = binding.etUsages.text.toString()
        val id = chineseCharacterId
        val newChar =
            ChineseCharacter(id, chinese, pinyin, translation, usages, category = cat)
        viewModel.addOrEditCharacter(newChar)
    }

    private fun setSpinnerSelection(cat: String) {
        val position = adapterForSpinner.getPosition(cat)
        if (position != -1) {
            binding.spinnerCat.setSelection(position)
        }
    }

    companion object {
        const val MODE_EDIT = "edit"
        const val MODE_ADD = "add"
        const val NEW_CHAR_ID = 0
        private const val MIN_LENGTH = 1
        private const val INITIAL_CAT = ""
    }
}

