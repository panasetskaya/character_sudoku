package com.panasetskaia.charactersudoku.presentation.fragments

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.panasetskaia.charactersudoku.R
import com.panasetskaia.charactersudoku.application.SudokuApplication
import com.panasetskaia.charactersudoku.databinding.FragmentSingleCharacterBinding
import com.panasetskaia.charactersudoku.domain.entities.ChineseCharacter
import com.panasetskaia.charactersudoku.presentation.MainActivity
import com.panasetskaia.charactersudoku.presentation.viewmodels.ChineseCharacterViewModel
import com.panasetskaia.charactersudoku.presentation.viewmodels.ViewModelFactory
import javax.inject.Inject


class SingleCharacterFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val component by lazy {
        (requireActivity().application as SudokuApplication).component
    }

    private lateinit var viewModel: ChineseCharacterViewModel

    private var _binding: FragmentSingleCharacterBinding? = null
    private val binding: FragmentSingleCharacterBinding
        get() = _binding ?: throw RuntimeException("FragmentSingleCharacterBinding is null")

    private var screenMode = SCREEN_MODE_DEFAULT
    private lateinit var chineseCharacter: ChineseCharacter

    override fun onAttach(context: Context) {
        component.inject(this)
        super.onAttach(context)
    }

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
        viewModel = ViewModelProvider(this,viewModelFactory)[ChineseCharacterViewModel::class.java]
        setupMenu()
        when(screenMode){
            MODE_EDIT -> launchEditMode()
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
                        if (chineseChar.length==1) {
                            val pinyin = binding.etPinyin.text.toString()
                            val translation = binding.etTranslation.text.toString()
                            val usages = binding.etUsages.text.toString()
                            val id = if (screenMode == MODE_ADD) 0 else  chineseCharacter.id
                            val newChar = ChineseCharacter(id,chineseChar,pinyin, translation, usages)
                            viewModel.addOrEditCharacter(newChar)
                            Toast.makeText(context, R.string.added, Toast.LENGTH_SHORT).show()
                            val fragment = DictionaryFragment()
                            parentFragmentManager.popBackStack()
                            parentFragmentManager.beginTransaction()
                                .replace(R.id.fcvMain,fragment)
                                .addToBackStack(null)
                                .commit()
                        } else if (chineseChar.length<1) {
                            Toast.makeText(context, R.string.no_char, Toast.LENGTH_SHORT).show()
                        } else {Toast.makeText(context, R.string.too_many, Toast.LENGTH_SHORT).show()}
                        true
                    }
                    else -> true
                }
            }
        }, viewLifecycleOwner)
    }

    private fun launchEditMode() {
        with(binding) {
            etCharacter.setText(chineseCharacter.character)
            etPinyin.setText(chineseCharacter.pinyin)
            etTranslation.setText(chineseCharacter.translation)
            etUsages.setText(chineseCharacter.usages)
        }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val EXTRA_SCREEN_MODE = "extra_mode"
        private const val EXTRA_CHINESE = "extra_chinese"
        private const val MODE_EDIT = "mode_edit"
        private const val MODE_ADD = "mode_add"
        private const val SCREEN_MODE_DEFAULT = ""

        fun newInstanceAddCharacter(): SingleCharacterFragment {
            return SingleCharacterFragment().apply {
                arguments = Bundle().apply {
                    putString(EXTRA_SCREEN_MODE, MODE_ADD)
                }
            }
        }

        fun newInstanceEditCharacter(chineseCharacter: ChineseCharacter): SingleCharacterFragment  {
            return SingleCharacterFragment().apply {
                arguments = Bundle().apply {
                    putString(EXTRA_SCREEN_MODE, MODE_EDIT)
                    putParcelable(EXTRA_CHINESE, chineseCharacter)
                }
            }
        }
    }
}