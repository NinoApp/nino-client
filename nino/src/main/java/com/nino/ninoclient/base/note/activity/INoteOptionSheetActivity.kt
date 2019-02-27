package com.nino.ninoclient.base.note.activity

import com.nino.ninoclient.base.database.room.note.Note
import com.nino.ninoclient.base.core.note.NoteState

interface INoteOptionSheetActivity {
  fun updateNote(note: Note)

  fun markItem(note: Note, state: NoteState)

  fun moveItemToTrashOrDelete(note: Note)

  fun notifyTagsChanged(note: Note)

  fun getSelectMode(note: Note): String

  fun notifyResetOrDismiss()

  fun lockedContentIsHidden(): Boolean
}