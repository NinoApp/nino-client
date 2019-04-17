package com.nino.ninoclient.base.note.tag.sheet

import android.app.Dialog
import android.view.View
import com.nino.ninoclient.R
import com.nino.ninoclient.base.database.room.tag.Tag
import com.nino.ninoclient.base.core.note.getTagUUIDs
import com.nino.ninoclient.base.core.tag.TagBuilder
import com.nino.ninoclient.base.note.selection.activity.SelectNotesActivity
import com.nino.ninoclient.base.note.tag.TagOptionsItem
import com.nino.ninoclient.base.config.CoreConfig.Companion.tagsDb
import com.nino.ninoclient.base.support.ui.ThemedActivity
import com.nino.ninoclient.base.support.ui.visibility

class SelectedTagChooseOptionsBottomSheet : TagOptionItemBottomSheetBase() {

  var onActionListener: (Tag, Boolean) -> Unit = { _, _ -> }

  override fun setupViewWithDialog(dialog: Dialog) {
    val options = getOptions()
    dialog.findViewById<View>(R.id.tag_card_layout).visibility = visibility(options.isNotEmpty())
    setOptions(dialog, getOptions())
  }

  override fun onNewTagClick() {
    val activity = context as ThemedActivity
    CreateOrEditTagBottomSheet.openSheet(activity, TagBuilder().emptyTag(), { tag, _ ->
      onActionListener(tag, true)
      reset(dialog)
    })
  }

  private fun getOptions(): List<TagOptionsItem> {
    val activity = themedContext() as SelectNotesActivity
    val options = ArrayList<TagOptionsItem>()

    val tags = HashSet<String>()
    tags.addAll(activity.getAllSelectedNotes().firstOrNull()?.getTagUUIDs() ?: emptySet())

    activity.getAllSelectedNotes().forEach {
      val uuids = it.getTagUUIDs().toMutableSet()
      val uuidsToRemove = HashSet<String>()
      for (tag in tags) {
        if (!uuids.contains(tag)) {
          uuidsToRemove.add(tag)
        }
      }
      tags.removeAll(uuidsToRemove)
    }
    for (tag in tagsDb.getAll()) {
      options.add(TagOptionsItem(
          tag = tag,
          listener = View.OnClickListener {
            onActionListener(tag, !tags.contains(tag.uuid))
            activity.refreshSelectedNotes()
            reset(dialog)
          },
          selected = tags.contains(tag.uuid)
      ))
    }
    return options
  }

  companion object {
    fun openSheet(activity: ThemedActivity, onActionListener: (Tag, Boolean) -> Unit) {
      val sheet = SelectedTagChooseOptionsBottomSheet()
      sheet.onActionListener = onActionListener
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}