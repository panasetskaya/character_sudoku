package com.panasetskaia.charactersudoku.utils

import android.app.Activity
import android.os.Bundle
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

fun Fragment.replaceWithThisFragment(fragment: Class<out Fragment>, args: Bundle?) {
    parentFragmentManager.beginTransaction()
        .setReorderingAllowed(true)
        .replace(R.id.fcvMain, fragment, args)
        .addToBackStack(null)
        .commit()
}

fun AppCompatActivity.replaceWithThisFragment(fragment: Class<out Fragment>, args: Bundle?) {
    supportFragmentManager.beginTransaction()
        .setReorderingAllowed(true)
        .replace(R.id.fcvMain, fragment, args)
        .addToBackStack(null)
        .commit()
}


fun Fragment.toast(@StringRes stringRes: Int) {
    Toast.makeText(requireContext(), stringRes, Toast.LENGTH_SHORT).show()
}

fun Fragment.toast(s: String) {
    Toast.makeText(requireContext(), s, Toast.LENGTH_SHORT).show()
}

fun Fragment.getAppComponent(): AppComponent =
    (requireActivity().application as SudokuApplication).component