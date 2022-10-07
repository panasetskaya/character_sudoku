package com.panasetskaia.charactersudoku.presentation.adapters

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.view.MotionEvent
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.panasetskaia.charactersudoku.R
import com.panasetskaia.charactersudoku.presentation.fragments.DictionaryFragment
import com.panasetskaia.charactersudoku.presentation.fragments.dialogFragments.ConfirmDeletingDialogFragment
import com.panasetskaia.charactersudoku.presentation.viewmodels.ChineseCharacterViewModel

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
        viewModel.isDialogHiddenLiveData.observe(context.viewLifecycleOwner) { isDialogHidden ->
            isDialogShowed = !isDialogHidden
        }
        if (dX > -300f && !isDialogShowed) {
            viewModel.finishDeleting(false)
            val item = adapter.currentList[viewHolder.adapterPosition]
            val fragment = ConfirmDeletingDialogFragment.newInstance(item.id)
            context.parentFragmentManager.beginTransaction()
                .add(R.id.fcvMain,fragment)
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