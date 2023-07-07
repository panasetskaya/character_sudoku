package com.panasetskaia.charactersudoku.presentation.settings_screen

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.viewModels
import com.panasetskaia.charactersudoku.R
import com.panasetskaia.charactersudoku.databinding.FragmentSettingsBinding
import com.panasetskaia.charactersudoku.presentation.base.BaseFragment
import com.panasetskaia.charactersudoku.presentation.viewmodels.ViewModelFactory
import com.panasetskaia.charactersudoku.utils.getAppComponent
import com.panasetskaia.charactersudoku.utils.toast
import javax.inject.Inject

class SettingsFragment :
    BaseFragment<FragmentSettingsBinding, SettingsViewModel>(FragmentSettingsBinding::inflate) {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    override val viewModel by viewModels<SettingsViewModel> { viewModelFactory }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        getAppComponent().inject(this)
    }

    override fun onReady(savedInstanceState: Bundle?) {
        setListeners()
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
                toast(R.string.soon)
            }
        }
    }
}