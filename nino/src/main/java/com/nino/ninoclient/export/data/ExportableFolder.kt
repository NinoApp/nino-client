package com.nino.ninoclient.export.data

import com.nino.ninoclient.core.folder.IFolderContainer
import com.nino.ninoclient.database.room.folder.Folder
import java.io.Serializable

class ExportableFolder(
    val uuid: String,
    val title: String,
    val timestamp: Long,
    val updateTimestamp: Long,
    val color: Int
) : Serializable, IFolderContainer {
  override fun timestamp(): Long = timestamp
  override fun updateTimestamp(): Long = updateTimestamp
  override fun color(): Int = color
  override fun title(): String = title
  override fun uuid(): String = uuid

  constructor(folder: Folder) : this(
      folder.uuid ?: "",
      folder.title ?: "",
      folder.timestamp ?: 0,
      folder.updateTimestamp,
      folder.color ?: 0)
}