package com.maubis.scarlet.base.export.support

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.github.bijoysingh.starter.util.ToastHelper
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.export.data.ExportableFolder
import com.maubis.scarlet.base.export.data.ExportableNote
import com.maubis.scarlet.base.export.data.ExportableTag
import com.maubis.scarlet.base.export.sheet.NOTES_EXPORT_FOLDER
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


const val KEY_EXTERNAL_FOLDER_SYNC_ENABLED = "external_folder_sync_enabled"
const val KEY_EXTERNAL_FOLDER_SYNC_LAST_SCAN = "external_folder_sync_last_sync"
const val KEY_EXTERNAL_FOLDER_SYNC_BACKUP_LOCKED = "external_folder_sync_backup_locked"
const val KEY_EXTERNAL_FOLDER_SYNC_PATH = "external_folder_sync_path"

var sExternalFolderSync: Boolean
  get() = CoreConfig.instance.store().get(KEY_EXTERNAL_FOLDER_SYNC_ENABLED, false)
  set(value) = CoreConfig.instance.store().put(KEY_EXTERNAL_FOLDER_SYNC_ENABLED, value)

var sFolderSyncPath: String
  get() = CoreConfig.instance.store().get(KEY_EXTERNAL_FOLDER_SYNC_PATH, "$NOTES_EXPORT_FOLDER/Sync/")
  set(value) = CoreConfig.instance.store().put(KEY_EXTERNAL_FOLDER_SYNC_PATH, value)

var sFolderSyncBackupLocked: Boolean
  get() = CoreConfig.instance.store().get(KEY_EXTERNAL_FOLDER_SYNC_BACKUP_LOCKED, true)
  set(value) = CoreConfig.instance.store().put(KEY_EXTERNAL_FOLDER_SYNC_BACKUP_LOCKED, value)

object ExternalFolderSync {

  fun hasPermission(context: Context): Boolean {
    return !(Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
  }

  fun enable(context: Context, enabled: Boolean) {
    if (enabled) {
      if (!hasPermission(context)) {
        GlobalScope.launch(Dispatchers.Main) {
          ToastHelper.show(context, R.string.permission_layout_give_permission_details)
          CoreConfig.instance.externalFolderSync().reset()
        }
        return
      }
      sExternalFolderSync = true
      loadFirstTime()
    } else {
      sExternalFolderSync = false
      CoreConfig.instance.externalFolderSync().reset()
    }
  }

  fun loadFirstTime() {
    CoreConfig.instance.externalFolderSync().init(
        {
          CoreConfig.instance.notesDatabase().getAll().forEach {
            CoreConfig.instance.externalFolderSync().insert(ExportableNote(it))
          }
        },
        {
          CoreConfig.instance.tagsDatabase().getAll().forEach {
            CoreConfig.instance.externalFolderSync().insert(ExportableTag(it))
          }
        },
        {
          CoreConfig.instance.foldersDatabase().getAll().forEach {
            CoreConfig.instance.externalFolderSync().insert(ExportableFolder(it))
          }
        })
  }

  fun setup(context: Context) {
    if (!sExternalFolderSync) {
      return
    }

    if (!hasPermission(context)) {
      sExternalFolderSync = false
      return
    }
    CoreConfig.instance.externalFolderSync().init()
  }
}