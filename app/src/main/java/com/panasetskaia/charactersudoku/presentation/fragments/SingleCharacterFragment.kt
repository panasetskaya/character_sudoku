package com.panasetskaia.charactersudoku.presentation.fragments

import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.panasetskaia.charactersudoku.R
import com.panasetskaia.charactersudoku.databinding.FragmentSingleCharacterBinding
import com.panasetskaia.charactersudoku.domain.entities.ChineseCharacter
import com.panasetskaia.charactersudoku.presentation.MainActivity
import com.panasetskaia.charactersudoku.presentation.adapters.SpinnerAdapter
import com.panasetskaia.charactersudoku.presentation.viewmodels.ChineseCharacterViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SingleCharacterFragment : Fragment(){

    private lateinit var viewModel: ChineseCharacterViewModel
    private var screenMode = SCREEN_MODE_DEFAULT
    private lateinit var chineseCharacter: ChineseCharacter

    private var _binding: FragmentSingleCharacterBinding? = null
    private val binding: FragmentSingleCharacterBinding
        get() = _binding ?: throw RuntimeException("FragmentSingleCharacterBinding is null")

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
        setupMenu()
        collectFlows()
        when (screenMode) {
            MODE_EDIT -> launchEditMode()
        }
        binding.addCat.setOnClickListener {
            binding.newCatGroup.isVisible = true
            binding.confirmCat.setOnClickListener {
                binding.etCategory.text?.let {
                    if (it.toString()!="") {
                        viewModel.addNewCategory(it.toString())
                    }
                }
                binding.newCatGroup.isVisible = false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun parseParams() {
        val args = requireArguments()
        if (!args.containsKey(EXTRA_SCREEN_MODE)) {
            throw RuntimeException("No screen mode param")
        }
        val mode = args.getString(EXTRA_SCREEN_MODE)
        if (mode != MODE_EDIT && mode != MODE_ADD) {
            throw RuntimeException("Unknown param: screen mode $mode")
        }
        screenMode = mode
        if (screenMode == MODE_EDIT) {
            if (!args.containsKey(EXTRA_CHINESE)) {
                throw RuntimeException("No param: ChineseCharacter")
            }
            chineseCharacter = args.getParcelable(EXTRA_CHINESE)!!
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
                            val pinyin = binding.etPinyin.text.toString()
                            val translation = binding.etTranslation.text.toString()
                            val usages = binding.etUsages.text.toString()
                            val id = if (screenMode == MODE_ADD) 0 else chineseCharacter.id
                            val category = if (binding.tiNewCategory.isVisible && binding.etCategory.text != null
                                && binding.etCategory.text.toString()!="") {
                                binding.etCategory.text.toString()
                            } else {
                                binding.spinnerCat.selectedItem.toString()
                            }
                            val newChar =
                                ChineseCharacter(id, chineseChar, pinyin, translation, usages, category = category)
                            viewModel.addOrEditCharacter(newChar)
                            Toast.makeText(context, R.string.added, Toast.LENGTH_SHORT).show()
                            parentFragmentManager.popBackStack()
                            replaceWithThisFragment(DictionaryFragment::class.java)
                        } else if (chineseChar.length < MIN_LENGTH) {
                            Toast.makeText(context, R.string.no_char, Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, R.string.too_many, Toast.LENGTH_SHORT).show()
                        }
                        true
                    }
                    else -> true
                }
            }
        }, viewLifecycleOwner)
    }

    private fun collectFlows() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.categoriesFlow.collectLatest { categories ->
                        val listOfCategories = mutableListOf<String>()
                        for (i in categories) {
                            listOfCategories.add(i.categoryName)
                        }
                        val adapter = SpinnerAdapter(
                            this@SingleCharacterFragment,
                            R.layout.category_spinner_item,
                            listOfCategories
                        )
                        adapter.setDropDownViewResource(R.layout.category_spinner_item)
                        binding.spinnerCat.adapter = adapter
                    }
                }
            }
        }
    }


    private fun launchEditMode() {
        with(binding) {
            etCharacter.setText(chineseCharacter.character)
            etPinyin.setText(chineseCharacter.pinyin)
            etTranslation.setText(chineseCharacter.translation)
            etUsages.setText(chineseCharacter.usages)
        }
    }

    private fun replaceWithThisFragment(fragment: Class<out Fragment>) {
        parentFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .replace(R.id.fcvMain, fragment, null)
            .addToBackStack(null)
            .commit()
    }

    companion object {
        const val EXTRA_SCREEN_MODE = "extra_mode"
        const val EXTRA_CHINESE = "extra_chinese"
        const val MODE_EDIT = "mode_edit"
        const val MODE_ADD = "mode_add"
        private const val SCREEN_MODE_DEFAULT = ""
        private const val MIN_LENGTH = 1
    }
}