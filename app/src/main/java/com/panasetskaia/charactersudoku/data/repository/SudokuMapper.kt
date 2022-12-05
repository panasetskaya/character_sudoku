package com.panasetskaia.charactersudoku.data.repository

import com.panasetskaia.charactersudoku.data.database.BoardDbModel
import com.panasetskaia.charactersudoku.data.database.CategoryDbModel
import com.panasetskaia.charactersudoku.data.database.ChineseCharacterDbModel
import com.panasetskaia.charactersudoku.data.database.RecordDbModel
import com.panasetskaia.charactersudoku.domain.entities.Board
import com.panasetskaia.charactersudoku.domain.entities.Category
import com.panasetskaia.charactersudoku.domain.entities.ChineseCharacter
import com.panasetskaia.charactersudoku.domain.entities.Record
import javax.inject.Inject

class SudokuMapper @Inject constructor() {

    fun mapDbChineseCharacterToDomainEntity(model: ChineseCharacterDbModel): ChineseCharacter {
        return ChineseCharacter(
            model.id,
            model.character,
            model.transcription,
            model.translation,
            model.usages,
            model.isChosen,
            model.category
        )
    }

    fun mapDomainChineseCharacterToDbModel(entity: ChineseCharacter): ChineseCharacterDbModel {
        return ChineseCharacterDbModel(
            entity.id,
            entity.character,
            entity.pinyin,
            entity.translation,
            entity.usages,
            entity.isChosen,
            entity.category
        )
    }

    fun mapDomainBoardToDbModel(domainBoard: Board): BoardDbModel {
        return BoardDbModel(
            domainBoard.id,
            domainBoard.size,
            domainBoard.cells,
            domainBoard.nineChars,
            domainBoard.timeSpent,
            domainBoard.alreadyFinished
        )
    }

    fun mapBoardDbModelToDomainEntity(boardDbModel: BoardDbModel): Board {
        return Board(
            boardDbModel.id,
            boardDbModel.size,
            boardDbModel.cells,
            boardDbModel.nineChars,
            boardDbModel.timeSpent,
            boardDbModel.alreadyFinished
        )
    }

    fun mapDomainCategoryToDbModel(category: Category): CategoryDbModel {
        return CategoryDbModel(
            category.id,
            category.categoryName
        )
    }

    fun mapDbModelToDomainCategory(category: CategoryDbModel): Category {
        return Category(
            category.id,
            category.categoryName
        )
    }

    fun mapDbModelToDomainRecord(dbModel: RecordDbModel): Record {
        return Record(
            dbModel.id,
            dbModel.recordTime,
            dbModel.level,
            dbModel.date
        )
    }

    fun mapDomainEntityToRecordDbModel(record: Record): RecordDbModel {
        return RecordDbModel(
            0,
            record.recordTime,
            record.level,
            record.date
        )
    }

}