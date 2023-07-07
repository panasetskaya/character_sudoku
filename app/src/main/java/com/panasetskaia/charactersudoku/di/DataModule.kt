package com.panasetskaia.charactersudoku.di

import android.app.Application
import com.panasetskaia.charactersudoku.data.database.board.BoardDao
import com.panasetskaia.charactersudoku.data.database.dictionary.ChineseCharacterDao
import com.panasetskaia.charactersudoku.data.database.records.RecordsDao
import com.panasetskaia.charactersudoku.data.database.SudokuDatabase
import com.panasetskaia.charactersudoku.data.repository.CharacterSudokuRepositoryImpl
import com.panasetskaia.charactersudoku.domain.CharacterSudokuRepository
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module
interface DataModule {

    @SudokuAppScope
    @Binds
    fun bindRepo(repoImpl: CharacterSudokuRepositoryImpl): CharacterSudokuRepository

    companion object {

        @SudokuAppScope
        @Provides
        fun provideBoardDao(application: Application): BoardDao {
            return SudokuDatabase.getInstance(application).boardDao()
        }

        @SudokuAppScope
        @Provides
        fun provideCharacterDao(application: Application): ChineseCharacterDao {
            return SudokuDatabase.getInstance(application).chineseCharacterDao()
        }

        @SudokuAppScope
        @Provides
        fun provideRecordsDao(application: Application): RecordsDao {
            return SudokuDatabase.getInstance(application).recordsDao()
        }

    }
}