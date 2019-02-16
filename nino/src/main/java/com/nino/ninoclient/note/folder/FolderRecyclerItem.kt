package com.nino.ninoclient.note.folder

import android.content.Context
import android.support.v4.content.ContextCompat
import com.nino.ninoclient.R
import com.nino.ninoclient.database.room.folder.Folder
import com.nino.ninoclient.config.CoreConfig.Companion.notesDb
import com.nino.ninoclient.support.recycler.RecyclerItem
import com.nino.ninoclient.support.ui.ColorUtil

class FolderRecyclerItem(context: Context,
                         val folder: Folder,
                         val click: () -> Unit = {},
                         val longClick: () -> Unit = {},
                         val selected: Boolean = false,
                         contents: Int = -1) : RecyclerItem() {

  val isLightShaded = ColorUtil.isLightColored(folder.color)
  val title = folder.title
  val titleColor = when (isLightShaded) {
    true -> ContextCompat.getColor(context, R.color.dark_secondary_text)
    false -> ContextCompat.getColor(context, R.color.light_primary_text)
  }

  val timestamp = folder.getDisplayTime()
  val timestampColor = when (isLightShaded) {
    true -> ContextCompat.getColor(context, R.color.dark_hint_text)
    false -> ContextCompat.getColor(context, R.color.light_hint_text)
  }

  val usage = if (contents == -1) notesDb.getNoteCountByFolder(folder.uuid) else contents
  val label = when {
    usage == 0 -> context.getString(R.string.folder_card_title)
    usage == 1 -> context.getString(R.string.folder_card_title_single_note)
    else -> context.getString(R.string.folder_card_title_notes, usage)
  }
  val labelColor = when (isLightShaded) {
    true -> ContextCompat.getColor(context, R.color.dark_tertiary_text)
    false -> ContextCompat.getColor(context, R.color.light_secondary_text)
  }


  override val type = RecyclerItem.Type.FOLDER
}
