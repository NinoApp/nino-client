package com.nino.ninoclient.core.folder

import com.nino.ninoclient.config.CoreConfig
import com.nino.ninoclient.database.room.folder.Folder
import com.nino.ninoclient.export.data.ExportableFolder

open class MaterialFolderActor(val folder: Folder) : IFolderActor {
  override fun offlineSave() {
    val id = CoreConfig.instance.foldersDatabase().database().insertFolder(folder)
    folder.uid = if (folder.isUnsaved()) id.toInt() else folder.uid

    CoreConfig.instance.foldersDatabase().notifyInsertFolder(folder)
  }

  override fun onlineSave() {
    CoreConfig.instance.externalFolderSync().insert(ExportableFolder(folder))
  }

  override fun save() {
    offlineSave()
    onlineSave()
  }

  override fun offlineDelete() {
    if (folder.isUnsaved()) {
      return
    }
    CoreConfig.instance.foldersDatabase().database().delete(folder)
    CoreConfig.instance.foldersDatabase().notifyDelete(folder)
    folder.uid = 0
  }

  override fun delete() {
    offlineDelete()
    CoreConfig.instance.externalFolderSync().remove(ExportableFolder(folder))
  }

}