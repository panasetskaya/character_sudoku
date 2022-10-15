package com.panasetskaia.charactersudoku.data.database

import androidx.room.TypeConverter
import com.panasetskaia.charactersudoku.domain.entities.Cell

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
}