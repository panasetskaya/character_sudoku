package com.panasetskaia.charactersudoku.presentation.settings_screen

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.panasetskaia.charactersudoku.databinding.FragmentSignInBinding
import com.panasetskaia.charactersudoku.presentation.base.BaseFragment
import com.panasetskaia.charactersudoku.presentation.viewmodels.ViewModelFactory
import com.panasetskaia.charactersudoku.utils.getAppComponent
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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        getAppComponent().inject(this)
    }

    override fun onReady(savedInstanceState: Bundle?) {
        auth = Firebase.auth
        setListeners()
        collectFlows()
    }

    private fun setListeners() {
        binding.buttonSignIn.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            viewModel.signInWithEmail(auth, email, password, requireActivity())
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