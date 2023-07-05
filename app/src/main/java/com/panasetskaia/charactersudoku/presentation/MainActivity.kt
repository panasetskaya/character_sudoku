package com.panasetskaia.charactersudoku.presentation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.panasetskaia.charactersudoku.R
import com.panasetskaia.charactersudoku.application.SudokuApplication
import com.panasetskaia.charactersudoku.databinding.ActivityMainBinding
import com.panasetskaia.charactersudoku.presentation.dict_screen.DictionaryFragment
import com.panasetskaia.charactersudoku.presentation.game_screen.GameFragment
import com.panasetskaia.charactersudoku.presentation.dict_screen.ChineseCharacterViewModel
import com.panasetskaia.charactersudoku.presentation.game_screen.GameViewModel
import com.panasetskaia.charactersudoku.presentation.settings_screen.SettingsFragment
import com.panasetskaia.charactersudoku.presentation.viewmodels.ViewModelFactory
import com.panasetskaia.charactersudoku.utils.replaceWithThisFragment
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val component by lazy {
        (application as SudokuApplication).component
    }

    val gameViewModel by lazy {
        ViewModelProvider(this, viewModelFactory)[GameViewModel::class.java]
    }

    val characterViewModel by lazy {
        ViewModelProvider(this,viewModelFactory)[ChineseCharacterViewModel::class.java]
    }

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        component.inject(this)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            if (item.itemId!=binding.bottomNavigation.selectedItemId) {
                when (item.itemId) {
                    R.id.action_game -> {
                        goToGame()
                    }
                    R.id.action_dictionary -> {
                        goToDict()
                    }
                    R.id.action_profile -> {
                        goToProfile()
                    }
                }
            }
            true
        }
    }

    override fun onStop() {
        characterViewModel.markAllUnselected()
        super.onStop()
    }

    private fun goToGame() {
        supportFragmentManager.popBackStack()
        replaceWithThisFragment(GameFragment::class.java, null)
    }

    private fun goToProfile() {
        supportFragmentManager.popBackStack()
        replaceWithThisFragment(SettingsFragment::class.java, null)
    }

    private fun goToDict() {
        val arguments = Bundle().apply {
            putString(
                DictionaryFragment.FILTER_EXTRA,
                DictionaryFragment.NO_FILTER
            )
        }
        supportFragmentManager.popBackStack()
        replaceWithThisFragment(DictionaryFragment::class.java, arguments)
    }


}
