package com.nino.ninoclient.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.nino.ninoclient.MainActivity
import com.nino.ninoclient.R
import com.nino.ninoclient.note.creation.activity.CreateListNoteActivity
import com.nino.ninoclient.note.creation.activity.CreateNoteActivity


class CreateNoteWidgetProvider : AppWidgetProvider() {

  override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
    val N = appWidgetIds.size
    for (i in 0 until N) {
      val appWidgetId = appWidgetIds[i]

      val intent = Intent(context, CreateNoteActivity::class.java)
      val pendingIntent = PendingIntent.getActivity(context, 23100, intent, 0)


      val views = RemoteViews(context.packageName, R.layout.add_note_widget_layout)
      views.setOnClickPendingIntent(R.id.add_note, pendingIntent)

      val intentList = Intent(context, CreateListNoteActivity::class.java)
      val pendingIntentList = PendingIntent.getActivity(context, 23101, intentList, 0)
      views.setOnClickPendingIntent(R.id.add_list, pendingIntentList)


      val intentApp = Intent(context, MainActivity::class.java)
      val pendingIntentApp = PendingIntent.getActivity(context, 23102, intentApp, 0)
      views.setOnClickPendingIntent(R.id.open_app, pendingIntentApp)

      appWidgetManager.updateAppWidget(appWidgetId, views);

    }
  }
}