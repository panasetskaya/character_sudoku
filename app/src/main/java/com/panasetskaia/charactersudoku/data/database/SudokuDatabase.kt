package com.panasetskaia.charactersudoku.data.database

import android.app.Application
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.panasetskaia.charactersudoku.data.database.board.BoardDao
import com.panasetskaia.charactersudoku.data.database.board.BoardDbModel
import com.panasetskaia.charactersudoku.data.database.dictionary.CategoryDbModel
import com.panasetskaia.charactersudoku.data.database.dictionary.ChineseCharacterDao
import com.panasetskaia.charactersudoku.data.database.dictionary.ChineseCharacterDbModel
import com.panasetskaia.charactersudoku.data.database.records.RecordDbModel
import com.panasetskaia.charactersudoku.data.database.records.RecordsDao
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@Database(
    entities = [ChineseCharacterDbModel::class, BoardDbModel::class, CategoryDbModel::class, RecordDbModel::class],
    version = 17,
    autoMigrations = [AutoMigration (from = 13, to = 14)],
    exportSchema = true
)
@TypeConverters(SudokuConverters::class)
abstract class SudokuDatabase : RoomDatabase() {

    abstract fun chineseCharacterDao(): ChineseCharacterDao
    abstract fun boardDao(): BoardDao
    abstract fun recordsDao(): RecordsDao

    companion object {

        private var INSTANCE: SudokuDatabase? = null
        private val LOCK = Any()
        private const val DB_NAME = "mandarin_sudoku.db"
        private const val NO_CAT = "-"
        private val initialCategory = CategoryDbModel(0, NO_CAT)

        val MIGRATION_14_17 = object : Migration(14, 17) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE BoardDbModel ADD COLUMN alreadyFinished INTEGER DEFAULT 0 NOT NULL")
                database.execSQL("CREATE TABLE `RecordDbModel` (`id` INTEGER DEFAULT 0 PRIMARY KEY AUTOINCREMENT NOT NULL, `recordTime` INTEGER NOT NULL, `level` TEXT NOT NULL, `date` TEXT NOT NULL)")
            }
        }


        fun getInstance(application: Application): SudokuDatabase {

            synchronized(LOCK) {
                INSTANCE?.let {
                    return it
                }
                val db = Room.databaseBuilder(application, SudokuDatabase::class.java, DB_NAME)
                    .fallbackToDestructiveMigration()
                    .addMigrations(MIGRATION_14_17)
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