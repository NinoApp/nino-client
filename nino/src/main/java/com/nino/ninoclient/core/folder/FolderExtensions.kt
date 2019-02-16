package com.nino.ninoclient.core.folder

import com.nino.ninoclient.database.room.folder.Folder

fun Folder.isUnsaved(): Boolean {
  return uid == 0
}
