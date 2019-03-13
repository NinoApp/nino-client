package com.nino.ninoclient.base.main.utils

import android.content.Context
import android.os.Handler
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.LinearLayout
import android.widget.TextView
import com.nino.ninoclient.R
import com.nino.ninoclient.base.database.room.note.Note
import com.nino.ninoclient.base.core.note.NoteBuilder
import com.nino.ninoclient.base.core.note.NoteState
import com.nino.ninoclient.base.core.note.getNoteState
import com.nino.ninoclient.base.note.save

class MainSnackbar(val layout: LinearLayout, val alwaysRunnable: () -> Unit) {

  val handler = Handler()
  val runnable = {
    layout.visibility = GONE
  }

  val title: TextView = layout.findViewById(R.id.bottom_snackbar_title)
  val action: TextView = layout.findViewById(R.id.bottom_snackbar_action)

  fun triggerSnackbar() {
    handler.removeCallbacks(runnable)
    layout.visibility = VISIBLE
    handler.postDelayed(runnable, 5 * 1000)
  }

  fun softUndo(context: Context, note: Note) {
    if (note.getNoteState() === NoteState.TRASH) {
      undoMoveNoteToTrash(context, note)
      return
    }
    undoDeleteNote(context, note)
  }

  fun undoMoveNoteToTrash(context: Context, note: Note) {
    val backupOfNote = NoteBuilder().copy(note)
    title.setText(R.string.recent_to_trash_message)
    action.setText(R.string.recent_to_trash_undo)
    action.setOnClickListener {
      backupOfNote.save(context)
      alwaysRunnable()
      layout.visibility = GONE
    }
    triggerSnackbar()
  }

  fun undoDeleteNote(context: Context, note: Note) {
    val backupOfNote = NoteBuilder().copy(note)
    title.setText(R.string.recent_to_delete_message)
    action.setText(R.string.recent_to_trash_undo)
    action.setOnClickListener {
      backupOfNote.save(context)
      alwaysRunnable()
      layout.visibility = GONE
    }
    triggerSnackbar()
  }
}