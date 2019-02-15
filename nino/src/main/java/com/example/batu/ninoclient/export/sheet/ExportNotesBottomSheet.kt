package com.example.batu.ninoclient.export.sheet

import android.app.Dialog
import android.content.Intent
import android.support.v4.content.FileProvider
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.github.bijoysingh.starter.async.MultiAsyncTask
import com.example.batu.ninoclient.MainActivity
import com.example.batu.ninoclient.R
import com.example.batu.ninoclient.config.CoreConfig
import com.example.batu.ninoclient.export.support.*
import com.example.batu.ninoclient.support.option.OptionsItem
import com.example.batu.ninoclient.support.sheets.getViewForOption
import com.example.batu.ninoclient.support.utils.Flavor
import com.example.batu.ninoclient.support.ui.ThemeColorType
import com.example.batu.ninoclient.support.ui.ThemedBottomSheetFragment


class ExportNotesBottomSheet : ThemedBottomSheetFragment() {
  override fun getBackgroundView(): Int {
    return R.id.container_layout
  }

  override fun setupView(dialog: Dialog?) {
    super.setupView(dialog)
    if (dialog == null) {
      return
    }

    val exportTitle = dialog.findViewById<TextView>(R.id.export_title)
    val filename = dialog.findViewById<TextView>(R.id.filename)
    val progressBar = dialog.findViewById<ProgressBar>(R.id.progress_bar)
    val resultLayout = dialog.findViewById<View>(R.id.results_layout)
    val exportDone = dialog.findViewById<TextView>(R.id.export_done)
    val exportShare = dialog.findViewById<TextView>(R.id.export_share)

    val activity = themedActivity()
    MultiAsyncTask.execute(object : MultiAsyncTask.Task<Boolean> {
      override fun run(): Boolean {
        val notes = NoteExporter().getExportContent()
        return NoteExporter().saveToManualExportFile(notes)
      }

      override fun handle(result: Boolean) {
        resultLayout.visibility = View.VISIBLE
        progressBar.visibility = View.INVISIBLE
        exportTitle.setText(
            if (result) R.string.import_export_layout_exported
            else R.string.import_export_layout_export_failed)
        exportDone.visibility = if (result) View.VISIBLE else View.GONE
      }
    })
    exportDone.setOnClickListener {
      dismiss()
    }

    val file = NoteExporter().getOrCreateManualExportFile()
    exportShare.setOnClickListener {
      if (file == null || !file.exists()) {
        return@setOnClickListener
      }

      val uri = FileProvider.getUriForFile(activity, GenericFileProvider.PROVIDER, file)

      val intent = Intent(Intent.ACTION_SEND)
      intent.type = "text/plain"
      intent.putExtra(Intent.EXTRA_STREAM, uri)
      startActivity(Intent.createChooser(intent, getString(R.string.share_using)))

      dismiss()
    }

    exportTitle.setTextColor(CoreConfig.instance.themeController().get(ThemeColorType.TERTIARY_TEXT))
    filename.setTextColor(CoreConfig.instance.themeController().get(ThemeColorType.HINT_TEXT))
    filename.text = "${file?.parentFile?.name}/${file?.name}"

    setOptions()
    makeBackgroundTransparent(dialog, R.id.root_layout)
  }

  fun reset(dialog: Dialog) {
    setupView(dialog)
  }

  private fun setOptions() {
    val layout = dialog.findViewById<LinearLayout>(R.id.options_container)
    layout.removeAllViews()

    val activity = context as MainActivity
    val options = ArrayList<OptionsItem>()
    val exportAsMarkdown = CoreConfig.instance.store().get(KEY_BACKUP_MARKDOWN, false)
    options.add(OptionsItem(
        title = R.string.home_option_export_markdown,
        subtitle = R.string.home_option_export_markdown_subtitle,
        icon = R.drawable.ic_markdown_logo,
        listener = View.OnClickListener {
          CoreConfig.instance.store().put(KEY_BACKUP_MARKDOWN, !exportAsMarkdown)
          reset(dialog)
        },
        enabled = exportAsMarkdown
    ))
    options.add(OptionsItem(
        title = R.string.import_export_locked,
        subtitle = R.string.import_export_locked_details,
        icon = R.drawable.ic_action_lock,
        listener = View.OnClickListener {
          BackupSettingsOptionsBottomSheet.exportLockedNotes = !BackupSettingsOptionsBottomSheet.exportLockedNotes
          reset(dialog)
        },
        enabled = BackupSettingsOptionsBottomSheet.exportLockedNotes
    ))
    val autoBackupEnabled = CoreConfig.instance.store().get(KEY_AUTO_BACKUP_MODE, false)
    options.add(OptionsItem(
        title = R.string.home_option_auto_export,
        subtitle = R.string.home_option_auto_export_subtitle,
        icon = R.drawable.ic_time,
        listener = View.OnClickListener {
          val manager = PermissionUtils().getStoragePermissionManager(activity)
          val hasAllPermissions = manager.hasAllPermissions()
          when {
            autoBackupEnabled -> {
              CoreConfig.instance.store().put(KEY_AUTO_BACKUP_MODE, false)
              reset(dialog)
            }
            hasAllPermissions -> {
              CoreConfig.instance.store().put(KEY_AUTO_BACKUP_MODE, true)
              reset(dialog)
            }
            else -> PermissionBottomSheet.openSheet(activity)
          }
        },
        enabled = autoBackupEnabled
    ))

    for (option in options) {
      if (!option.visible) {
        continue
      }

      val contentView = getViewForOption(
          themedContext(), option, getOptionsTitleColor(option.selected), getOptionsSubtitleColor(option.selected))
      layout.addView(contentView)
    }
  }

  override fun getLayout(): Int = R.layout.bottom_sheet_import_export

  override fun getBackgroundCardViewIds(): Array<Int> = arrayOf(R.id.export_card, R.id.options_card_layout)

  companion object {
    val MATERIAL_NOTES_FOLDER
      get() = when (CoreConfig.instance.appFlavor()) {
        Flavor.NONE -> "MaterialNotes"
        Flavor.LITE -> "Scarlet"
        Flavor.PRO -> "ScarletPro"
      }
    val FILENAME = "manual_backup"

    fun openSheet(activity: MainActivity) {
      val sheet = ExportNotesBottomSheet()
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}