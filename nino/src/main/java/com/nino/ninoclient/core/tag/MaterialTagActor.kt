package com.nino.ninoclient.core.tag

import com.nino.ninoclient.config.CoreConfig
import com.nino.ninoclient.database.room.tag.Tag
import com.nino.ninoclient.export.data.ExportableTag

open class MaterialTagActor(val tag: Tag) : ITagActor {
  override fun offlineSave() {
    val id = CoreConfig.instance.tagsDatabase().database().insertTag(tag)
    tag.uid = if (tag.isUnsaved()) id.toInt() else tag.uid
    CoreConfig.instance.tagsDatabase().notifyInsertTag(tag)
  }

  override fun onlineSave() {
    CoreConfig.instance.externalFolderSync().insert(ExportableTag(tag))
  }

  override fun save() {
    offlineSave()
    onlineSave()
  }

  override fun offlineDelete() {
    if (tag.isUnsaved()) {
      return
    }
    CoreConfig.instance.tagsDatabase().database().delete(tag)
    CoreConfig.instance.tagsDatabase().notifyDelete(tag)
    tag.uid = 0
  }

  override fun delete() {
    offlineDelete()
    CoreConfig.instance.externalFolderSync().remove(ExportableTag(tag))
  }

}