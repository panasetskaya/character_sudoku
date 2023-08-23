package com.panasetskaia.charactersudoku.presentation.settings_screen

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.panasetskaia.charactersudoku.presentation.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class SettingsViewModel @Inject constructor(): BaseViewModel() {

    private val _isUserSignedInFlow = MutableStateFlow(false)
    val isUserSignedInFlow: StateFlow<Boolean>
        get() = _isUserSignedInFlow

    fun checkSignIn(auth: FirebaseAuth) {
        val currentUser = auth.currentUser
        _isUserSignedInFlow.value = currentUser != null
    }

    fun signOut() {
        Firebase.auth.signOut()
        _isUserSignedInFlow.value = false
    }


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

    fun goToSingIn() {
        navigate(SettingsFragmentDirections.actionSettingsFragmentToSignInFragment())
    }

}