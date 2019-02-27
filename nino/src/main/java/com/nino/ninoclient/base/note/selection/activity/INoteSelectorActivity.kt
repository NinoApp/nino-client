package com.nino.ninoclient.base.note.selection.activity

import com.nino.ninoclient.base.database.room.note.Note

interface INoteSelectorActivity {
  fun onNoteClicked(note: Note)

  fun isNoteSelected(note: Note): Boolean
}