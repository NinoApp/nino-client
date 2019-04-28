package com.maubis.scarlet.base.note.tag.sheet

import android.app.Dialog
import android.content.DialogInterface
import android.view.View
import com.github.bijoysingh.uibasics.views.UITextView
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.database.room.note.Note
import com.maubis.scarlet.base.core.note.getTagUUIDs
import com.maubis.scarlet.base.core.tag.TagBuilder
import com.maubis.scarlet.base.note.save
import com.maubis.scarlet.base.note.tag.TagOptionsItem
import com.maubis.scarlet.base.note.toggleTag
import com.maubis.scarlet.base.config.CoreConfig.Companion.tagsDb
import com.maubis.scarlet.base.support.ui.ThemedActivity
import com.maubis.scarlet.base.support.ui.visibility
import com.thefinestartist.finestwebview.FinestWebView



class TagShortcutsBottomSheet : TagOptionItemBottomSheetBase() {

  var note: Note? = null
  var dismissListener: () -> Unit = {}
  var type: String = "google"

  override fun setupViewWithDialog(dialog: Dialog) {
    if (note === null) {
      dismiss()
      return
    }

    val options = getOptions()
    dialog.findViewById<View>(R.id.tag_card_layout).visibility = visibility(options.isNotEmpty())
    dialog.findViewById<UITextView>(R.id.new_tag_button).visibility = visibility(false);
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
    var url = "none"
    when (type) {
      "google" -> { url = "https://www.google.com/search?q="}
      "wikipedia" -> { url = "https://en.m.wikipedia.org/wiki/" }
      "youtube" -> { url = "https://www.youtube.com/results?search_query=" }
    }

    val activity = context as ThemedActivity

    for (tag in tagsDb.getAll()) {
      if (tags.contains(tag.uuid)) {
        options.add(TagOptionsItem(
                tag = tag,
                listener = View.OnClickListener {
                  FinestWebView.Builder(activity.applicationContext).titleDefault(tag.title).show(url + tag.title)
                },
                selected = true
        ))
      }
    }
    return options
  }

  companion object {
    fun openSheet(activity: ThemedActivity, note: Note, type: String, dismissListener: () -> Unit) {
      val sheet = TagShortcutsBottomSheet()

      sheet.note = note
      sheet.dismissListener = dismissListener
      sheet.type = type
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}