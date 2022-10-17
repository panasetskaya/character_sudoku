package com.panasetskaia.charactersudoku.data.repository

import com.panasetskaia.charactersudoku.data.database.BoardDbModel
import com.panasetskaia.charactersudoku.data.database.ChineseCharacterDbModel
import com.panasetskaia.charactersudoku.domain.entities.Board
import com.panasetskaia.charactersudoku.domain.entities.ChineseCharacter
import javax.inject.Inject

class SudokuMapper @Inject constructor() {

    fun mapDbChineseCharacterToDomainEntity(model: ChineseCharacterDbModel): ChineseCharacter {
        return ChineseCharacter(
            model.id,
            model.character,
            model.transcription,
            model.translation,
            model.usages,
            model.isChosen
        )
    }

    fun mapDomainChineseCharacterToDbModel(entity: ChineseCharacter): ChineseCharacterDbModel {
        return ChineseCharacterDbModel(
            entity.id,
            entity.character,
            entity.pinyin,
            entity.translation,
            entity.usages,
            entity.isChosen
        )
    }

    fun mapDomainBoardToDbModel(domainBoard: Board): BoardDbModel {
        return BoardDbModel(
            domainBoard.id,
            domainBoard.size,
            domainBoard.cells,
            domainBoard.nineChars,
            domainBoard.timeSpent
        )
    }

    fun mapBoardDbModelToDomainEntity(boardDbModel: BoardDbModel): Board {
        return Board(
            boardDbModel.id,
            boardDbModel.size,
            boardDbModel.cells,
            boardDbModel.nineChars,
            boardDbModel.timeSpent
        )
    }

}