package com.nino.ninoclient.note.folder.sheet

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.view.View
import com.nino.ninoclient.R
import com.nino.ninoclient.database.room.folder.Folder
import com.nino.ninoclient.database.room.note.Note
import com.nino.ninoclient.core.folder.FolderBuilder
import com.nino.ninoclient.note.folder.FolderOptionsItem
import com.nino.ninoclient.note.save
import com.nino.ninoclient.config.CoreConfig.Companion.foldersDb
import com.nino.ninoclient.config.CoreConfig.Companion.notesDb
import com.nino.ninoclient.support.ui.ThemedActivity
import com.nino.ninoclient.support.ui.visibility

class FolderChooseOptionsBottomSheet : FolderOptionItemBottomSheetBase() {

  var note: Note? = null
  var dismissListener: () -> Unit = {}

  override fun setupViewWithDialog(dialog: Dialog) {
    if (note === null) {
      dismiss()
      return
    }

    val options = getOptions()
    dialog.findViewById<View>(R.id.tag_card_layout).visibility = visibility(options.isNotEmpty())
    setOptions(dialog, getOptions())
  }

  override fun onNewFolderClick() {
    val activity = context as ThemedActivity
    CreateOrEditFolderBottomSheet.openSheet(activity, FolderBuilder().emptyFolder(), { folder, _ ->
      toggleFolder(activity, note, folder)
      reset(dialog)
    })
  }

  fun toggleFolder(context: Context, note: Note?, folder: Folder) {
    val localNote = note
    if (localNote === null) {
      return
    }
    localNote.folder = if (localNote.folder === folder.uuid) "" else folder.uuid
    localNote.save(context)
  }

  override fun onDismiss(dialog: DialogInterface?) {
    super.onDismiss(dialog)
    dismissListener()
  }

  private fun getOptions(): List<FolderOptionsItem> {
    val activity = themedContext() as ThemedActivity
    val options = ArrayList<FolderOptionsItem>()
    val selectedFolder = note!!.folder
    for (folder in foldersDb.getAll()) {
      options.add(FolderOptionsItem(
          folder = folder,
          usages = notesDb.getNoteCountByFolder(folder.uuid),
          listener = {
            toggleFolder(activity, note, folder)
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
    fun openSheet(activity: ThemedActivity, note: Note, dismissListener: () -> Unit) {
      val sheet = FolderChooseOptionsBottomSheet()

      sheet.note = note
      sheet.dismissListener = dismissListener
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}