package com.nino.ninoclient.settings.sheet

import android.app.Dialog
import android.view.View
import com.nino.ninoclient.MainActivity
import com.nino.ninoclient.R
import com.nino.ninoclient.config.CoreConfig
import com.nino.ninoclient.config.CoreConfig.Companion.foldersDb
import com.nino.ninoclient.config.CoreConfig.Companion.notesDb
import com.nino.ninoclient.config.CoreConfig.Companion.tagsDb
import com.nino.ninoclient.main.sheets.AlertBottomSheet
import com.nino.ninoclient.note.delete
import com.nino.ninoclient.note.folder.delete
import com.nino.ninoclient.note.tag.delete
import com.nino.ninoclient.support.option.OptionsItem
import com.nino.ninoclient.support.sheets.OptionItemBottomSheetBase
import kotlinx.coroutines.*

class DeleteAndMoreOptionsBottomSheet : OptionItemBottomSheetBase() {

  override fun setupViewWithDialog(dialog: Dialog) {
    GlobalScope.launch(Dispatchers.Main) {
      val options = GlobalScope.async(Dispatchers.IO) { getOptions() }
      setOptions(dialog, options.await())
    }
  }

  private fun getOptions(): List<OptionsItem> {
    val activity = context as MainActivity
    val options = ArrayList<OptionsItem>()
    options.add(OptionsItem(
        title = R.string.home_option_delete_all_notes,
        subtitle = R.string.home_option_delete_all_notes_details,
        icon = R.drawable.ic_note_white_48dp,
        listener = View.OnClickListener {
          AlertBottomSheet.openDeleteAllXSheet(activity, R.string.home_option_delete_all_notes_details) {
            GlobalScope.launch(Dispatchers.Main) {
              withContext(Dispatchers.IO) { notesDb.getAll().forEach { it.delete(activity) } }
              activity.resetAndSetupData()
              dismiss()
            }
          }
        }
    ))
    options.add(OptionsItem(
        title = R.string.home_option_delete_all_tags,
        subtitle = R.string.home_option_delete_all_tags_details,
        icon = R.drawable.ic_action_tags,
        listener = View.OnClickListener {
          AlertBottomSheet.openDeleteAllXSheet(activity, R.string.home_option_delete_all_tags_details) {
            GlobalScope.launch(Dispatchers.Main) {
              withContext(Dispatchers.IO) { tagsDb.getAll().forEach { it.delete() } }
              activity.resetAndSetupData()
              dismiss()
            }
          }
        }
    ))
    options.add(OptionsItem(
        title = R.string.home_option_delete_all_folders,
        subtitle = R.string.home_option_delete_all_folders_details,
        icon = R.drawable.ic_folder,
        listener = View.OnClickListener {
          AlertBottomSheet.openDeleteAllXSheet(activity, R.string.home_option_delete_all_folders_details) {
            GlobalScope.launch(Dispatchers.Main) {
              withContext(Dispatchers.IO) { foldersDb.getAll().forEach { it.delete() } }
              activity.resetAndSetupData()
              dismiss()
            }
          }
        }
    ))
    options.add(OptionsItem(
        title = R.string.home_option_delete_everything,
        subtitle = R.string.home_option_delete_everything_details,
        icon = R.drawable.ic_delete_permanently,
        listener = View.OnClickListener {
          AlertBottomSheet.openDeleteAllXSheet(activity, R.string.home_option_delete_everything_details) {
            GlobalScope.launch(Dispatchers.Main) {
              val notes = GlobalScope.async(Dispatchers.IO) { notesDb.getAll().forEach { it.delete(activity) } }
              val tags = GlobalScope.async(Dispatchers.IO) { tagsDb.getAll().forEach { it.delete() } }
              val folders = GlobalScope.async(Dispatchers.IO) { foldersDb.getAll().forEach { it.delete() } }

              notes.await()
              tags.await()
              folders.await()

              activity.resetAndSetupData()
              dismiss()
            }
          }

        }
    ))
    val forgetMeClick = CoreConfig.instance.authenticator().openForgetMeActivity(activity)
    options.add(OptionsItem(
        title = R.string.forget_me_option_title,
        subtitle = R.string.forget_me_option_details,
        icon = R.drawable.ic_action_forget_me,
        listener = View.OnClickListener {
          forgetMeClick?.run()
          dismiss()
        },
        visible = forgetMeClick !== null && CoreConfig.instance.authenticator().isLoggedIn()
    ))
    return options
  }

  override fun getLayout(): Int = R.layout.bottom_sheet_options

  companion object {
    fun openSheet(activity: MainActivity) {
      val sheet = DeleteAndMoreOptionsBottomSheet()
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}