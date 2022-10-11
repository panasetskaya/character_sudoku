package com.panasetskaia.charactersudoku.data.database

import android.app.Application
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [ChineseCharacterDbModel::class, BoardDbModel::class], version = 6, exportSchema = false)
@TypeConverters(SudokuConverters::class)
abstract class SudokuDatabase: RoomDatabase() {

    abstract fun chineseCharacterDao(): ChineseCharacterDao
    abstract fun boardDao(): BoardDao

    companion object {

        private var INSTANCE: SudokuDatabase? = null
        private val LOCK = Any()
        private const val DB_NAME = "mandarin_sudoku.db"

        fun getInstance(application: Application): SudokuDatabase {

            synchronized(LOCK){
                INSTANCE?.let {
                    return it
                }
                val db = Room.databaseBuilder(application, SudokuDatabase::class.java, DB_NAME)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = db
                return db
            }
        }
    }

}