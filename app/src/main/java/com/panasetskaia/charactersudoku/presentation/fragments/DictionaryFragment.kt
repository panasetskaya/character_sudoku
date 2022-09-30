package com.panasetskaia.charactersudoku.presentation.fragments

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.*
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.panasetskaia.charactersudoku.R
import com.panasetskaia.charactersudoku.databinding.FragmentDictionaryBinding
import com.panasetskaia.charactersudoku.presentation.MainActivity
import com.panasetskaia.charactersudoku.presentation.viewmodels.GameViewModel

class DictionaryFragment : Fragment() {

    private lateinit var viewModel: GameViewModel

    private val linearInterpolator = LinearInterpolator()

    private var _binding: FragmentDictionaryBinding? = null
    private val binding: FragmentDictionaryBinding
        get() = _binding ?: throw RuntimeException("FragmentDictionaryBinding is null")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDictionaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as MainActivity).viewModel
        setupMenu()
        requireActivity().runOnUiThread {
            AnimatorSet().apply {
                play(shakeAnimator(binding.fabAdd, "rotation"))
                start()
            }
        }
        binding.fabAdd.setOnClickListener {
            binding.root.findNavController()
                .navigate(R.id.action_dictionaryFragment_to_singleCharacterFragment)
        }

    }

    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.dict_toolbar_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.search_icon -> {
                        Toast.makeText(context, "Will search", Toast.LENGTH_SHORT).show() //todo
                        true
                    }
                    R.id.sudoku_icon -> {
                        binding.root.findNavController()
                            .navigate(R.id.action_dictionaryFragment_to_gameFragment)
                        true
                    }
                    R.id.dict_help_icon -> {
                        Toast.makeText(context, "Will go to Help", Toast.LENGTH_SHORT).show() //todo
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

    private fun shakeAnimator(shake: View, propertyName: String) =
        ObjectAnimator.ofFloat(shake, propertyName, -180f, 90f).apply {
            repeatMode = ValueAnimator.RESTART
            repeatCount = 1
            duration = 400
            interpolator = linearInterpolator
        }


}