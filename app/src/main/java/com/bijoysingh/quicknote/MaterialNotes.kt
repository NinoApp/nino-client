package com.bijoysingh.quicknote

import android.content.Context
import com.maubis.scarlet.base.config.ApplicationBase
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.config.MaterialNoteConfig
import com.maubis.scarlet.base.export.support.ExternalFolderSync
import android.support.multidex.MultiDex



class MaterialNotes : ApplicationBase() {

  override fun onCreate() {
    super.onCreate()
    CoreConfig.instance = MaterialNoteConfig(this)
    CoreConfig.instance.themeController().setup(this)
    ExternalFolderSync.setup(this)
  }

  override fun attachBaseContext(base: Context) {
    super.attachBaseContext(base)
    MultiDex.install(this)
  }

}