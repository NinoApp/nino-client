package com.nino.ninoclient.core.folder

interface IFolderContainer {
  fun uuid(): String

  fun title(): String

  fun timestamp(): Long

  fun updateTimestamp(): Long

  fun color(): Int
}