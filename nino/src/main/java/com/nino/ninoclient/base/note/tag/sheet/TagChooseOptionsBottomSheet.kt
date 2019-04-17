package com.nino.ninoclient.base.note.tag.sheet

import android.app.Dialog
import android.content.DialogInterface
import android.view.View
import com.nino.ninoclient.R
import com.nino.ninoclient.base.database.room.note.Note
import com.nino.ninoclient.base.core.note.getTagUUIDs
import com.nino.ninoclient.base.core.tag.TagBuilder
import com.nino.ninoclient.base.note.save
import com.nino.ninoclient.base.note.tag.TagOptionsItem
import com.nino.ninoclient.base.note.toggleTag
import com.nino.ninoclient.base.config.CoreConfig.Companion.tagsDb
import com.nino.ninoclient.base.support.ui.ThemedActivity
import com.nino.ninoclient.base.support.ui.visibility

class TagChooseOptionsBottomSheet : TagOptionItemBottomSheetBase() {

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

  override fun onNewTagClick() {
    val activity = context as ThemedActivity
    CreateOrEditTagBottomSheet.openSheet(activity, TagBuilder().emptyTag(), { tag, _ ->
      note!!.toggleTag(tag)
      note!!.save(activity)
      reset(dialog)
    })
  }

  override fun onDismiss(dialog: DialogInterface?) {
    super.onDismiss(dialog)
    dismissListener()
  }

  private fun getOptions(): List<TagOptionsItem> {
    val options = ArrayList<TagOptionsItem>()
    val tags = note!!.getTagUUIDs()
    for (tag in tagsDb.getAll()) {
      options.add(TagOptionsItem(
          tag = tag,
          listener = View.OnClickListener {
            note!!.toggleTag(tag)
            note!!.save(themedContext())
            reset(dialog)
          },
          selected = tags.contains(tag.uuid)
      ))
    }
    return options
  }

  companion object {
    fun openSheet(activity: ThemedActivity, note: Note, dismissListener: () -> Unit) {
      val sheet = TagChooseOptionsBottomSheet()

      sheet.note = note
      sheet.dismissListener = dismissListener
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}