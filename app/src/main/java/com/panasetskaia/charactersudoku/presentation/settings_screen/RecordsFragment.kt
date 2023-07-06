package com.panasetskaia.charactersudoku.presentation.settings_screen

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.panasetskaia.charactersudoku.R
import com.panasetskaia.charactersudoku.databinding.FragmentRecordsBinding
import com.panasetskaia.charactersudoku.presentation.MainActivity
import com.panasetskaia.charactersudoku.presentation.game_screen.GameFragment
import com.panasetskaia.charactersudoku.presentation.game_screen.GameViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class RecordsFragment : Fragment() {

    private var _binding: FragmentRecordsBinding? = null
    private val binding: FragmentRecordsBinding
        get() = _binding ?: throw RuntimeException("FragmentRecordsBinding is null")

    private lateinit var gameViewModel: GameViewModel
    private lateinit var listAdapter: RecordListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecordsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        gameViewModel = (activity as MainActivity).gameViewModel
        gameViewModel.getRecords()
        setupMenu()
        setupRecyclerView()
        collectFlows()
    }

    private fun collectFlows() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                gameViewModel.recordsFlow.collectLatest {
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


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupMenu() {
        binding.appBar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
            true
        }
    }
}