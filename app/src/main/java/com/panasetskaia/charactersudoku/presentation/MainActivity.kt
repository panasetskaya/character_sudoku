package com.panasetskaia.charactersudoku.presentation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.panasetskaia.charactersudoku.R
import com.panasetskaia.charactersudoku.domain.SudokuSolver
import kotlin.streams.asSequence

class MainActivity : AppCompatActivity() {

    private val viewModel by lazy {
        ViewModelProvider(this)[MainViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val sequence = SudokuSolver.fromBoardString("..3..82.4.2..64.1.9.......8.8............698.......5....49.7.3.8....1....7..5.4..").solutions().asSequence()
        var result = ""
        sequence.forEach {
            result += it
        }
        Log.d("MY_TAG",result)

    }
}