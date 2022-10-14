package com.panasetskaia.charactersudoku.application

import android.app.Application
import com.panasetskaia.charactersudoku.di.DaggerAppComponent

class SudokuApplication: Application() {

    val component by lazy {
        DaggerAppComponent.factory().create(this)
    }
}