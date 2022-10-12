package com.panasetskaia.charactersudoku.presentation

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.panasetskaia.charactersudoku.databinding.ActivityMainBinding
import com.panasetskaia.charactersudoku.presentation.viewmodels.ChineseCharacterViewModel
import com.panasetskaia.charactersudoku.presentation.viewmodels.GameViewModel

class MainActivity : AppCompatActivity() {

    val gameViewModel by lazy {
        ViewModelProvider(this)[GameViewModel::class.java]
    }

    val characterViewModel by lazy {
        ViewModelProvider(this)[ChineseCharacterViewModel::class.java]
    }

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onStop() {
        characterViewModel.markAllUnselected()
        super.onStop()
    }
}
