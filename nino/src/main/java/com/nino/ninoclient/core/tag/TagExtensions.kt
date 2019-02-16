package com.nino.ninoclient.core.tag

import com.nino.ninoclient.database.room.tag.Tag

fun Tag.isUnsaved(): Boolean {
  return uid == 0
}
