package com.example.batu.ninoclient.core.tag

import com.example.batu.ninoclient.database.room.tag.Tag

fun Tag.isUnsaved(): Boolean {
  return uid == 0
}
