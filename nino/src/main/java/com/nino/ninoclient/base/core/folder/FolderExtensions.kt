package com.nino.ninoclient.base.core.folder

import com.nino.ninoclient.base.database.room.folder.Folder

fun Folder.isUnsaved(): Boolean {
  return uid == 0
}
