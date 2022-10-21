package com.panasetskaia.charactersudoku.data.database

import android.app.Application
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.concurrent.thread

@Database(entities = [ChineseCharacterDbModel::class, BoardDbModel::class, CategoryDbModel::class], version = 11, exportSchema = false)
@TypeConverters(SudokuConverters::class)
abstract class SudokuDatabase: RoomDatabase() {

    abstract fun chineseCharacterDao(): ChineseCharacterDao
    abstract fun boardDao(): BoardDao

    companion object {

        private var INSTANCE: SudokuDatabase? = null
        private val LOCK = Any()
        private const val DB_NAME = "mandarin_sudoku.db"
        private const val NO_CAT = "no category"
        private val initialCategory = CategoryDbModel(0,NO_CAT)

        fun getInstance(application: Application): SudokuDatabase {

            synchronized(LOCK){
                INSTANCE?.let {
                    return it
                }
                val db = Room.databaseBuilder(application, SudokuDatabase::class.java, DB_NAME)
                    .fallbackToDestructiveMigration()
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            GlobalScope.launch {
                                getInstance(application).chineseCharacterDao()
                                    .addOrEditCategory(initialCategory)

                            }
                        }
                    })
                    .build()
                INSTANCE = db
                return db
            }
        }
    }

}