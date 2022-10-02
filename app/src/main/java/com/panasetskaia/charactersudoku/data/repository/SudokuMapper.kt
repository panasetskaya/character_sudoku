package com.panasetskaia.charactersudoku.data.repository

import com.panasetskaia.charactersudoku.data.database.ChineseCharacterDb
import com.panasetskaia.charactersudoku.domain.entities.ChineseCharacter

class SudokuMapper {

    fun mapDbChineseCharacterToDomainEntity(model: ChineseCharacterDb): ChineseCharacter {
        return ChineseCharacter(
            model.character,
            model.transcription,
            model.translation,
            model.usages,
            model.timesPlayed,
            model.isChosen
        )
    }

    fun mapDomainChineseCharacterToDbModel(entity: ChineseCharacter): ChineseCharacterDb {
        return ChineseCharacterDb(
            0,
            entity.character,
            entity.pinyin,
            entity.translation,
            entity.usages,
            entity.timesPlayed,
            entity.isChosen
        )
    }

}