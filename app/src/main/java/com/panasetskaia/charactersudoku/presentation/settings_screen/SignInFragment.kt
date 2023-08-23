package com.panasetskaia.charactersudoku.presentation.settings_screen

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.panasetskaia.charactersudoku.R
import com.panasetskaia.charactersudoku.databinding.FragmentSignInBinding
import com.panasetskaia.charactersudoku.presentation.base.BaseFragment
import com.panasetskaia.charactersudoku.presentation.viewmodels.ViewModelFactory
import com.panasetskaia.charactersudoku.utils.getAppComponent
import com.panasetskaia.charactersudoku.utils.myLog
import com.panasetskaia.charactersudoku.utils.toast
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

class SignInFragment : BaseFragment<FragmentSignInBinding, AuthViewModel>(
    FragmentSignInBinding::inflate
) {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    override val viewModel by viewModels<AuthViewModel> { viewModelFactory }

    private lateinit var auth: FirebaseAuth
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest

    private val googleRegisterResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            when (result.resultCode) {
                Activity.RESULT_OK -> {
                    viewModel.signInWithGoogle(auth, oneTapClient, result.data, requireActivity())
                }
            }
        }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        getAppComponent().inject(this)
    }

    override fun onReady(savedInstanceState: Bundle?) {
        setupAuth()
        viewModel.checkSignIn(auth)
        setListeners()
        collectFlows()
    }

    private fun setupAuth() {
        auth = Firebase.auth
        oneTapClient = Identity.getSignInClient(requireActivity())
        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(getString(R.string.web_client_id))
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .setAutoSelectEnabled(true)
            .build()
    }

    private fun setListeners() {
        binding.buttonSignIn.setOnClickListener {
            launchEmailSignIn()
        }
        binding.googleSignInButton.setOnClickListener {
            launchGoogleSignIn()
        }
        binding.tvSignUp.setOnClickListener {
            viewModel.goToSignUp()
        }
        binding.appBar.setNavigationOnClickListener {
            viewModel.navigateBack()
        }
    }


    private fun launchEmailSignIn() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        viewModel.signInWithEmail(auth, email, password, requireActivity())
    }

    private fun launchGoogleSignIn() {
        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener(requireActivity()) { result ->
                try {
                    val intentSenderRequest = IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
                    googleRegisterResultLauncher.launch(intentSenderRequest)
                } catch (e: IntentSender.SendIntentException) {
                    myLog("Couldn't start One Tap UI: ${e.localizedMessage}")
                }
            }
            .addOnFailureListener(requireActivity()) { e ->
                myLog(e.localizedMessage)
            }

    }

    private fun collectFlows() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.toastFlow.collectLatest { event ->
                        event?.getContentIfNotHandled()?.let { stringResource ->
                            toast(stringResource)
                        }
                    }
                }
                launch {
                    viewModel.isUserSignedInFlow.collectLatest { signedIn ->
                        if (signedIn) {
                            viewModel.navigateBack()
                        }
                    }
                }
            }
        }
    }
}
