package com.panasetskaia.charactersudoku.presentation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.panasetskaia.charactersudoku.R
import com.panasetskaia.charactersudoku.domain.SudokuSolver

class MainActivity : AppCompatActivity() {

    private val viewModel by lazy {
        ViewModelProvider(this)[MainViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val stringSudoku = "..3..82.4.2..64.1.9.......8.8............698.......5....49.7.3.8....1....7..5.4.."
        val stringSudoku2 = "..3..82.4.2..64.1.9.......8.8.....1111111698.......5....49.7.3.8....1....7..5.4.."
        val solution = try {
            SudokuSolver.fromBoardString(stringSudoku).solution
        } catch (e: IllegalArgumentException) {
            null
        }
        val exists = (solution!=null)

        Log.d("MY_TAG","Solution exists: $exists. Solution is: $solution. The length of solution: ${solution?.length}")
    }
}