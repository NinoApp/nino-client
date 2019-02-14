package com.example.batu.ninoclient;

import android.app.Application;
import android.content.Context
import android.support.multidex.MultiDex
import android.support.multidex.MultiDexApplication
import com.evernote.android.job.JobManager
import com.facebook.soloader.SoLoader
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.config.MaterialNoteConfig
import com.maubis.scarlet.base.export.support.ExternalFolderSync
import com.maubis.scarlet.base.note.reminders.ReminderJobCreator

class Nino : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        SoLoader.init(this, false)
        JobManager.create(this).addJobCreator(ReminderJobCreator())
        CoreConfig.instance = MaterialNoteConfig(this)
        CoreConfig.instance.themeController().setup(this)
        ExternalFolderSync.setup(this)
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }
}
