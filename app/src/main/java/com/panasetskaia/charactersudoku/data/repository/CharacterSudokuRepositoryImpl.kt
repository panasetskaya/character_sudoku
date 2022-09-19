package com.panasetskaia.charactersudoku.data.repository

import com.panasetskaia.charactersudoku.domain.CharacterSudokuRepository
import com.panasetskaia.charactersudoku.domain.entities.Board
import com.panasetskaia.charactersudoku.domain.entities.ChineseCharacter

class CharacterSudokuRepositoryImpl: CharacterSudokuRepository {
    override fun getNineRandomCharFromDict(): List<ChineseCharacter> {
        TODO("Not yet implemented")
    }

    override fun addCharToDict(character: ChineseCharacter) {
        TODO("Not yet implemented")
    }

    override fun deleteCharFromDict(character: ChineseCharacter) {
        TODO("Not yet implemented")
    }

    override fun editCharinDict(character: ChineseCharacter) {
        TODO("Not yet implemented")
    }

    override fun searchForCharacter(character: String): ChineseCharacter {
        TODO("Not yet implemented")
    }

    override fun getWholeDictionary(): List<ChineseCharacter> {
        TODO("Not yet implemented")
    }

    override fun getNewGame(nineCharacters: List<ChineseCharacter>): Board {
        TODO("Not yet implemented")
    }

    override fun saveGame(board: Board) {
        TODO("Not yet implemented")
    }

    override fun getSavedGame(): Board {
        TODO("Not yet implemented")
    }


}