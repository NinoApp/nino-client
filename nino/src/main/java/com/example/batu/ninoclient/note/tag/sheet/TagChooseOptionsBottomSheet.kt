package com.example.batu.ninoclient.note.tag.sheet

import android.app.Dialog
import android.content.DialogInterface
import android.view.View
import com.example.batu.ninoclient.R
import com.example.batu.ninoclient.database.room.note.Note
import com.example.batu.ninoclient.core.note.getTagUUIDs
import com.example.batu.ninoclient.core.tag.TagBuilder
import com.example.batu.ninoclient.note.save
import com.example.batu.ninoclient.note.tag.TagOptionsItem
import com.example.batu.ninoclient.note.toggleTag
import com.example.batu.ninoclient.config.CoreConfig.Companion.tagsDb
import com.example.batu.ninoclient.support.ui.ThemedActivity
import com.example.batu.ninoclient.support.ui.visibility

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