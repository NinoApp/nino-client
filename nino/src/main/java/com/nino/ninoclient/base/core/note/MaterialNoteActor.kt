package com.nino.ninoclient.base.core.note

import android.app.NotificationManager
import android.content.Context
import android.os.AsyncTask
import android.os.Build
import android.support.v7.app.AppCompatActivity
import com.github.bijoysingh.starter.util.IntentUtils
import com.github.bijoysingh.starter.util.TextUtils
import com.nino.ninoclient.R
import com.nino.ninoclient.base.config.CoreConfig
import com.nino.ninoclient.base.config.CoreConfig.Companion.notesDb
import com.nino.ninoclient.base.database.room.note.Note
import com.nino.ninoclient.base.core.format.FormatBuilder
import com.nino.ninoclient.base.export.data.ExportableNote
import com.nino.ninoclient.base.main.activity.WidgetConfigureActivity
import com.nino.ninoclient.base.note.*
import com.nino.ninoclient.base.notification.NotificationConfig
import com.nino.ninoclient.base.notification.NotificationHandler
import com.nino.ninoclient.base.widget.AllNotesWidgetProvider.Companion.notifyAllChanged
import com.nino.ninoclient.base.service.FloatingNoteService
import java.util.*

open class MaterialNoteActor(val note: Note) : INoteActor {
  override fun copy(context: Context) {
    TextUtils.copyToClipboard(context, note.getFullText())
  }

  override fun share(context: Context) {
    IntentUtils.ShareBuilder(context)
        .setSubject(note.getTitle())
        .setText(note.getText())
        .setChooserText(context.getString(R.string.share_using))
        .share()
  }

  override fun popup(activity: AppCompatActivity) {
    FloatingNoteService.openNote(activity, note, true)
  }

  override fun offlineSave(context: Context) {
    val id = notesDb.database().insertNote(note)
    note.uid = if (note.isUnsaved()) id.toInt() else note.uid
    notesDb.notifyInsertNote(note)
    AsyncTask.execute {
      onNoteUpdated(context)
    }
  }

  override fun onlineSave(context: Context) {
    CoreConfig.instance.externalFolderSync().insert(ExportableNote(note))
  }

  override fun save(context: Context) {
    offlineSave(context)
    onlineSave(context)
  }

  override fun softDelete(context: Context) {
    if (note.getNoteState() === NoteState.TRASH) {
      delete(context)
      return
    }
    note.mark(context, NoteState.TRASH)
  }

  override fun offlineDelete(context: Context) {
    NoteImage(context).deleteAllFiles(note)
    if (note.isUnsaved()) {
      return
    }
    notesDb.database().delete(note)
    notesDb.notifyDelete(note)
    note.description = FormatBuilder().getDescription(ArrayList())
    note.uid = 0
    AsyncTask.execute {
      onNoteDestroyed(context)
    }
  }

  override fun disableBackup(activity: AppCompatActivity) {
    note.disableBackup = true
    note.saveWithoutSync(activity)
    note.deleteToSync(activity)
  }

  override fun enableBackup(activity: AppCompatActivity) {
    note.disableBackup = false
    note.save(activity)
  }


  override fun onlineDelete(context: Context) {
    CoreConfig.instance.externalFolderSync().remove(ExportableNote(note))
  }

  override fun delete(context: Context) {
    offlineDelete(context)
    onlineDelete(context)
  }

  protected fun onNoteDestroyed(context: Context) {
    WidgetConfigureActivity.notifyNoteChange(context, note)
    notifyAllChanged(context)
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
    notificationManager?.cancel(note.uid)
  }

  protected fun onNoteUpdated(context: Context) {
    WidgetConfigureActivity.notifyNoteChange(context, note)
    notifyAllChanged(context)
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
    if (Build.VERSION.SDK_INT >= 23 && notificationManager != null) {
      for (notification in notificationManager.activeNotifications) {
        if (notification.id == note.uid) {
          val handler = NotificationHandler(context)
          handler.openNotification(NotificationConfig(note = note))
        }
      }
    }
  }

}
