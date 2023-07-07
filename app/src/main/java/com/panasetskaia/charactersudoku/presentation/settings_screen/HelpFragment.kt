package com.panasetskaia.charactersudoku.presentation.settings_screen

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.panasetskaia.charactersudoku.R
import com.panasetskaia.charactersudoku.databinding.FragmentHelpBinding
import com.panasetskaia.charactersudoku.presentation.base.BaseFragment
import com.panasetskaia.charactersudoku.presentation.dict_screen.ChineseCharacterViewModel
import com.panasetskaia.charactersudoku.presentation.dict_screen.DictionaryFragment
import com.panasetskaia.charactersudoku.presentation.game_screen.GameFragment
import com.panasetskaia.charactersudoku.presentation.viewmodels.ViewModelFactory
import com.panasetskaia.charactersudoku.utils.getAppComponent
import javax.inject.Inject

class HelpFragment : BaseFragment<FragmentHelpBinding, HelpViewModel>(FragmentHelpBinding::inflate) {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    override val viewModel by viewModels<HelpViewModel> { viewModelFactory }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        getAppComponent().inject(this)
    }

    override fun onReady(savedInstanceState: Bundle?) {
        binding.appBar.setNavigationOnClickListener {
            viewModel.navigateBack()
        }
    }

}