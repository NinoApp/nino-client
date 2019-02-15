package com.example.batu.ninoclient.support.option

import android.view.View
import com.example.batu.ninoclient.database.room.tag.Tag

abstract class TagOptionsItemBase(
    val tag: Tag,
    val usages: Int,
    val selected: Boolean,
    val editable: Boolean,
    val editListener: View.OnClickListener? = null,
    val listener: View.OnClickListener) : Comparable<TagOptionsItemBase> {

  abstract fun getIcon(): Int

  abstract fun getEditIcon(): Int

  override fun compareTo(other: TagOptionsItemBase): Int {
    return other.usages.compareTo(usages)
  }
}