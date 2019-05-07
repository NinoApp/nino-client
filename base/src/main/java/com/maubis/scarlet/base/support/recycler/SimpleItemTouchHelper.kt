package com.maubis.scarlet.base.support.recycler

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ItemTouchHelper

class SimpleItemTouchHelper(private val mAdapter: ItemTouchHelperAdapter) : ItemTouchHelper.Callback() {

  override fun isLongPressDragEnabled(): Boolean = true

  override fun isItemViewSwipeEnabled(): Boolean = true

  override fun getMovementFlags(recyclerView: androidx.recyclerview.widget.RecyclerView,
                                viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder): Int {
    val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
    val swipeFlags = 0 //ItemTouchHelper.START | ItemTouchHelper.END;
    return ItemTouchHelper.Callback.makeMovementFlags(dragFlags, swipeFlags)
  }

  override fun onMove(recyclerView: androidx.recyclerview.widget.RecyclerView,
                      viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder,
                      target: androidx.recyclerview.widget.RecyclerView.ViewHolder): Boolean {
    mAdapter.onItemMove(viewHolder.adapterPosition, target.adapterPosition)
    return true
  }

  override fun onSwiped(viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder, direction: Int) {
    mAdapter.onItemDismiss(viewHolder.adapterPosition)
  }
}