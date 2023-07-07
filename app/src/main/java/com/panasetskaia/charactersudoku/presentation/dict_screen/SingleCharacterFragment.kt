package com.panasetskaia.charactersudoku.presentation.dict_screen

import android.os.Bundle
import android.view.*
import android.view.animation.OvershootInterpolator
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.panasetskaia.charactersudoku.R
import com.panasetskaia.charactersudoku.databinding.FragmentSingleCharacterBinding
import com.panasetskaia.charactersudoku.domain.entities.ChineseCharacter
import com.panasetskaia.charactersudoku.presentation.MainActivity
import com.panasetskaia.charactersudoku.utils.replaceWithThisFragment
import com.panasetskaia.charactersudoku.utils.toast
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SingleCharacterFragment : Fragment(), AdapterView.OnItemSelectedListener {

    private lateinit var viewModel: ChineseCharacterViewModel
    private var chineseCharacterId = NEW_CHAR_ID
    private var mode = MODE_DEFAULT
    private var selectedCategory = ""

    private var _binding: FragmentSingleCharacterBinding? = null
    private val binding: FragmentSingleCharacterBinding
        get() = _binding ?: throw RuntimeException("FragmentSingleCharacterBinding is null")

    private lateinit var adapterForSpinner: SpinnerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        parseParams()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSingleCharacterBinding.inflate(inflater, container, false)
        (activity as AppCompatActivity).setSupportActionBar(binding.appBar)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as MainActivity).characterViewModel
        binding.spinnerCat.onItemSelectedListener = this
        setupMenu()
        if (mode == MODE_ADD) {
            launchModeAdd()
        }
        if (mode == MODE_EDIT) {
            launchModeEdit()
        }
        collectCategories()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun parseParams() {
        val args = requireArguments()
        if (!args.containsKey(EXTRA_MODE)) {
            throw RuntimeException("No param: Mode")
        }
        if (!args.containsKey(EXTRA_CHINESE_ID)) {
            throw RuntimeException("No param: ChineseCharacterId")
        }
        mode = args.getString(EXTRA_MODE) ?: MODE_DEFAULT
        if (mode == MODE_DEFAULT) {
            throw RuntimeException("Mode unknown")
        }
        chineseCharacterId = args.getInt(EXTRA_CHINESE_ID)
    }

    private fun launchModeEdit() {
        binding.addCat.setOnClickListener {
            binding.newCatGroup.isVisible = true
            binding.confirmCat.setOnClickListener {
                binding.etCategory.text?.let {
                    val newCat = it.toString().trim()
                    if (newCat != "") {
                        viewModel.addNewCategory(newCat)
                        selectedCategory = newCat
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
                            selectedCategory = it.category
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
                        viewModel.addNewCategory(newCat)
                        selectedCategory = newCat
                    }
                }
                binding.newCatGroup.isVisible = false
            }
        }
    }

    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.single_character_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.add_icon -> {
                        val chineseChar = binding.etCharacter.text.toString()
                        if (chineseChar.length == 1) {
                            val category = binding.spinnerCat.selectedItem.toString()
                            addCharacter(category)
                            toast(R.string.added)
                            parentFragmentManager.popBackStack()
                            replaceWithThisFragment(DictionaryFragment::class.java, null)
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
        }, viewLifecycleOwner)
    }

    private fun collectCategories() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.categoriesFlow.collectLatest { categories ->
                        val listOfCategories = mutableListOf<String>()
                        for (i in categories) {
                            listOfCategories.add(i.categoryName)
                        }
                        adapterForSpinner = SpinnerAdapter(
                            this@SingleCharacterFragment,
                            R.layout.category_spinner_item,
                            listOfCategories,
                            viewModel
                        )
                        binding.spinnerCat.adapter = adapterForSpinner
                        setSpinnerSelection(selectedCategory)
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
        const val EXTRA_CHINESE_ID = "extra_chinese_id"
        const val EXTRA_MODE = "extra_mode"
        const val MODE_EDIT = "edit"
        const val MODE_ADD = "add"
        const val MODE_DEFAULT = ""
        const val NEW_CHAR_ID = 0
        private const val MIN_LENGTH = 1
    }
}

