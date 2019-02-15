package com.example.batu.ninoclient.note.recycler

import android.content.Context
import android.support.v4.content.ContextCompat
import com.maubis.markdown.Markdown
import com.example.batu.ninoclient.R
import com.example.batu.ninoclient.core.note.getNoteState
import com.example.batu.ninoclient.core.note.getReminderV2
import com.example.batu.ninoclient.database.room.note.Note
import com.example.batu.ninoclient.note.*
import com.example.batu.ninoclient.note.creation.sheet.sEditorMarkdownEnabled
import com.example.batu.ninoclient.settings.sheet.LineCountBottomSheet
import com.example.batu.ninoclient.settings.sheet.UISettingsOptionsBottomSheet.Companion.sMarkdownEnabledHome
import com.example.batu.ninoclient.support.recycler.RecyclerItem
import com.example.batu.ninoclient.support.ui.ColorUtil

class NoteRecyclerItem(context: Context, val note: Note) : RecyclerItem() {

  private val isLightShaded = ColorUtil.isLightColored(note.color)
  private val isMarkdownEnabled = sEditorMarkdownEnabled && sMarkdownEnabledHome
  val lineCount = LineCountBottomSheet.getDefaultLineCount()

  val title = note.getMarkdownTitle(isMarkdownEnabled)
  val titleColor = when (isLightShaded) {
    true -> ContextCompat.getColor(context, R.color.dark_tertiary_text)
    false -> ContextCompat.getColor(context, R.color.light_primary_text)
  }

  val description = note.getLockedText(isMarkdownEnabled)
  val descriptionColor = when (isLightShaded) {
    true -> ContextCompat.getColor(context, R.color.dark_tertiary_text)
    false -> ContextCompat.getColor(context, R.color.light_primary_text)
  }

  val state = note.getNoteState()
  val indicatorColor = when (isLightShaded) {
    true -> ContextCompat.getColor(context, R.color.dark_tertiary_text)
    false -> ContextCompat.getColor(context, R.color.light_tertiary_text)
  }

  val hasReminder = note.getReminderV2() !== null
  val actionBarIconColor = when (isLightShaded) {
    true -> ContextCompat.getColor(context, R.color.dark_secondary_text)
    false -> ContextCompat.getColor(context, R.color.light_secondary_text)
  }

  val tagsSource = note.getTagString()
  val tags = Markdown.renderSegment(tagsSource)
  val tagsColor = when (isLightShaded) {
    true -> ContextCompat.getColor(context, R.color.dark_tertiary_text)
    false -> ContextCompat.getColor(context, R.color.light_secondary_text)
  }

  val timestamp = note.getDisplayTime()
  val timestampColor = when (isLightShaded) {
    true -> ContextCompat.getColor(context, R.color.dark_hint_text)
    false -> ContextCompat.getColor(context, R.color.light_hint_text)
  }

  val imageSource = note.getImageFile()
  val disableBackup = note.disableBackup

  override val type = RecyclerItem.Type.NOTE
}
