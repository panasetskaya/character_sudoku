package com.panasetskaia.charactersudoku.di

import android.app.Application
import com.panasetskaia.charactersudoku.presentation.MainActivity
import dagger.BindsInstance
import dagger.Component

@SudokuAppScope
@Component(modules = [DataModule::class, ViewModelModule::class])
interface AppComponent {

    fun inject(activity: MainActivity)

    @Component.Factory
    interface AppComponentFactory {
        fun create(@BindsInstance application: Application): AppComponent
    }
}