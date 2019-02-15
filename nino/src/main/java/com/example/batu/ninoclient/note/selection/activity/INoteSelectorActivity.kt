package com.example.batu.ninoclient.note.selection.activity

import com.example.batu.ninoclient.database.room.note.Note

interface INoteSelectorActivity {
  fun onNoteClicked(note: Note)

  fun isNoteSelected(note: Note): Boolean
}