package com.panasetskaia.charactersudoku.data.database

import android.app.Application
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@Database(
entities = [ChineseCharacterDbModel::class, BoardDbModel::class, CategoryDbModel::class],
version = 16,
//autoMigrations = [AutoMigration (from = 13, to = 14)], //todo: здесь, похоже, надо будет добавить ручные миграции с 14 версии
exportSchema = true)
@TypeConverters(SudokuConverters::class)
abstract class SudokuDatabase: RoomDatabase() {

    abstract fun chineseCharacterDao(): ChineseCharacterDao
    abstract fun boardDao(): BoardDao

    companion object {

        private var INSTANCE: SudokuDatabase? = null
        private val LOCK = Any()
        private const val DB_NAME = "mandarin_sudoku.db"
        private const val NO_CAT = "-"
        private val initialCategory = CategoryDbModel(0,NO_CAT)

        val MIGRATION_15_16 = object : Migration(15, 16) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE BoardDbModel ADD COLUMN alreadyFinished INTEGER DEFAULT 0 NOT NULL")
            }
        }

        fun getInstance(application: Application): SudokuDatabase {

            synchronized(LOCK){
                INSTANCE?.let {
                    return it
                }
                val db = Room.databaseBuilder(application, SudokuDatabase::class.java, DB_NAME)
                    .fallbackToDestructiveMigration()
                    .addMigrations(MIGRATION_15_16)
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