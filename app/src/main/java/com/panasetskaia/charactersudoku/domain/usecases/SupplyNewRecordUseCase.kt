package com.panasetskaia.charactersudoku.domain.usecases

import com.panasetskaia.charactersudoku.domain.CharacterSudokuRepository
import com.panasetskaia.charactersudoku.domain.entities.Record
import javax.inject.Inject

class SupplyNewRecordUseCase @Inject constructor(private val repo: CharacterSudokuRepository) {

    suspend operator fun invoke(record: Record) {
        repo.supplyNewRecord(record)
    }
}