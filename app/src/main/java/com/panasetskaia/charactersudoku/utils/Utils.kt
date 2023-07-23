package com.panasetskaia.charactersudoku.utils

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.panasetskaia.charactersudoku.R
import com.panasetskaia.charactersudoku.application.SudokuApplication
import com.panasetskaia.charactersudoku.di.AppComponent
import java.util.concurrent.TimeUnit

fun Long.formatToTime(context: Activity): String {
    val hours = TimeUnit.MILLISECONDS.toHours(this)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(this) - TimeUnit.HOURS.toMinutes(hours)
    val seconds =
        TimeUnit.MILLISECONDS.toSeconds(this) - TimeUnit.HOURS.toSeconds(hours) - TimeUnit.MINUTES.toSeconds(
            minutes
        )

    val timePassed = String.format(
        context.getString(R.string.time_formatted),
        hours, minutes, seconds
    )
    return timePassed
}

fun Fragment.toast(@StringRes stringRes: Int) {
    Toast.makeText(requireContext(), stringRes, Toast.LENGTH_SHORT).show()
}

fun Fragment.toast(s: String) {
    Toast.makeText(requireContext(), s, Toast.LENGTH_SHORT).show()
}

fun Fragment.getAppComponent(): AppComponent =
    (requireActivity().application as SudokuApplication).component

fun String.simplifyPinyin(): String {
    var result = ""
    val complicatedAList = listOf('ā','à','á','ǎ','â')
    val complicatedUList = listOf('û','ǖ', 'ū','ǚ','ǔ','ú','ǘ','ǜ','ù','ü')
    val complicatedIList = listOf('î','ī','í','ǐ','ì')
    val complicatedOList = listOf('ô','ó','ō','ǒ','ò')
    val complicatedEList = listOf('ê','ē','é','ě','è')
    for (i in this.lowercase()) {
        result += if (i in complicatedAList) {
            'a'
        } else if (i in complicatedUList) {
            'u'
        } else if (i in complicatedEList) {
            'e'
        } else if (i in complicatedOList) {
            'o'
        } else if (i in complicatedIList) {
            'i'
        } else {
            i
        }
    }
    return result
}

fun myLog(s: String) {
    Log.d("MYMYMY", s)
}

