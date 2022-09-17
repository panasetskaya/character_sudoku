package com.panasetskaia.charactersudoku.domain

interface CharacterSudokuRepository {

    fun getNineRandomCharFromDict(): List<ChineseCharacter>

    fun addCharToDict(character: ChineseCharacter)

    fun deleteCharFromDict(character: ChineseCharacter)

    fun editCharinDict(character: ChineseCharacter)

    fun searchForCharacter(character: String)

    fun getWholeDictionary(): List<ChineseCharacter>

    fun getGame(): Map<String, String>

}