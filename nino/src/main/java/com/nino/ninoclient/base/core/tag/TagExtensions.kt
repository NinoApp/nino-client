package com.nino.ninoclient.base.core.tag

import com.nino.ninoclient.base.database.room.tag.Tag

fun Tag.isUnsaved(): Boolean {
  return uid == 0
}
