package com.panasetskaia.charactersudoku.di

import androidx.lifecycle.ViewModel
import com.panasetskaia.charactersudoku.presentation.dict_screen.ChineseCharacterViewModel
import com.panasetskaia.charactersudoku.presentation.game_screen.GameViewModel
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
}