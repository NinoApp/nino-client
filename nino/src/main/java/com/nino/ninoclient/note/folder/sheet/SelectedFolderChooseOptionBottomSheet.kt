package com.nino.ninoclient.note.folder.sheet

import android.app.Dialog
import android.view.View
import com.nino.ninoclient.R
import com.nino.ninoclient.database.room.folder.Folder
import com.nino.ninoclient.core.folder.FolderBuilder
import com.nino.ninoclient.note.folder.FolderOptionsItem
import com.nino.ninoclient.note.selection.activity.SelectNotesActivity
import com.nino.ninoclient.config.CoreConfig.Companion.foldersDb
import com.nino.ninoclient.config.CoreConfig.Companion.notesDb
import com.nino.ninoclient.support.ui.ThemedActivity
import com.nino.ninoclient.support.ui.visibility

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