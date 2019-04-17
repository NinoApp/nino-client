package com.nino.ninoclient.base.note.folder

import com.nino.ninoclient.R
import com.nino.ninoclient.base.database.room.folder.Folder

class FolderOptionsItem(
    val folder: Folder,
    val usages: Int = 0,
    val selected: Boolean = false,
    val editable: Boolean = false,
    val editListener: () -> Unit = {},
    val listener: () -> Unit = {}) {

  fun getIcon(): Int = when (selected) {
    true -> R.drawable.ic_folder
    false -> R.drawable.ic_folder
  }

  fun getEditIcon(): Int = R.drawable.ic_edit_white_48dp
}