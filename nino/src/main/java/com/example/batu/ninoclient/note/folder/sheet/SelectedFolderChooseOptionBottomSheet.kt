package com.example.batu.ninoclient.note.folder.sheet

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.view.View
import com.example.batu.ninoclient.R
import com.example.batu.ninoclient.database.room.folder.Folder
import com.example.batu.ninoclient.database.room.note.Note
import com.example.batu.ninoclient.database.room.tag.Tag
import com.example.batu.ninoclient.core.folder.FolderBuilder
import com.example.batu.ninoclient.note.folder.FolderOptionsItem
import com.example.batu.ninoclient.note.save
import com.example.batu.ninoclient.note.selection.activity.SelectNotesActivity
import com.example.batu.ninoclient.config.CoreConfig.Companion.foldersDb
import com.example.batu.ninoclient.config.CoreConfig.Companion.notesDb
import com.example.batu.ninoclient.support.ui.ThemedActivity
import com.example.batu.ninoclient.support.ui.visibility

class SelectedFolderChooseOptionsBottomSheet : FolderOptionItemBottomSheetBase() {

  var onActionListener: (Folder, Boolean) -> Unit = { _, _ -> }

  override fun setupViewWithDialog(dialog: Dialog) {
    val options = getOptions()
    dialog.findViewById<View>(R.id.tag_card_layout).visibility = visibility(options.isNotEmpty())
    setOptions(dialog, getOptions())
  }

  override fun onNewFolderClick() {
    val activity = context as ThemedActivity
    CreateOrEditFolderBottomSheet.openSheet(activity, FolderBuilder().emptyFolder(), { folder, _ ->
      onActionListener(folder, true)
      reset(dialog)
    })
  }

  private fun getOptions(): List<FolderOptionsItem> {
    val activity = themedContext() as SelectNotesActivity
    val options = ArrayList<FolderOptionsItem>()

    val folders = activity.getAllSelectedNotes().map { it.folder }.distinct()
    val selectedFolder = when (folders.size) {
      1 -> folders.first()
      else -> ""
    }
    for (folder in foldersDb.getAll()) {
      options.add(FolderOptionsItem(
          folder = folder,
          usages = notesDb.getNoteCountByFolder(folder.uuid),
          listener = {
            onActionListener(folder, folder.uuid != selectedFolder)
            reset(dialog)
          },
          editListener = {
            CreateOrEditFolderBottomSheet.openSheet(activity, folder, {_,_ -> reset(dialog)})
          },
          selected = folder.uuid == selectedFolder
      ))
    }
    return options
  }

  companion object {
    fun openSheet(activity: ThemedActivity, listener: (Folder, Boolean) -> Unit) {
      val sheet = SelectedFolderChooseOptionsBottomSheet()
      sheet.onActionListener = listener
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}