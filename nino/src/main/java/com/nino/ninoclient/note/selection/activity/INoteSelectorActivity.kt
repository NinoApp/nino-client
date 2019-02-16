package com.nino.ninoclient.note.selection.activity

import com.nino.ninoclient.database.room.note.Note

interface INoteSelectorActivity {
  fun onNoteClicked(note: Note)

  fun isNoteSelected(note: Note): Boolean
}