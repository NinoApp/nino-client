package com.example.batu.ninoclient.note.tag

import android.view.View
import com.example.batu.ninoclient.R
import com.example.batu.ninoclient.database.room.tag.Tag
import com.example.batu.ninoclient.support.option.TagOptionsItemBase

class TagOptionsItem(
    tag: Tag,
    usages: Int = 0,
    selected: Boolean = false,
    editable: Boolean = false,
    editListener: View.OnClickListener? = null,
    listener: View.OnClickListener) : TagOptionsItemBase(tag, usages, selected, editable, editListener, listener) {

  override fun getIcon(): Int = when (selected) {
    true -> R.drawable.ic_action_label
    false -> R.drawable.ic_action_label_unselected
  }

  override fun getEditIcon(): Int = R.drawable.ic_edit_white_48dp
}