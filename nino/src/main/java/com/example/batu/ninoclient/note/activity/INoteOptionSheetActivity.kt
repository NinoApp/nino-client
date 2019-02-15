package com.example.batu.ninoclient.note.activity

import com.example.batu.ninoclient.database.room.note.Note
import com.example.batu.ninoclient.core.note.NoteState

interface INoteOptionSheetActivity {
  fun updateNote(note: Note)

  fun markItem(note: Note, state: NoteState)

  fun moveItemToTrashOrDelete(note: Note)

  fun notifyTagsChanged(note: Note)

  fun getSelectMode(note: Note): String

  fun notifyResetOrDismiss()

  fun lockedContentIsHidden(): Boolean
}