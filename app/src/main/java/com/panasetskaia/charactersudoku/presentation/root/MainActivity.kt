package com.panasetskaia.charactersudoku.presentation.root

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.panasetskaia.charactersudoku.R
import com.panasetskaia.charactersudoku.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {

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

    fun switchToGame() {
        binding.activityMainBottomNavigation.selectedItemId = R.id.gameFragment
    }

}
