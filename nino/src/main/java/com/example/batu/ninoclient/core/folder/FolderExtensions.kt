package com.example.batu.ninoclient.core.folder

import com.example.batu.ninoclient.database.room.folder.Folder

fun Folder.isUnsaved(): Boolean {
  return uid == 0
}
