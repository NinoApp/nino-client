package com.nino.ninoclient.base.main.recycler

import com.nino.ninoclient.base.support.recycler.RecyclerItem

class EmptyRecyclerItem : RecyclerItem() {
  override val type = RecyclerItem.Type.EMPTY
}

class GenericRecyclerItem(itemType: Type) : RecyclerItem() {
  override val type = itemType
}
