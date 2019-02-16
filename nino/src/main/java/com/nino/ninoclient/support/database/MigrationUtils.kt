package com.nino.ninoclient.support.database

import android.content.Context
import com.google.gson.Gson
import com.nino.ninoclient.config.CoreConfig
import com.nino.ninoclient.config.CoreConfig.Companion.notesDb
import com.nino.ninoclient.core.note.NoteMeta
import com.nino.ninoclient.core.note.Reminder
import com.nino.ninoclient.core.note.getReminder
import com.nino.ninoclient.note.reminders.ReminderJob
import com.nino.ninoclient.note.saveWithoutSync
import com.nino.ninoclient.settings.sheet.UISettingsOptionsBottomSheet.Companion.KEY_LIST_VIEW
import com.nino.ninoclient.support.ui.KEY_APP_THEME
import com.nino.ninoclient.support.ui.KEY_NIGHT_THEME
import com.nino.ninoclient.support.ui.Theme
import com.nino.ninoclient.support.utils.getLastUsedAppVersionCode
import java.io.File

const val KEY_MIGRATE_THEME = "KEY_MIGRATE_THEME"
const val KEY_MIGRATE_DEFAULT_VALUES = "KEY_MIGRATE_DEFAULT_VALUES"
const val KEY_MIGRATE_REMINDERS = "KEY_MIGRATE_REMINDERS"
const val KEY_MIGRATE_IMAGES = "KEY_MIGRATE_IMAGES"

class Migrator(val context: Context) {

  fun start() {
    runTask(KEY_MIGRATE_THEME) {
      val isNightMode = CoreConfig.instance.store().get(KEY_NIGHT_THEME, true)
      CoreConfig.instance.store().put(KEY_APP_THEME, if (isNightMode) Theme.DARK.name else Theme.LIGHT.name)
      CoreConfig.instance.themeController().notifyChange(context)
    }
    runTask(key = KEY_MIGRATE_REMINDERS) {
      val notes = notesDb.getAll()
      notes.forEach {
        val legacyReminder = it.getReminder()
        if (legacyReminder !== null) {
          val reminder = Reminder(0, legacyReminder.alarmTimestamp, legacyReminder.interval)

          val meta = NoteMeta()
          val uid = ReminderJob.scheduleJob(it.uuid, reminder)
          reminder.uid = uid
          if (uid == -1) {
            return@forEach
          }

          meta.reminderV2 = reminder
          it.meta = Gson().toJson(meta)
          it.saveWithoutSync(context)
        }
      }
    }
    runTask(KEY_MIGRATE_IMAGES) {
      File(context.cacheDir, "images").renameTo(File(context.filesDir, "images"))
    }
    runTaskIf(
        getLastUsedAppVersionCode() == 0,
        KEY_MIGRATE_DEFAULT_VALUES) {
          CoreConfig.instance.store().put(KEY_APP_THEME, Theme.DARK.name)
          CoreConfig.instance.store().put(KEY_LIST_VIEW, true)
        }
  }

  private fun runTask(key: String, task: () -> Unit) {
    if (CoreConfig.instance.store().get(key, false)) {
      return
    }
    task()
    CoreConfig.instance.store().put(key, true)
  }

  private fun runTaskIf(condition: Boolean, key: String, task: () -> Unit) {
    if (!condition) {
      return
    }
    runTask(key, task)
  }
}