package com.nino.ninoclient.base.main.activity

import android.app.Activity
import android.app.Application
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.RemoteViews
import com.github.bijoysingh.starter.util.TextUtils
import com.nino.ninoclient.R
import com.nino.ninoclient.base.config.CoreConfig
import com.nino.ninoclient.base.config.CoreConfig.Companion.notesDb
import com.nino.ninoclient.base.core.note.NoteState
import com.nino.ninoclient.base.database.room.note.Note
import com.nino.ninoclient.base.database.room.widget.Widget
import com.nino.ninoclient.base.note.creation.activity.ViewAdvancedNoteActivity
import com.nino.ninoclient.base.note.getLockedText
import com.nino.ninoclient.base.note.getTitle
import com.nino.ninoclient.base.note.selection.activity.INoteSelectorActivity
import com.nino.ninoclient.base.note.selection.activity.SelectableNotesActivityBase
import com.nino.ninoclient.base.support.ui.ColorUtil
import com.nino.ninoclient.base.widget.NoteWidgetProvider

class WidgetConfigureActivity : SelectableNotesActivityBase(), INoteSelectorActivity {

  var appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_select_note)

    val intent = intent
    val extras = intent.extras
    if (extras != null) {
      appWidgetId = extras.getInt(
          AppWidgetManager.EXTRA_APPWIDGET_ID,
          AppWidgetManager.INVALID_APPWIDGET_ID)
    }

    if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
      finish()
      return
    }

    initUI()
  }

  override fun getNotes(): List<Note> {
    return notesDb.getByNoteState(
        arrayOf(NoteState.DEFAULT.name, NoteState.FAVOURITE.name, NoteState.ARCHIVED.name))
        .filter { note -> !note.locked }
  }

  override fun onNoteClicked(note: Note) {
    val widget = Widget(appWidgetId, note.uuid)
    CoreConfig.instance.database().widgets().insert(widget)
    createWidget(widget)
  }

  override fun isNoteSelected(note: Note): Boolean {
    return true
  }

  fun createWidget(widget: Widget) {
    createNoteWidget(this, widget)

    val resultValue = Intent()
    resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widget.widgetId)
    setResult(Activity.RESULT_OK, resultValue)
    finish()
  }

  companion object {
    fun createNoteWidget(context: Context, widget: Widget) {
      val note = notesDb.getByUUID(widget.noteUUID)
      val appWidgetManager = AppWidgetManager.getInstance(context)
      if (note === null || note.locked) {
        val views = RemoteViews(context.getPackageName(), R.layout.widget_invalid_note)
        appWidgetManager.updateAppWidget(widget.widgetId, views)
        return
      }

      val intent = ViewAdvancedNoteActivity.getIntent(context, note)
      val pendingIntent = PendingIntent.getActivity(context, 5000 + note.uid, intent, 0)
      val views = RemoteViews(context.getPackageName(), R.layout.widget_layout)

      val noteTitle = note.getTitle()
      views.setViewVisibility(R.id.title, if (TextUtils.isNullOrEmpty(noteTitle)) GONE else VISIBLE)
      views.setTextViewText(R.id.title, noteTitle)
      views.setTextViewText(R.id.description, note.getLockedText(false))
      views.setInt(R.id.container_layout, "setBackgroundColor", note.color)

      val isLightShaded = ColorUtil.isLightColored(note.color)
      val colorResource = if (isLightShaded) R.color.dark_tertiary_text else R.color.light_secondary_text
      val textColor = ContextCompat.getColor(context, colorResource)
      views.setInt(R.id.title, "setTextColor", textColor)
      views.setInt(R.id.description, "setTextColor", textColor)

      views.setOnClickPendingIntent(R.id.title, pendingIntent)
      views.setOnClickPendingIntent(R.id.description, pendingIntent)
      views.setOnClickPendingIntent(R.id.container_layout, pendingIntent)

      appWidgetManager.updateAppWidget(widget.widgetId, views)
    }

    private fun notifyNoteChangeBroadcast(context: Context, note: Note): Intent? {
      val application: Application = context.applicationContext as Application
      val ids = AppWidgetManager.getInstance(application).getAppWidgetIds(
          ComponentName(application, NoteWidgetProvider::class.java))
      val widgets = CoreConfig.instance.database().widgets().getByNote(note.uuid)

      val widgetIds = ArrayList<Int>()
      for (widget in widgets) {
        if (ids.contains(widget.widgetId)) {
          widgetIds.add(widget.widgetId)
        }
      }

      if (widgetIds.isEmpty()) {
        return null
      }

      val intentIds = IntArray(widgetIds.size, { index -> widgetIds[index] })

      val intent = Intent(application, NoteWidgetProvider::class.java)
      intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
      intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intentIds)
      return intent
    }

    fun notifyNoteChange(context: Context?, note: Note?) {
      if (context === null || note === null) {
        return
      }

      val intent = notifyNoteChangeBroadcast(context, note)
      if (intent === null) {
        return
      }
      context.sendBroadcast(intent);
    }
  }
}