package com.example.batu.ninoclient.export.sheet

import android.app.Dialog
import android.content.Intent
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.support.v7.widget.CardView
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.github.bijoysingh.starter.async.MultiAsyncTask
import com.example.batu.ninoclient.R
import com.example.batu.ninoclient.config.CoreConfig
import com.example.batu.ninoclient.export.sheet.ExportNotesBottomSheet.Companion.MATERIAL_NOTES_FOLDER
import com.example.batu.ninoclient.export.support.*
import com.example.batu.ninoclient.export.support.ExternalFolderSync.externalFolderSync
import com.example.batu.ninoclient.support.option.OptionsItem
import com.example.batu.ninoclient.support.sheets.getViewForOption
import com.example.batu.ninoclient.support.ui.ThemeColorType
import com.example.batu.ninoclient.support.ui.ThemedBottomSheetFragment

class ExternalFolderSyncBottomSheet : ThemedBottomSheetFragment() {
  override fun getBackgroundView(): Int {
    return R.id.container_layout
  }

  override fun setupView(dialog: Dialog?) {
    super.setupView(dialog)
    if (dialog == null) {
      return
    }

    val syncTitle = dialog.findViewById<TextView>(R.id.folder_sync_title)
    val syncDescription = dialog.findViewById<TextView>(R.id.folder_sync_description)

    val syncFolderTitle = dialog.findViewById<TextView>(R.id.folder_sync_path_title)
    val syncFolderPath = dialog.findViewById<EditText>(R.id.folder_sync_path)

    val enableDisableCard = dialog.findViewById<CardView>(R.id.enable_disable_card)
    val enableDisableSync = dialog.findViewById<TextView>(R.id.folder_sync_enabled)
    enableDisableSync.setOnClickListener {
      ExternalFolderSync.enable(themedContext(), !externalFolderSync)
      reset(dialog)
    }

    when (externalFolderSync) {
      true -> {
        enableDisableSync.setText(R.string.import_export_layout_folder_sync_disable)
        enableDisableCard.setCardBackgroundColor(ContextCompat.getColor(themedContext(), R.color.material_grey_700))
      }
      false -> {
        enableDisableSync.setText(R.string.import_export_layout_folder_sync_enable)
        enableDisableCard.setCardBackgroundColor(ContextCompat.getColor(themedContext(), R.color.material_red_800))
      }
    }

    syncTitle.setTextColor(CoreConfig.instance.themeController().get(ThemeColorType.SECONDARY_TEXT))
    syncDescription.setTextColor(CoreConfig.instance.themeController().get(ThemeColorType.TERTIARY_TEXT))
    syncFolderTitle.setTextColor(CoreConfig.instance.themeController().get(ThemeColorType.SECONDARY_TEXT))
    syncFolderPath.setTextColor(CoreConfig.instance.themeController().get(ThemeColorType.TERTIARY_TEXT))
    syncFolderPath.setText(folderSyncPath)
    syncFolderPath.isEnabled = false

    setOptions()
    makeBackgroundTransparent(dialog, R.id.root_layout)
  }

  fun reset(dialog: Dialog) {
    setupView(dialog)
  }

  private fun setOptions() {
    val layout = dialog.findViewById<LinearLayout>(R.id.options_container)
    layout.removeAllViews()

    val options = ArrayList<OptionsItem>()
    options.add(OptionsItem(
        title = R.string.import_export_locked,
        subtitle = R.string.import_export_locked_details,
        icon = R.drawable.ic_action_lock,
        listener = View.OnClickListener {
          folderSyncBackupLocked = !folderSyncBackupLocked
          reset(dialog)
        },
        enabled = folderSyncBackupLocked
    ))
    for (option in options) {
      val contentView = getViewForOption(
          themedContext(), option, getOptionsTitleColor(option.selected), getOptionsSubtitleColor(option.selected))
      layout.addView(contentView)
    }
  }

  override fun getLayout(): Int = R.layout.bottom_sheet_folder_sync

  override fun getBackgroundCardViewIds(): Array<Int> = arrayOf(R.id.title_card, R.id.export_card, R.id.options_card_layout)

  companion object {
    var folderSyncPath: String
      get() = CoreConfig.instance.store().get(KEY_EXTERNAL_FOLDER_SYNC_PATH, "Scarlet/Sync/")
      set(value) = CoreConfig.instance.store().put(KEY_EXTERNAL_FOLDER_SYNC_PATH, value)

    var folderSyncBackupLocked: Boolean
      get() = CoreConfig.instance.store().get(KEY_EXTERNAL_FOLDER_SYNC_BACKUP_LOCKED, true)
      set(value) = CoreConfig.instance.store().put(KEY_EXTERNAL_FOLDER_SYNC_BACKUP_LOCKED, value)
  }
}