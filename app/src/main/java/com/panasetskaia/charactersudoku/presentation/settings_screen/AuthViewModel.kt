package com.panasetskaia.charactersudoku.presentation.settings_screen

import android.app.Activity
import com.google.firebase.auth.FirebaseAuth
import com.panasetskaia.charactersudoku.R
import com.panasetskaia.charactersudoku.presentation.base.BaseViewModel
import com.panasetskaia.charactersudoku.utils.Event
import com.panasetskaia.charactersudoku.utils.myLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class AuthViewModel @Inject constructor(): BaseViewModel()  {

    private val _toastFlow = MutableStateFlow<Event<Int>?>(null)
    val toastFlow: StateFlow<Event<Int>?>
        get() = _toastFlow

    private val _isUserSignedInFlow = MutableStateFlow(false)
    val isUserSignedInFlow: StateFlow<Boolean>
        get() = _isUserSignedInFlow

    override fun deleteThisCategory(cat: String) {
    }

    fun goToSignUp() {
    //todo
    // navigate(SignInFragmentDirections.actionSignInFragmentToSignUpFragment())
    }

    fun signInWithEmail(auth: FirebaseAuth, email: String, password: String, activity: Activity) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
                    myLog("signInWithEmail:success")
                    _isUserSignedInFlow.value = true
                } else {
                    myLog("signInWithEmail:failure: ${task.exception}")
                    _toastFlow.value = Event(R.string.auth_failed)
                    _isUserSignedInFlow.value = false
                }
            }
    }
}