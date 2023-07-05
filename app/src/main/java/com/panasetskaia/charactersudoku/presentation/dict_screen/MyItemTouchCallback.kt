package com.panasetskaia.charactersudoku.presentation.dict_screen

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.os.Bundle
import android.view.MotionEvent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.panasetskaia.charactersudoku.R
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

open class MyItemTouchCallback(
    private val context: DictionaryFragment,
    private val adapter: DictionaryListAdapter,
    private val viewModel: ChineseCharacterViewModel
) : ItemTouchHelper.Callback() {

    private var swipeback = false
    var isDialogShowed = false

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        return makeMovementFlags(0, ItemTouchHelper.LEFT)
    }

    override fun convertToAbsoluteDirection(flags: Int, layoutDirection: Int): Int {
        if (swipeback) {
            swipeback = false
            return 0
        }
        return super.convertToAbsoluteDirection(flags, layoutDirection)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean = false

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        setTouchListener(recyclerView)
        context.viewLifecycleOwner.lifecycleScope.launch {
            context.viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isDialogHiddenStateFlow.collectLatest { isDialogHidden ->
                    isDialogShowed = !isDialogHidden
                }
            }
        }
        if (dX > -300f && !isDialogShowed) {
            viewModel.finishDeleting(false)
            val item = adapter.currentList[viewHolder.adapterPosition]
            val arguments = Bundle().apply {
                putInt(ConfirmDeletingDialogFragment.ITEM_ID_EXTRA, item.id)
                putString(ConfirmDeletingDialogFragment.MODE_EXTRA, ConfirmDeletingDialogFragment.MODE_SINGLE)
            }
            context.parentFragmentManager.beginTransaction()
                .setReorderingAllowed(true)
                .add(R.id.fcvMain, ConfirmDeletingDialogFragment::class.java, arguments)
                .addToBackStack(null)
                .commit()
        }
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setTouchListener(recyclerView: RecyclerView) {
        recyclerView.setOnTouchListener { view, motionEvent ->
            swipeback =
                motionEvent.action == MotionEvent.ACTION_CANCEL || motionEvent.action == MotionEvent.ACTION_UP
            view.performClick()
            false
        }
    }
}