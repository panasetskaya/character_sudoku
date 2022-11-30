package com.panasetskaia.charactersudoku.data.database

import androidx.room.TypeConverter
import com.panasetskaia.charactersudoku.domain.entities.Cell
import com.panasetskaia.charactersudoku.domain.entities.Level

class SudokuConverters {

    @TypeConverter
    fun toListOfCells(s: String): List<Cell> {
        val mutableCells = mutableListOf<Cell>()
        val listOfStringCells = s.split(";")
        for (stringCell in listOfStringCells) {
            if (stringCell!="") {
                val list = stringCell.split(",")
                val row = list[0].trim().toInt()
                val col = list[1].trim().toInt()
                val value = list[2].trim()
                val isFixed = list[3].trim().toBoolean()
                val isDoubtful = list[4].trim().toBoolean()
                mutableCells.add(Cell(row, col, value, isFixed, isDoubtful))
            }
        }
        return mutableCells.toList()
    }

    @TypeConverter
    fun fromListOfCells(cells: List<Cell>): String {
        var s = ""
        for (cell in cells) {
            s += "${cell.row},${cell.col},${cell.value},${cell.isFixed},${cell.isDoubtful};"
        }
        return s
    }

    @TypeConverter
    fun fromListOfStrings(list: List<String>): String {
        var s = ""
        for (string in list) {
            s += "$string,"
        }
        return s
    }

    @TypeConverter
    fun toListOfStrings(s: String): List<String> {
        return s.split(",")
    }

    @TypeConverter
    fun fromLevelToString(level: Level): String {
        return when (level) {
            Level.EASY -> EASY
            Level.MEDIUM -> MEDIUM
            Level.HARD -> HARD
        }
    }

    @TypeConverter
    fun fromStringToLevel(s: String): Level {
        return when (s) {
            EASY -> Level.EASY
            MEDIUM -> Level.MEDIUM
            HARD -> Level.HARD
            else -> Level.EASY
        }
    }

    companion object {
        const val EASY = "easy"
        const val MEDIUM = "medium"
        const val HARD = "hard"
    }
}