package com.example.batu.ninoclient.support.recycler

interface ItemTouchHelperAdapter {
  fun onItemMove(fromPosition: Int, toPosition: Int): Boolean

  fun onItemDismiss(position: Int)
}