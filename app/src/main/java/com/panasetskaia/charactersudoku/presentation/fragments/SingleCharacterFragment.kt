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

class SingleCharacterFragment : Fragment(), AdapterView.OnItemSelectedListener {

    private lateinit var viewModel: ChineseCharacterViewModel
    private var chineseCharacterId = NEW_CHAR_ID

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
        setupMenu()
        collectFlows()
        binding.spinnerCat.onItemSelectedListener = this
        binding.addCat.setOnClickListener {
            binding.newCatGroup.isVisible = true
            binding.confirmCat.setOnClickListener {
                binding.etCategory.text?.let {
                    if (it.toString() != "") {
                        viewModel.addNewCategory(it.toString())
                        addCharacter()
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
        if (!args.containsKey(EXTRA_CHINESE_ID)) {
            throw RuntimeException("No param: ChineseCharacterId")
        }
        chineseCharacterId = args.getInt(EXTRA_CHINESE_ID)
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
                            addCharacter()
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
                        adapterForSpinner = SpinnerAdapter(
                            this@SingleCharacterFragment,
                            R.layout.category_spinner_item,
                            listOfCategories,
                            viewModel
                        )
                        binding.spinnerCat.adapter = adapterForSpinner
                    }
                }
                launch {
                    viewModel.getOneCharacterById(chineseCharacterId).collectLatest {
                        with(binding) {
                            etCharacter.setText(it.character)
                            etPinyin.setText(it.pinyin)
                            etTranslation.setText(it.translation)
                            etUsages.setText(it.usages)
                            val position = adapterForSpinner.getPosition(it.category)
                            spinnerCat.setSelection(position)
                        }
                    }
                }
            }
        }
    }


    private fun replaceWithThisFragment(fragment: Class<out Fragment>) {
        parentFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .replace(R.id.fcvMain, fragment, null)
            .addToBackStack(null)
            .commit()
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        p0?.setSelection(p2)
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        TODO("Not yet implemented")
    }

    private fun addCharacter() {
        val chinese = binding.etCharacter.text.toString()
        val pinyin = binding.etPinyin.text.toString()
        val translation = binding.etTranslation.text.toString()
        val usages = binding.etUsages.text.toString()
        val id = if (chineseCharacterId == NEW_CHAR_ID) 0 else chineseCharacterId
        val category = if (binding.tiNewCategory.isVisible && binding.etCategory.text != null
            && binding.etCategory.text.toString() != ""
        ) {
            binding.etCategory.text.toString()
        } else if (binding.spinnerCat.selectedItem!=null){
            binding.spinnerCat.selectedItem.toString()
        } else {
            null
        }
        val newChar =
            ChineseCharacter(id, chinese, pinyin, translation, usages, category = category)
        viewModel.addOrEditCharacter(newChar)
    }

    companion object {
        const val EXTRA_CHINESE_ID = "extra_chinese_id"
        const val NEW_CHAR_ID = -1
        private const val MIN_LENGTH = 1
    }
}

