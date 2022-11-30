package com.panasetskaia.charactersudoku.domain.usecases

import com.panasetskaia.charactersudoku.domain.CharacterSudokuRepository
import com.panasetskaia.charactersudoku.domain.entities.Record
import javax.inject.Inject

class GetTopFifteenRecordsUseCase @Inject constructor(private val repo: CharacterSudokuRepository) {

    suspend operator fun invoke(): List<Record> {
        return repo.getAllRecords()
    }
}