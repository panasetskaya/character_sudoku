package com.panasetskaia.charactersudoku.data.remote

import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.panasetskaia.charactersudoku.data.database.dictionary.ChineseCharacterDao
import com.panasetskaia.charactersudoku.data.database.dictionary.ChineseCharacterDbModel
import com.panasetskaia.charactersudoku.data.repository.SudokuMapper
import com.panasetskaia.charactersudoku.domain.entities.ChineseCharacter
import com.panasetskaia.charactersudoku.utils.myLog
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RemoteRepo @Inject constructor(private val dao: ChineseCharacterDao) {

    private val exceptionHandler =
        CoroutineExceptionHandler { _, throwable ->
            myLog("Exception while using Firebase Realtime Database: $throwable ${throwable.message}")
//            FirebaseCrashlytics.getInstance()
//                .recordException(throwable)
        }

    private val scope = CoroutineScope(Dispatchers.IO + exceptionHandler)
    fun getRussianHSK1() {
        pullDict(RU_PATH)
    }

    fun getEnglishHSK1() {
        pullDict(ENG_PATH)
    }

    private fun pullDict(path: String) {
        scope.launch {
            val rltimeDatabase = Firebase.database.reference
            rltimeDatabase.child("dictionaries").child(path).get()
                .addOnSuccessListener { snapshot ->
                    val value = snapshot.value as ArrayList<*>
                    for (i in value) {
                        val characterMap = i as HashMap<String, String>
                        val character = characterMap["character"].toString()
                        val pinyin = characterMap["pinyin"].toString()
                        val translation = characterMap["translation"].toString()
                        val usages = characterMap["usages"].toString()
                        val category = characterMap["category"].toString()
                        val id = 0
                        val isChosen = false
                        val char = ChineseCharacterDbModel(
                            id,
                            character,
                            pinyin,
                            translation,
                            usages,
                            isChosen,
                            category
                        )
                        scope.launch {
                            dao.addOrEditCharacter(char)
                        }
                    }
                    myLog("firebase: success")

                }.addOnFailureListener { e ->
                myLog("firebase: Error getting data: $e")
            }
        }
    }

    companion object {
        private const val ENG_PATH = "hsk1_en"
        private const val RU_PATH = "hsk1_ru"
    }
}