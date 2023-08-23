package com.panasetskaia.charactersudoku.presentation.settings_screen

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.viewModels
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.panasetskaia.charactersudoku.databinding.FragmentSignUpBinding
import com.panasetskaia.charactersudoku.presentation.base.BaseFragment
import com.panasetskaia.charactersudoku.presentation.viewmodels.ViewModelFactory
import com.panasetskaia.charactersudoku.utils.getAppComponent
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
    }

    private fun setupAuth() {
        auth = Firebase.auth
    }

}