package com.example.batu.ninoclient.config

import android.app.Application
import com.evernote.android.job.JobManager
import com.facebook.soloader.SoLoader
import com.example.batu.ninoclient.note.reminders.ReminderJobCreator
import com.example.batu.ninoclient.support.utils.ImageCache

abstract class ApplicationBase : Application() {
  override fun onCreate() {
    super.onCreate()
    SoLoader.init(this, false)
    JobManager.create(this).addJobCreator(ReminderJobCreator())
  }
}