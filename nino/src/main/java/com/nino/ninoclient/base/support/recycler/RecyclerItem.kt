package com.nino.ninoclient.base.support.recycler

abstract class RecyclerItem {

  abstract val type: Type

  enum class Type {
    NOTE,
    EMPTY,
    FILE,
    FOLDER,
    INFORMATION,
    TOOLBAR
  }
}
