package com.panasetskaia.charactersudoku.presentation.root

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.panasetskaia.charactersudoku.R
import com.panasetskaia.charactersudoku.application.SudokuApplication
import com.panasetskaia.charactersudoku.databinding.ActivityMainBinding
import com.panasetskaia.charactersudoku.presentation.dict_screen.ChineseCharacterViewModel
import com.panasetskaia.charactersudoku.presentation.viewmodels.ViewModelFactory
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

//    @Inject
//    lateinit var viewModelFactory: ViewModelFactory
//
//    private val component by lazy {
//        (application as SudokuApplication).component
//    }
//
//    private lateinit var _navControl: NavController
//    val navController: NavController
//        get() = _navControl
//
//    val characterViewModel by lazy {
//        ViewModelProvider(this, viewModelFactory)[ChineseCharacterViewModel::class.java]
//    }
//    //todo: сделать свою viewmodel
//
    private lateinit var binding: ActivityMainBinding
//
    override fun onCreate(savedInstanceState: Bundle?) {
//        component.inject(this)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupBottomNavigationBar()
    }


    private fun setupBottomNavigationBar() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.activity_main_nav_host_container) as NavHostFragment
        binding.activityMainBottomNavigation.setupWithNavController(navHostFragment.navController)

    }

    fun switchToDict() {
        binding.activityMainBottomNavigation.selectedItemId = R.id.dictionary_nav_graph
    }
//
//    override fun onStop() {
//        characterViewModel.markAllUnselected()
//        super.onStop()
//    }

//    fun goToGameWithSelected(lvl: Int) {
//        _navControl.navigate(DictionaryFragmentDirections.actionDictionaryFragmentToGameFragment(lvl))
//    }
//
//    fun goToDict() {
//        _navControl.navigate(R.id.dictionaryFragment)
//    }
}
