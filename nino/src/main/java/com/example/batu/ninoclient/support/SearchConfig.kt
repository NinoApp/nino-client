package com.example.batu.ninoclient.support

import com.example.batu.ninoclient.config.CoreConfig.Companion.foldersDb
import com.example.batu.ninoclient.config.CoreConfig.Companion.notesDb
import com.example.batu.ninoclient.core.note.NoteState
import com.example.batu.ninoclient.core.note.sort
import com.example.batu.ninoclient.database.room.folder.Folder
import com.example.batu.ninoclient.database.room.note.Note
import com.example.batu.ninoclient.database.room.tag.Tag
import com.example.batu.ninoclient.main.HomeNavigationState
import com.example.batu.ninoclient.note.getFullText
import com.example.batu.ninoclient.settings.sheet.SortingOptionsBottomSheet

class SearchConfig(
    var text: String = "",
    var mode: HomeNavigationState = HomeNavigationState.DEFAULT,
    var colors: MutableList<Int> = emptyList<Int>().toMutableList(),
    var tags: MutableList<Tag> = emptyList<Tag>().toMutableList(),
    var folders: MutableList<Folder> = emptyList<Folder>().toMutableList()) {

  fun hasFolder(folder: Folder) = folders.firstOrNull { it.uuid == folder.uuid } !== null

  fun hasFilter(): Boolean {
    return folders.isNotEmpty()
        || tags.isNotEmpty()
        || colors.isNotEmpty()
        || text.isNotBlank()
        || mode !== HomeNavigationState.DEFAULT;
  }

  fun clear(): SearchConfig {
    mode = HomeNavigationState.DEFAULT
    text = ""
    colors.clear()
    tags.clear()
    folders.clear()
    return this
  }

  fun resetMode(state: HomeNavigationState): SearchConfig {
    mode = state
    return this
  }

  fun copy(): SearchConfig {
    return SearchConfig(
        text,
        mode,
        colors.filter { true }.toMutableList(),
        tags.filter { true }.toMutableList(),
        folders.filter { true }.toMutableList())
  }
}

fun unifiedSearchSynchronous(config: SearchConfig): List<Note> {
  val sorting = SortingOptionsBottomSheet.getSortingState()
  val notes = filterSearchWithoutFolder(config)
      .filter {
        when (config.folders.isEmpty()) {
          true -> it.folder.isBlank()
          false -> config.folders.map { it.uuid }.contains(it.folder)
        }
      }
  return sort(notes, sorting)
}

private fun filterSearchWithoutFolder(config: SearchConfig): List<Note> {
  return getNotesForMode(config)
      .filter { config.colors.isEmpty() || config.colors.contains(it.color) }
      .filter { note -> config.tags.isEmpty() || config.tags.filter { note.tags !== null && note.tags.contains(it.uuid) }.isNotEmpty() }
      .filter {
        when {
          config.text.isBlank() -> true
          it.locked -> false
          else -> it.getFullText().contains(config.text, true)
        }
      }
}

fun unifiedFolderSearchSynchronous(config: SearchConfig): List<Folder> {
  if (!config.folders.isEmpty()) {
    return emptyList()
  }
  if (config.text.isNotBlank() || config.tags.isNotEmpty()) {
    val folders = HashSet<Folder>()
    if (config.text.isNotBlank()) {
      folders.addAll(
          foldersDb.getAll()
              .filter { config.colors.isEmpty() || config.colors.contains(it.color) }
              .filter { it.title.contains(config.text, true) })
    }
    folders.addAll(
        filterSearchWithoutFolder(config)
            .filter { it.folder.isNotBlank() }
            .map { it.folder }
            .distinct()
            .map { foldersDb.getByUUID(it) }
            .filterNotNull())
    return folders.toList()
  }
  return foldersDb.getAll()
      .filter {
        config.colors.isEmpty()
            || config.colors.contains(it.color)
            || notesDb.getNotesByFolder(it.uuid).filter { config.colors.contains(it.color) }.isNotEmpty()
      }
      .filter {
        when {
          config.text.isBlank() -> true
          else -> it.title.contains(config.text, true)
        }
      }
}

fun getNotesForMode(config: SearchConfig): List<Note> {
  return when (config.mode) {
    HomeNavigationState.FAVOURITE -> notesDb.getByNoteState(arrayOf(NoteState.FAVOURITE.name))
    HomeNavigationState.ARCHIVED -> notesDb.getByNoteState(arrayOf(NoteState.ARCHIVED.name))
    HomeNavigationState.TRASH -> notesDb.getByNoteState(arrayOf(NoteState.TRASH.name))
    HomeNavigationState.DEFAULT -> notesDb.getByNoteState(arrayOf(NoteState.DEFAULT.name, NoteState.FAVOURITE.name))
    else -> throw Exception("Invalid Search Mode")
  }
}