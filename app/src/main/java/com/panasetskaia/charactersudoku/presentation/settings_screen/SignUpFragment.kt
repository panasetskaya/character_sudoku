package com.panasetskaia.charactersudoku.presentation.settings_screen

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.panasetskaia.charactersudoku.databinding.FragmentSignUpBinding
import com.panasetskaia.charactersudoku.presentation.base.BaseFragment
import com.panasetskaia.charactersudoku.presentation.viewmodels.ViewModelFactory
import com.panasetskaia.charactersudoku.utils.getAppComponent
import com.panasetskaia.charactersudoku.utils.toast
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

class SignUpFragment : BaseFragment<FragmentSignUpBinding, AuthViewModel>(
    FragmentSignUpBinding::inflate
) {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    override val viewModel: AuthViewModel by viewModels { viewModelFactory }

    private lateinit var auth: FirebaseAuth


    override fun onAttach(context: Context) {
        super.onAttach(context)
        getAppComponent().inject(this)
    }

    override fun onReady(savedInstanceState: Bundle?) {
        setupAuth()
        collectFlows()
        setupListeners()
    }

    private fun setupAuth() {
        auth = Firebase.auth
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

    private fun setupListeners() {
        binding.buttonSignUp.setOnClickListener {
            val password = checkPassword()
            password?.let {
                val email = binding.etRegisterEmail.text.toString().trim()
                if (email.isNotEmpty()) {
                    viewModel.signupWithEmail(auth,email,it,requireActivity())
                }
            }
        }
        binding.appBar.setNavigationOnClickListener {
            viewModel.navigateBack()
        }
    }


    private fun checkPassword(): String? {
        val password = binding.etRegisterPassword.text.toString().trim()
        val confirm = binding.etConfirmPassword.text.toString().trim()
        return if (password!=confirm) {
            viewModel.showWrongPasswordToast()
            null
        } else password
    }

}