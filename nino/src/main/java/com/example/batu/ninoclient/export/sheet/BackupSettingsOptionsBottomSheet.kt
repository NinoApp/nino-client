package com.example.batu.ninoclient.export.sheet

import android.app.Dialog
import android.view.View
import com.github.bijoysingh.starter.util.IntentUtils
import com.example.batu.ninoclient.MainActivity
import com.example.batu.ninoclient.R
import com.example.batu.ninoclient.config.CoreConfig
import com.example.batu.ninoclient.export.activity.ImportNoteActivity
import com.example.batu.ninoclient.export.support.ExternalFolderSync
import com.example.batu.ninoclient.export.support.KEY_BACKUP_LOCKED
import com.example.batu.ninoclient.export.support.PermissionUtils
import com.example.batu.ninoclient.main.sheets.EnterPincodeBottomSheet
import com.example.batu.ninoclient.settings.sheet.SecurityOptionsBottomSheet
import com.example.batu.ninoclient.support.option.OptionsItem
import com.example.batu.ninoclient.support.sheets.OptionItemBottomSheetBase
import com.example.batu.ninoclient.support.ui.ThemedActivity
import com.example.batu.ninoclient.support.ui.ThemedBottomSheetFragment
import com.example.batu.ninoclient.support.utils.Flavor

class BackupSettingsOptionsBottomSheet : OptionItemBottomSheetBase() {

  override fun setupViewWithDialog(dialog: Dialog) {
    setOptions(dialog, getOptions())
  }

  private fun getOptions(): List<OptionsItem> {
    val activity = context as MainActivity
    val options = ArrayList<OptionsItem>()
    options.add(OptionsItem(
        title = R.string.home_option_install_from_store,
        subtitle = R.string.home_option_install_from_store_subtitle,
        icon = R.drawable.ic_action_play,
        listener = View.OnClickListener {
          IntentUtils.openAppPlayStore(context)
          dismiss()
        },
        visible = CoreConfig.instance.appFlavor() == Flavor.NONE
    ))
    options.add(OptionsItem(
        title = R.string.home_option_export,
        subtitle = R.string.home_option_export_subtitle,
        icon = R.drawable.ic_export,
        listener = View.OnClickListener {
          val manager = PermissionUtils().getStoragePermissionManager(activity)
          val hasAllPermissions = manager.hasAllPermissions()
          when (hasAllPermissions) {
            true -> {
              openExportSheet()
              dismiss()
            }
            false -> {
              PermissionBottomSheet.openSheet(activity)
            }
          }
        }
    ))
    options.add(OptionsItem(
        title = R.string.home_option_import,
        subtitle = R.string.home_option_import_subtitle,
        icon = R.drawable.ic_import,
        listener = View.OnClickListener {
          val manager = PermissionUtils().getStoragePermissionManager(activity)
          val hasAllPermissions = manager.hasAllPermissions()
          when (hasAllPermissions) {
            true -> {
              IntentUtils.startActivity(activity, ImportNoteActivity::class.java)
              dismiss()
            }
            false -> {
              PermissionBottomSheet.openSheet(activity)
            }
          }
        }
    ))
    options.add(OptionsItem(
        title = R.string.import_export_layout_folder_sync,
        subtitle = R.string.import_export_layout_folder_sync_details,
        icon = R.drawable.icon_folder_sync,
        listener = View.OnClickListener {
          val manager = PermissionUtils().getStoragePermissionManager(activity)
          val hasAllPermissions = manager.hasAllPermissions()
          when (hasAllPermissions) {
            true -> {
              ThemedBottomSheetFragment.openSheet(activity, ExternalFolderSyncBottomSheet())
            }
            false -> PermissionBottomSheet.openSheet(activity)
          }
        }
    ))
    return options
  }

  private fun openExportSheet() {
    val activity = themedActivity() as MainActivity
    if (!SecurityOptionsBottomSheet.hasPinCodeEnabled()) {
      ExportNotesBottomSheet.openSheet(activity)
      return
    }
    EnterPincodeBottomSheet.openUnlockSheet(
        activity as ThemedActivity,
        object : EnterPincodeBottomSheet.PincodeSuccessListener {
          override fun onFailure() {
            openExportSheet()
          }

          override fun onSuccess() {
            ExportNotesBottomSheet.openSheet(activity)
          }
        })
  }

  override fun getLayout(): Int = R.layout.bottom_sheet_options

  companion object {
    fun openSheet(activity: MainActivity) {
      val sheet = BackupSettingsOptionsBottomSheet()
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }

    var exportLockedNotes: Boolean
      get() = CoreConfig.instance.store().get(KEY_BACKUP_LOCKED, true)
      set(value) = CoreConfig.instance.store().put(KEY_BACKUP_LOCKED, value)
  }
}