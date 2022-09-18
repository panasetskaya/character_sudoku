package com.panasetskaia.charactersudoku.data.repository

import com.panasetskaia.charactersudoku.domain.CharacterSudokuRepository
import com.panasetskaia.charactersudoku.domain.ChineseCharacter

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

    override fun getGame(): Map<String, String> {
        TODO("Not yet implemented")
    }
}