package com.panasetskaia.charactersudoku.application

import android.app.Application
import com.panasetskaia.charactersudoku.di.DaggerAppComponent

class SudokuApplication: Application() {

    companion object {
        lateinit var instance: SudokuApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    val component by lazy {
        DaggerAppComponent.factory().create(this)
    }
}