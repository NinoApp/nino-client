package com.example.batu.ninoclient.database.remote

import android.content.Context
import com.github.bijoysingh.starter.util.TextUtils
import com.example.batu.ninoclient.config.CoreConfig
import com.example.batu.ninoclient.core.folder.FolderBuilder
import com.example.batu.ninoclient.core.folder.IFolderContainer
import com.example.batu.ninoclient.core.note.INoteContainer
import com.example.batu.ninoclient.core.note.NoteBuilder
import com.example.batu.ninoclient.core.note.isEqual
import com.example.batu.ninoclient.core.tag.ITagContainer
import com.example.batu.ninoclient.core.tag.TagBuilder
import com.example.batu.ninoclient.note.deleteWithoutSync
import com.example.batu.ninoclient.note.folder.deleteWithoutSync
import com.example.batu.ninoclient.note.folder.saveWithoutSync
import com.example.batu.ninoclient.note.save
import com.example.batu.ninoclient.note.saveWithoutSync
import com.example.batu.ninoclient.note.tag.deleteWithoutSync
import com.example.batu.ninoclient.note.tag.saveWithoutSync
import com.example.batu.ninoclient.service.NoteBroadcast
import com.example.batu.ninoclient.service.sendNoteBroadcast

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