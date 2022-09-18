package com.panasetskaia.charactersudoku.data.gameGenerator

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class SudokuGameTest {

    lateinit var SUT: SudokuGame
    val scope = CoroutineScope(Dispatchers.IO)

    @Before
    fun setup() {
        SUT = SudokuGame()
    }

    @Test
    fun testGameMapSize() {
        scope.launch {
            val mapSize = SUT.fillGrid().size
            Assert.assertEquals(1,mapSize)
        }
    }

    @Test
    fun testGameStringsLength() {
        scope.launch {
            val key = SUT.fillGrid().keys.toList()[0]
            val value = SUT.fillGrid().values.toList()[0]
            Assert.assertEquals(true,key.length==value.length)
        }
    }

    @Test
    fun testProvidedDigits() {
        scope.launch {
            val gridRemoved = SUT.fillGrid().values.toList()[0].toList()
            var numberOfDigits = 0
            for (i in gridRemoved) {
                if (i.toString().toInt()!=0) {
                    numberOfDigits++
                }
            }
            Assert.assertEquals(SudokuGame.PROVIDED_DIGITS,numberOfDigits)
        }
    }
    @Test
    fun testOnlyNineNumbersInFullGrid() {
        scope.launch {
            val fullGrid = SUT.fillGrid().keys.toList()[0].toList().toSet()
            Assert.assertEquals(setOf(1,2,3,4,5,6,7,8,9),fullGrid)
        }
    }
}