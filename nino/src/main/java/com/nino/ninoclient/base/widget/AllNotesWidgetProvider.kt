package com.nino.ninoclient.base.widget

import android.app.Application
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import com.nino.ninoclient.R
import android.app.PendingIntent
import android.content.ComponentName
import com.nino.ninoclient.MainActivity
import com.nino.ninoclient.base.note.creation.activity.CreateListNoteActivity
import com.nino.ninoclient.base.note.creation.activity.CreateNoteActivity
import com.nino.ninoclient.base.note.creation.activity.ViewAdvancedNoteActivity


const val STORE_KEY_ALL_NOTE_WIDGET = "all_note_widget"

class AllNotesWidgetProvider : AppWidgetProvider() {

  override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
    val N = appWidgetIds.size
    for (i in 0 until N) {
      val appWidgetId = appWidgetIds[i]

      val views = RemoteViews(
          context.packageName,
          R.layout.widget_layout_all_notes
      )
      val intent = Intent(context, AllNotesWidgetService::class.java)
      intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i])
      intent.data = Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME))
      views.setRemoteAdapter(R.id.list, intent)

      val noteIntent = Intent(context, ViewAdvancedNoteActivity::class.java)
      noteIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i])
      val notePendingIntent = PendingIntent.getActivity(context, 0, noteIntent, PendingIntent.FLAG_UPDATE_CURRENT)
      views.setPendingIntentTemplate(R.id.list, notePendingIntent)

      val createNoteIntent = Intent(context, CreateNoteActivity::class.java)
      val createNotePendingIntent = PendingIntent.getActivity(context, 23214, createNoteIntent, PendingIntent.FLAG_UPDATE_CURRENT)
      views.setOnClickPendingIntent(R.id.add_note, createNotePendingIntent)

      val createListNoteIntent = Intent(context, CreateListNoteActivity::class.java)
      val createListNotePendingIntent = PendingIntent.getActivity(context, 13123, createListNoteIntent, PendingIntent.FLAG_UPDATE_CURRENT)
      views.setOnClickPendingIntent(R.id.add_list, createListNotePendingIntent)

      val mainIntent = Intent(context, MainActivity::class.java)
      val mainPendingIntent = PendingIntent.getActivity(context, 13124, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT)
      views.setOnClickPendingIntent(R.id.app_icon, mainPendingIntent)

      appWidgetManager.updateAppWidget(appWidgetId, views)
    }
  }

  companion object {
    fun notifyAllChanged(context: Context) {
      val application: Application = context.applicationContext as Application
      val ids = AppWidgetManager.getInstance(application).getAppWidgetIds(
          ComponentName(application, AllNotesWidgetProvider::class.java))
      if (ids.isEmpty()) {
        return
      }

      AppWidgetManager.getInstance(application).notifyAppWidgetViewDataChanged(ids, R.id.list)
    }
  }
}