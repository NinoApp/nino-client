package com.example.batu.ninoclient.export.recycler

import com.example.batu.ninoclient.export.sheet.ExportNotesBottomSheet.Companion.FILENAME
import com.example.batu.ninoclient.export.support.AUTO_BACKUP_FILENAME
import com.example.batu.ninoclient.support.recycler.RecyclerItem
import java.io.File

class FileRecyclerItem(val name: String,
                       val date: Long,
                       val path: String,
                       val file: File) : RecyclerItem(), Comparable<FileRecyclerItem> {
  var selected = false

  override val type = Type.FILE

  override fun compareTo(other: FileRecyclerItem): Int {
    if (name.startsWith(FILENAME) || name.startsWith(AUTO_BACKUP_FILENAME)) {
      return -1;
    }
    if (other.name.startsWith(FILENAME) || other.name.startsWith(AUTO_BACKUP_FILENAME)) {
      return 1;
    }
    return 0;
  }

}