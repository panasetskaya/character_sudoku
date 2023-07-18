package com.panasetskaia.charactersudoku.presentation.settings_screen

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.panasetskaia.charactersudoku.databinding.FragmentRecordsBinding
import com.panasetskaia.charactersudoku.presentation.base.BaseFragment
import com.panasetskaia.charactersudoku.presentation.game_screen.GameViewModel
import com.panasetskaia.charactersudoku.presentation.viewmodels.ViewModelFactory
import com.panasetskaia.charactersudoku.utils.getAppComponent
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

class RecordsFragment : BaseFragment<FragmentRecordsBinding,RecordsViewModel>(FragmentRecordsBinding::inflate) {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    override val viewModel by viewModels<RecordsViewModel> { viewModelFactory }

    private lateinit var listAdapter: RecordListAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        getAppComponent().inject(this)
    }

    override fun onReady(savedInstanceState: Bundle?) {
        viewModel.getRecords()
        setupMenu()
        setupRecyclerView()
        collectFlows()
    }

    private fun collectFlows() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.recordsFlow.collectLatest {
                    listAdapter.submitList(it)
                }
            }
        }
    }

    private fun setupRecyclerView() {
        listAdapter = RecordListAdapter(requireActivity())
        listAdapter.stateRestorationPolicy =
            RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        binding.recyclerViewRecords.adapter = listAdapter
    }

    private fun setupMenu() {
        binding.appBar.setNavigationOnClickListener {
            viewModel.navigateBack()
        }
    }
}