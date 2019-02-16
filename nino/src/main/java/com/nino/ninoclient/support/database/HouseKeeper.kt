package com.nino.ninoclient.support.database

import android.content.Context
import com.nino.ninoclient.config.CoreConfig.Companion.foldersDb
import com.nino.ninoclient.config.CoreConfig.Companion.notesDb
import com.nino.ninoclient.core.note.NoteImage.Companion.deleteIfExist
import com.nino.ninoclient.core.note.ReminderInterval
import com.nino.ninoclient.core.note.getReminderV2
import com.nino.ninoclient.core.note.setReminderV2
import com.nino.ninoclient.note.delete
import com.nino.ninoclient.note.reminders.ReminderJob.Companion.nextJobTimestamp
import com.nino.ninoclient.note.reminders.ReminderJob.Companion.scheduleJob
import com.nino.ninoclient.note.save
import com.nino.ninoclient.note.saveWithoutSync
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

class HouseKeeper(val context: Context) {

  private val houseKeeperTasks: Array<() -> Unit> = arrayOf(
      { removeOlderClips() },
      { removeDecoupledFolders() },
      { removeOldReminders() },
      { deleteRedundantImageFiles() },
      { migrateZeroUidNotes() }
  )

  fun execute() {
    for (task in houseKeeperTasks) {
      task()
    }
  }

  private fun removeOlderClips() {
    val notes = notesDb.database()
        .getOldTrashedNotes(
            Calendar.getInstance().timeInMillis - 1000 * 60 * 60 * 24 * 7)
    for (note in notes) {
      note.delete(context)
    }
  }

  private fun removeDecoupledFolders() {
    val folders = foldersDb.getAll().map { it.uuid }
    notesDb.getAll()
        .filter { it.folder.isNotBlank() }
        .forEach {
          if (!folders.contains(it.folder)) {
            it.folder = ""
            it.save(context)
          }
        }
  }

  private fun removeOldReminders() {
    notesDb.getAll().forEach {
      val reminder = it.getReminderV2()
      if (reminder === null) {
        return@forEach
      }

      // Some gap to allow delays in alarm
      if (reminder.timestamp >= System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(30)) {
        return@forEach
      }

      if (reminder.interval == ReminderInterval.ONCE) {
        it.meta = ""
        it.saveWithoutSync(context)
        return@forEach
      }

      reminder.timestamp = nextJobTimestamp(reminder.timestamp, System.currentTimeMillis())
      reminder.uid = scheduleJob(it.uuid, reminder)
      it.setReminderV2(reminder)
      it.saveWithoutSync(context)
    }
  }

  private fun deleteRedundantImageFiles() {
    val uuids = notesDb.getAllUUIDs()

    val imagesFolder = File(context.filesDir, "images" + File.separator)
    val uuidFiles = imagesFolder.listFiles()
    if (uuidFiles === null || uuidFiles.isEmpty()) {
      return
    }

    val availableDirectories = HashSet<String>()
    for (file in uuidFiles) {
      if (file.isDirectory) {
        availableDirectories.add(file.name)
      }
    }
    for (id in uuids) {
      availableDirectories.remove(id)
    }
    for (uuid in availableDirectories) {
      val noteFolder = File(imagesFolder, uuid)
      for (file in noteFolder.listFiles()) {
        deleteIfExist(file)
      }
    }
  }

  private fun migrateZeroUidNotes() {
    val note = notesDb.getByID(0)
    if (note != null) {
      notesDb.database().delete(note)
      notesDb.notifyDelete(note)
      note.uid = null
      note.save(context)
    }
  }
}