package com.example.batu.ninoclient.core.note

import com.github.bijoysingh.starter.util.TextUtils
import com.google.gson.Gson
import com.example.batu.ninoclient.database.room.note.Note
import com.example.batu.ninoclient.core.format.Format
import com.example.batu.ninoclient.core.format.FormatBuilder
import com.example.batu.ninoclient.note.saveWithoutSync
import java.util.*

fun Note.isUnsaved(): Boolean {
  return this.uid === null || this.uid == 0
}

fun Note.isEqual(note: Note): Boolean {
  return TextUtils.areEqualNullIsEmpty(this.state, note.state)
      && TextUtils.areEqualNullIsEmpty(this.description, note.description)
      && TextUtils.areEqualNullIsEmpty(this.uuid, note.uuid)
      && TextUtils.areEqualNullIsEmpty(this.tags, note.tags)
      && this.timestamp.toLong() == note.timestamp.toLong()
      && this.color.toInt() == note.color.toInt()
      && this.locked == note.locked
      && this.pinned == note.pinned
}

/**************************************************************************************
 ********************************* Object Functions ***********************************
 **************************************************************************************/

fun Note.getFormats(): List<Format> {
  return FormatBuilder().getFormats(this.description)
}

fun Note.getNoteState(): NoteState {
  try {
    return NoteState.valueOf(this.state)
  } catch (exception: Exception) {
    return NoteState.DEFAULT
  }
}

fun Note.getMeta(): NoteMeta {
  try {
    return Gson().fromJson<NoteMeta>(this.meta, NoteMeta::class.java) ?: NoteMeta()
  } catch (e: Exception) {
    return NoteMeta()
  }
}

fun Note.getReminder(): NoteReminder? {
  return getMeta().reminder
}

fun Note.getReminderV2(): Reminder? {
  return getMeta().reminderV2
}

fun Note.setReminderV2(reminder: Reminder) {
  val noteMeta = NoteMeta()
  noteMeta.reminderV2 = reminder
  meta = Gson().toJson(noteMeta)
}

fun Note.getTagUUIDs(): MutableSet<String> {
  val tags = if (this.tags == null) "" else this.tags
  val split = tags.split(",")
  return HashSet<String>(split)
}