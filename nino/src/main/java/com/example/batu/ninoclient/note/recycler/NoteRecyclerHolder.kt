package com.example.batu.ninoclient.note.recycler

import android.content.Context
import android.os.Bundle
import android.view.View
import com.example.batu.ninoclient.MainActivity
import com.example.batu.ninoclient.database.room.note.Note
import com.example.batu.ninoclient.main.sheets.EnterPincodeBottomSheet
import com.example.batu.ninoclient.note.actions.NoteOptionsBottomSheet
import com.example.batu.ninoclient.note.copy
import com.example.batu.ninoclient.note.edit
import com.example.batu.ninoclient.note.share
import com.example.batu.ninoclient.note.view
import com.example.batu.ninoclient.support.ui.ThemedActivity

class NoteRecyclerHolder(context: Context, view: View) : NoteRecyclerViewHolderBase(context, view) {

  private val activity = context as MainActivity

  override fun viewClick(note: Note, extra: Bundle?) {
    actionOrUnlockNote(note, Runnable { openNote(note) })
  }

  override fun viewLongClick(note: Note, extra: Bundle?) {
    NoteOptionsBottomSheet.openSheet(activity, note)
  }

  override fun deleteIconClick(note: Note, extra: Bundle?) {
    actionOrUnlockNote(note, Runnable { activity.moveItemToTrashOrDelete(note) })
  }

  override fun shareIconClick(note: Note, extra: Bundle?) {
    actionOrUnlockNote(note, Runnable { note.share(context) })
  }

  override fun editIconClick(note: Note, extra: Bundle?) {
    note.edit(context)
  }

  override fun copyIconClick(note: Note, extra: Bundle?) {
    actionOrUnlockNote(note, Runnable { note.copy(context) })
  }

  override fun moreOptionsIconClick(note: Note, extra: Bundle?) {
    NoteOptionsBottomSheet.openSheet(activity, note)
  }

  private fun actionOrUnlockNote(data: Note, runnable: Runnable) {
    if (context is ThemedActivity && data.locked) {
      EnterPincodeBottomSheet.openUnlockSheet(
          context as ThemedActivity,
          object : EnterPincodeBottomSheet.PincodeSuccessListener {
            override fun onFailure() {
              actionOrUnlockNote(data, runnable)
            }

            override fun onSuccess() {
              runnable.run()
            }
          })
      return
    } else if (data.locked) {
      return
    }
    runnable.run()
  }

  private fun openNote(data: Note) {
    data.view(context)
  }
}
