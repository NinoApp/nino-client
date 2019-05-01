package com.maubis.scarlet.base.note.folder.sheet

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.util.Log
import android.view.View
import com.koushikdutta.ion.Ion
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.database.room.folder.Folder
import com.maubis.scarlet.base.database.room.note.Note
import com.maubis.scarlet.base.core.folder.FolderBuilder
import com.maubis.scarlet.base.note.folder.FolderOptionsItem
import com.maubis.scarlet.base.note.save
import com.maubis.scarlet.base.config.CoreConfig.Companion.foldersDb
import com.maubis.scarlet.base.config.CoreConfig.Companion.notesDb
import com.maubis.scarlet.base.note.getTagString
import com.maubis.scarlet.base.support.ui.ThemedActivity
import com.maubis.scarlet.base.support.ui.visibility

import com.maubis.scarlet.base.config.CoreConfig.Companion.tagsDb
import com.maubis.scarlet.base.note.folder.save

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

    val tagArray = note!!.tags.split(",")
    var topics = ""

    if (tagArray.size > 0)
      if (tagArray[0].isNotEmpty())
        topics += tagsDb.getByUUID( tagArray[0])!!.title

    for (i in 1..4)
      if (tagArray.size > i && tagArray[i].isNotEmpty())
        topics += "," + tagsDb.getByUUID(tagArray[i])!!.title

    val suggestions = ArrayList<String>()

    for (tag in tagArray) {
      if (tag.isNotEmpty()) {
        suggestions.add(tagsDb.getByUUID(tag)!!.title)
        /* TODO: fix this
        val jsonSuggestions = Ion.with(activity)
                .load("https://api.datamuse.com/words?max=2&rel_spc=" + tag + "&topics=" + topics)
                .setLogging("IONLOGS", Log.DEBUG)
                .asJsonArray()
                .get()

        for (jsonSuggestion in jsonSuggestions) {
          val entity = jsonSuggestion.asJsonObject.getAsJsonPrimitive("word").asString
          suggestions.add(entity)
        }
        */
      }
    }

    var isOkey = true

    for (fname in suggestions) {
      for (folder in foldersDb.getAll()) {
        if (fname == folder.title) {
          isOkey = false
          break
        }
      }

      if (!isOkey)
        continue

      val folder = FolderBuilder().emptyFolder()
      folder.title = "Suggestion: " + fname
      folder.uuid = fname

      options.add(FolderOptionsItem(
              folder = folder,
              usages = 0,
              listener = {
                folder.title = folder.title.replace("Suggestion: ", "")
                folder.updateTimestamp = System.currentTimeMillis()
                folder.save()
                toggleFolder(activity, note, folder)
                reset(dialog)
              },
              editListener = {
                CreateOrEditFolderBottomSheet.openSheet(activity, folder, {_,_ -> reset(dialog)})
              },
              selected = folder.uuid == selectedFolder
      ))

    }

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