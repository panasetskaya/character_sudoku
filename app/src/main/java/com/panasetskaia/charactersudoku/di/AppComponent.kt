package com.panasetskaia.charactersudoku.di

import android.app.Application
import com.panasetskaia.charactersudoku.presentation.MainActivity
import com.panasetskaia.charactersudoku.presentation.base.BaseFragment
import com.panasetskaia.charactersudoku.presentation.dict_screen.DictionaryFragment
import com.panasetskaia.charactersudoku.presentation.game_screen.GameFragment
import com.panasetskaia.charactersudoku.presentation.settings_screen.ExportFragment
import com.panasetskaia.charactersudoku.presentation.settings_screen.HelpFragment
import com.panasetskaia.charactersudoku.presentation.settings_screen.RecordsFragment
import com.panasetskaia.charactersudoku.presentation.settings_screen.SettingsFragment
import dagger.BindsInstance
import dagger.Component

@SudokuAppScope
@Component(modules = [DataModule::class, ViewModelModule::class])
interface AppComponent {

    fun inject(activity: MainActivity)

    fun inject(fragment: SettingsFragment)

    fun inject(fragment: ExportFragment)

    fun inject(fragment: HelpFragment)

    fun inject(fragment: RecordsFragment)

    fun inject(fragment: DictionaryFragment)

    fun inject(fragment: GameFragment)

    @Component.Factory
    interface AppComponentFactory {
        fun create(@BindsInstance application: Application): AppComponent
    }
}