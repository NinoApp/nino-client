package com.nino.ninoclient.export.support

import android.os.AsyncTask
import android.os.Environment
import com.github.bijoysingh.starter.util.DateFormatter
import com.github.bijoysingh.starter.util.FileManager
import com.google.gson.Gson
import com.nino.ninoclient.config.CoreConfig
import com.nino.ninoclient.config.CoreConfig.Companion.foldersDb
import com.nino.ninoclient.config.CoreConfig.Companion.notesDb
import com.nino.ninoclient.config.CoreConfig.Companion.tagsDb
import com.nino.ninoclient.export.data.ExportableFileFormat
import com.nino.ninoclient.export.data.ExportableFolder
import com.nino.ninoclient.export.data.ExportableNote
import com.nino.ninoclient.export.data.ExportableTag
import com.nino.ninoclient.export.sheet.BackupSettingsOptionsBottomSheet
import com.nino.ninoclient.export.sheet.ExportNotesBottomSheet
import com.nino.ninoclient.note.getFullText
import java.io.File
import java.util.*

const val KEY_NOTE_VERSION = "KEY_NOTE_VERSION"
const val KEY_BACKUP_LOCKED = "KEY_BACKUP_LOCKED"
const val KEY_BACKUP_MARKDOWN = "KEY_BACKUP_MARKDOWN"
const val KEY_BACKUP_LOCATION = "KEY_BACKUP_LOCATION"
const val KEY_AUTO_BACKUP_MODE = "KEY_AUTO_BACKUP_MODE"
const val KEY_AUTO_BACKUP_LAST_TIMESTAMP = "KEY_AUTO_BACKUP_LAST_TIMESTAMP"

const val EXPORT_NOTE_SEPARATOR = ">S>C>A>R>L>E>T>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>N>O>T>E>S>"
const val EXPORT_VERSION = 6

const val AUTO_BACKUP_FILENAME = "auto_backup"
const val AUTO_BACKUP_INTERVAL_MS = 1000 * 60 * 60 * 6 // 6 hours update

class NoteExporter() {

  fun getExportContent(): String {
    if (CoreConfig.instance.store().get(KEY_BACKUP_MARKDOWN, false)) {
      return getMarkdownExportContent()
    }

    val exportLocked = BackupSettingsOptionsBottomSheet.exportLockedNotes
    val notes = notesDb
        .getAll()
        .filter { exportLocked || !it.locked }
        .map { ExportableNote(it) }
    val tags = tagsDb.getAll().map { ExportableTag(it) }
    val folders = foldersDb.getAll().map { ExportableFolder(it) }
    val fileContent = ExportableFileFormat(EXPORT_VERSION, notes, tags, folders)
    return Gson().toJson(fileContent)
  }

  private fun getMarkdownExportContent(): String {
    var totalText = "$EXPORT_NOTE_SEPARATOR\n\n"
    notesDb.getAll()
        .map { it.getFullText() }
        .forEach {
          totalText += it
          totalText += "\n\n$EXPORT_NOTE_SEPARATOR\n\n"
        }
    return totalText
  }

  fun tryAutoExport() {
    AsyncTask.execute {
      val autoBackup = CoreConfig.instance.store().get(KEY_AUTO_BACKUP_MODE, false)
      if (!autoBackup) {
        return@execute
      }
      val lastBackup = CoreConfig.instance.store().get(KEY_AUTO_BACKUP_LAST_TIMESTAMP, 0L)
      val lastTimestamp = notesDb.getLastTimestamp()
      if (lastBackup + AUTO_BACKUP_INTERVAL_MS >= lastTimestamp) {
        return@execute
      }

      val exportFile = getOrCreateFileForExport(AUTO_BACKUP_FILENAME + " " + DateFormatter.getDate("dd_MMM_yyyy", Calendar.getInstance()))
      if (exportFile === null) {
        return@execute
      }
      saveToFile(exportFile, getExportContent())
      CoreConfig.instance.store().put(KEY_AUTO_BACKUP_LAST_TIMESTAMP, System.currentTimeMillis())
    }
  }

  fun getOrCreateManualExportFile(): File? {
    return getOrCreateFileForExport(ExportNotesBottomSheet.FILENAME + " " + DateFormatter.getDate("dd_MMM_yyyy HH_mm", Calendar.getInstance()))
  }

  fun getOrCreateFileForExport(filename: String): File? {
    val folder = createFolder()
    if (folder === null) {
      return null
    }
    return File(folder, filename + ".txt")
  }

  fun saveToManualExportFile(text: String): Boolean {
    val file = getOrCreateManualExportFile()
    if (file === null) {
      return false
    }
    return saveToFile(file, text)
  }

  fun saveToFile(file: File, text: String): Boolean {
    return FileManager.writeToFile(file, text)
  }

  fun createFolder(): File? {
    val folder = File(Environment.getExternalStorageDirectory(), ExportNotesBottomSheet.MATERIAL_NOTES_FOLDER)
    if (!folder.exists() && !folder.mkdirs()) {
      return null
    }
    return folder
  }
}