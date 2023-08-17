package com.panasetskaia.charactersudoku.di

import androidx.lifecycle.ViewModel
import com.panasetskaia.charactersudoku.presentation.dict_screen.ChineseCharacterViewModel
import com.panasetskaia.charactersudoku.presentation.game_screen.GameViewModel
import com.panasetskaia.charactersudoku.presentation.settings_screen.AuthViewModel
import com.panasetskaia.charactersudoku.presentation.settings_screen.HelpViewModel
import com.panasetskaia.charactersudoku.presentation.settings_screen.RecordsViewModel
import com.panasetskaia.charactersudoku.presentation.settings_screen.SettingsViewModel
import com.panasetskaia.charactersudoku.presentation.settings_screen.SignInFragment
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface ViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(GameViewModel::class)
    fun bindGameViewModel(impl: GameViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ChineseCharacterViewModel::class)
    fun bindChineseCharacterViewModel(impl: ChineseCharacterViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SettingsViewModel::class)
    fun bindSettingsViewModel(impl: SettingsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(HelpViewModel::class)
    fun bindHelpViewModel(impl: HelpViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(RecordsViewModel::class)
    fun bindRecordsViewModel(impl: RecordsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AuthViewModel::class)
    fun bindAuthViewModel(impl: AuthViewModel): ViewModel
}