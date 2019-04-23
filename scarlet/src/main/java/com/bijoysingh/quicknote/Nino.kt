package com.bijoysingh.quicknote

import com.bijoysingh.quicknote.firebase.FirebaseRemoteDatabase
import com.bijoysingh.quicknote.scarlet.ScarletConfig
import com.maubis.scarlet.base.config.ApplicationBase
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.export.support.ExternalFolderSync

class Nino : ApplicationBase() {

  override fun onCreate() {
    super.onCreate()
    CoreConfig.instance = ScarletConfig(this)
    CoreConfig.instance.themeController().setup(this)
    CoreConfig.instance.authenticator().setup(this)
    CoreConfig.instance.remoteConfigFetcher().setup(this)
    ExternalFolderSync.setup(this)
  }

  companion object {
    var firebase: FirebaseRemoteDatabase? = null
  }
}