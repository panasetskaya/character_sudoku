package com.panasetskaia.charactersudoku.application

import android.app.Application

class SudokuApplication: Application() {

    companion object {
        lateinit var instance: SudokuApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

}