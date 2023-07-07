package com.panasetskaia.charactersudoku.presentation.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.panasetskaia.charactersudoku.R
import com.panasetskaia.charactersudoku.navigation.NavigationCommand
import com.panasetskaia.charactersudoku.utils.getAppComponent
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

typealias Inflate<T> = (LayoutInflater, ViewGroup?, Boolean) -> T

abstract class BaseFragment<mbinding : ViewBinding, mviewModel : BaseViewModel>(private val inflate: Inflate<mbinding>) :
    Fragment() {

    protected abstract fun onReady(savedInstanceState: Bundle?)

    protected abstract val viewModel: mviewModel

    private var _binding: mbinding? = null
    val binding: mbinding
        get() = _binding ?: throw RuntimeException(getString(R.string.binding_null))

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = inflate.invoke(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeNavigation()
        onReady(savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun observeNavigation() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.navigation.collectLatest {
                        it?.getContentIfNotHandled()?.let { navigationCommand ->
                            handleNavigation(navigationCommand)
                        }
                    }
                }
            }
        }
    }

    private fun handleNavigation(navCommand: NavigationCommand) {
        when (navCommand) {
            is NavigationCommand.ToDirection -> findNavController().navigate(navCommand.directions)
            is NavigationCommand.Back -> findNavController().navigateUp()
        }
    }


}