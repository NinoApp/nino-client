package com.nino.ninoclient.base.database.remote

import android.content.Context
import com.github.bijoysingh.starter.util.TextUtils
import com.nino.ninoclient.base.config.CoreConfig
import com.nino.ninoclient.base.core.folder.FolderBuilder
import com.nino.ninoclient.base.core.folder.IFolderContainer
import com.nino.ninoclient.base.core.note.INoteContainer
import com.nino.ninoclient.base.core.note.NoteBuilder
import com.nino.ninoclient.base.core.note.isEqual
import com.nino.ninoclient.base.core.tag.ITagContainer
import com.nino.ninoclient.base.core.tag.TagBuilder
import com.nino.ninoclient.base.note.deleteWithoutSync
import com.nino.ninoclient.base.note.folder.deleteWithoutSync
import com.nino.ninoclient.base.note.folder.saveWithoutSync
import com.nino.ninoclient.base.note.save
import com.nino.ninoclient.base.note.saveWithoutSync
import com.nino.ninoclient.base.note.tag.deleteWithoutSync
import com.nino.ninoclient.base.note.tag.saveWithoutSync
import com.nino.ninoclient.base.service.NoteBroadcast
import com.nino.ninoclient.base.service.sendNoteBroadcast

object IRemoteDatabaseUtils {
  fun onRemoteInsert(context: Context, note: INoteContainer) {
    val notifiedNote = NoteBuilder().copy(note)
    val existingNote = CoreConfig.notesDb.existingMatch(note)
    val isSameAsExisting = existingNote !== null && notifiedNote.isEqual(existingNote)

    if (existingNote === null) {
      notifiedNote.saveWithoutSync(context)
      sendNoteBroadcast(context, NoteBroadcast.NOTE_CHANGED, note.uuid())
      return
    }
    if (!isSameAsExisting) {
      notifiedNote.uid = existingNote.uid

      val noteToSave = NoteBuilder().copy(notifiedNote)
      noteToSave.saveWithoutSync(context)
      sendNoteBroadcast(context, NoteBroadcast.NOTE_CHANGED, existingNote.uuid)
    }
  }

  fun onRemoteInsert(context: Context, tag: ITagContainer) {
    val notifiedTag = TagBuilder().copy(tag)
    val existingTag = CoreConfig.tagsDb.getByUUID(tag.uuid())
    var isSameAsExisting = existingTag !== null
        && TextUtils.areEqualNullIsEmpty(notifiedTag.title, existingTag.title)

    if (existingTag === null) {
      notifiedTag.saveWithoutSync()
      sendNoteBroadcast(context, NoteBroadcast.TAG_CHANGED, tag.uuid())
      return
    }
    if (!isSameAsExisting) {
      existingTag.title = tag.title()
      existingTag.saveWithoutSync()
      sendNoteBroadcast(context, NoteBroadcast.TAG_CHANGED, existingTag.uuid)
    }
  }

  fun onRemoteInsert(context: Context, folder: IFolderContainer) {
    val notifiedFolder = FolderBuilder().copy(folder)
    val existingFolder = CoreConfig.foldersDb.getByUUID(folder.uuid())
    var isSameAsExisting = existingFolder !== null
        && TextUtils.areEqualNullIsEmpty(notifiedFolder.title, existingFolder.title)
        && (notifiedFolder.color == existingFolder.color)
        && (notifiedFolder.timestamp == existingFolder.timestamp)
        && (notifiedFolder.updateTimestamp == existingFolder.updateTimestamp)

    if (existingFolder === null) {
      notifiedFolder.saveWithoutSync()
      sendNoteBroadcast(context, NoteBroadcast.FOLDER_CHANGED, folder.uuid())
      return
    }
    if (!isSameAsExisting) {
      existingFolder.title = folder.title()
      existingFolder.color = folder.color()
      existingFolder.timestamp = folder.timestamp()
      existingFolder.updateTimestamp = folder.updateTimestamp()
      existingFolder.saveWithoutSync()
      sendNoteBroadcast(context, NoteBroadcast.FOLDER_CHANGED, existingFolder.uuid)
    }
  }

  fun onRemoteRemove(context: Context, note: INoteContainer) {
    val existingNote = CoreConfig.notesDb.existingMatch(note)
    if (existingNote !== null && !existingNote.disableBackup) {
      existingNote.deleteWithoutSync(context)
      sendNoteBroadcast(context, NoteBroadcast.NOTE_DELETED, existingNote.uuid)
    }
  }

  fun onRemoteRemove(context: Context, tag: ITagContainer) {
    val existingTag = CoreConfig.tagsDb.getByUUID(tag.uuid())
    if (existingTag !== null) {
      existingTag.deleteWithoutSync()
      sendNoteBroadcast(context, NoteBroadcast.TAG_DELETED, existingTag.uuid)
    }
  }

  fun onRemoteRemove(context: Context, folder: IFolderContainer) {
    val existingFolder = CoreConfig.foldersDb.getByUUID(folder.uuid())
    if (existingFolder !== null) {
      existingFolder.deleteWithoutSync()
      CoreConfig.notesDb.getAll().filter { it.folder == existingFolder.uuid }.forEach {
        it.folder = ""
        it.save(context)
      }
      sendNoteBroadcast(context, NoteBroadcast.FOLDER_DELETED, existingFolder.uuid)
    }
  }
}