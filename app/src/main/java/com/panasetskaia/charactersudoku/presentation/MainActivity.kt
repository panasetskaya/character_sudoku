package com.panasetskaia.charactersudoku.presentation

import android.os.Bundle
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
}

//todo: альбомная ориентация!! особенно с твоей customview.

// todo: диалоговые окна: удаление, "здесь нет ироглифа, не можем сохранить",

// todo: "начать заново?" "рэндомные или выбрать?" "загрузить сохраненную игру?"