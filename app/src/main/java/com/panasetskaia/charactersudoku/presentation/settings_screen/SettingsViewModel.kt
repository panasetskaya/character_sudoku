package com.panasetskaia.charactersudoku.presentation.settings_screen

import com.panasetskaia.charactersudoku.presentation.base.BaseViewModel
import javax.inject.Inject

class SettingsViewModel @Inject constructor(): BaseViewModel() {

    fun goToTopRecords() {
        navigate(SettingsFragmentDirections.actionSettingsFragmentToRecordsFragment())
    }

    fun goToHelp() {
        navigate(SettingsFragmentDirections.actionSettingsFragmentToHelpFragment())
    }

    fun goToExportImport() {
        navigate(SettingsFragmentDirections.actionSettingsFragmentToExportFragment())
    }

    override fun deleteThisCategory(cat: String) {
    }

}