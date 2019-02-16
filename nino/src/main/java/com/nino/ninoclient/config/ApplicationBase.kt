package com.nino.ninoclient.config

import android.app.Application
import com.evernote.android.job.JobManager
import com.facebook.soloader.SoLoader
import com.nino.ninoclient.note.reminders.ReminderJobCreator

abstract class ApplicationBase : Application() {
  override fun onCreate() {
    super.onCreate()
    SoLoader.init(this, false)
    JobManager.create(this).addJobCreator(ReminderJobCreator())
  }
}