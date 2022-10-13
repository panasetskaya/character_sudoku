package com.panasetskaia.charactersudoku.di

import android.app.Application
import com.panasetskaia.charactersudoku.presentation.MainActivity
import com.panasetskaia.charactersudoku.presentation.fragments.DictionaryFragment
import com.panasetskaia.charactersudoku.presentation.fragments.GameFragment
import com.panasetskaia.charactersudoku.presentation.fragments.SingleCharacterFragment
import com.panasetskaia.charactersudoku.presentation.fragments.dialogFragments.ConfirmDeletingDialogFragment
import com.panasetskaia.charactersudoku.presentation.fragments.dialogFragments.ConfirmRefreshFragment
import com.panasetskaia.charactersudoku.presentation.fragments.dialogFragments.RandomOrSelectDialogFragment
import dagger.BindsInstance
import dagger.Component

@SudokuAppScope
@Component(modules = [DataModule::class, ViewModelModule::class])
interface AppComponent {

    fun inject(activity: MainActivity)

    fun inject(fragment: GameFragment)

    fun inject(fragment: DictionaryFragment)

    fun inject(fragment: SingleCharacterFragment)

    fun inject(fragment: ConfirmRefreshFragment)

    fun inject(fragment: ConfirmDeletingDialogFragment)

    fun inject(fragment: RandomOrSelectDialogFragment)

    @Component.Factory
    interface AppComponentFactory {
        fun create(@BindsInstance application: Application): AppComponent
    }
}