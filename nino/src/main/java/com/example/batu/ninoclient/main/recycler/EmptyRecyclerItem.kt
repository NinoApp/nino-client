package com.example.batu.ninoclient.main.recycler

import com.example.batu.ninoclient.support.recycler.RecyclerItem

class EmptyRecyclerItem : RecyclerItem() {
  override val type = RecyclerItem.Type.EMPTY
}

class GenericRecyclerItem(itemType: Type) : RecyclerItem() {
  override val type = itemType
}
