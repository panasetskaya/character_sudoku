package com.panasetskaia.charactersudoku.presentation.settings_screen

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.panasetskaia.charactersudoku.R
import com.panasetskaia.charactersudoku.databinding.FragmentSettingsBinding
import com.panasetskaia.charactersudoku.presentation.base.BaseFragment
import com.panasetskaia.charactersudoku.presentation.viewmodels.ViewModelFactory
import com.panasetskaia.charactersudoku.utils.getAppComponent
import com.panasetskaia.charactersudoku.utils.toast
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

class SettingsFragment :
    BaseFragment<FragmentSettingsBinding, SettingsViewModel>(FragmentSettingsBinding::inflate) {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    override val viewModel by viewModels<SettingsViewModel> { viewModelFactory }

    private lateinit var auth: FirebaseAuth

    override fun onAttach(context: Context) {
        super.onAttach(context)
        getAppComponent().inject(this)
    }

    override fun onReady(savedInstanceState: Bundle?) {
        auth = Firebase.auth
        setListeners()
        viewModel.checkSignIn(auth)
        collectFlows()
    }

    private fun collectFlows() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.isUserSignedInFlow.collectLatest { signedIn ->
                        if (signedIn) {
                            showCurrentUser()
                        } else {
                            showNewUser()
                        }
                    }
                }
            }
        }
    }

    private fun showCurrentUser() {
        with(binding) {
            buttonLogin.visibility = View.GONE
            buttonLogout.visibility = View.VISIBLE
            ivUserImage.setColorFilter(
                ContextCompat.getColor(requireContext(), R.color.primaryLightColor)
            )
            //todo: get user's email/name from Firebase to show under the pic
        }
    }

    private fun showNewUser() {
        with(binding) {
            buttonLogin.visibility = View.VISIBLE
            buttonLogout.visibility = View.GONE
            ivUserImage.setColorFilter(
                ContextCompat.getColor(requireContext(), R.color.secondaryColor)
            )
        }
    }


    private fun setListeners() {
        with(binding) {
            buttonTopResults.setOnClickListener {
                viewModel.goToTopRecords()
            }
            buttonExport.setOnClickListener {
                viewModel.goToExportImport()
            }
            buttonHelp.setOnClickListener {
                viewModel.goToHelp()
            }
            buttonLogin.setOnClickListener {
                viewModel.goToSingIn()
            }
            buttonLogout.setOnClickListener {
                viewModel.signOut()
            }
        }
    }
}