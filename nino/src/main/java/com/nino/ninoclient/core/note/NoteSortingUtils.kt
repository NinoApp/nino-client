package com.nino.ninoclient.core.note

import com.nino.ninoclient.database.room.note.Note
import com.nino.ninoclient.note.getFullText

enum class SortingTechnique() {
  LAST_MODIFIED,
  NEWEST_FIRST,
  OLDEST_FIRST,
  ALPHABETICAL,
}

fun sort(notes: List<Note>, sortingTechnique: SortingTechnique): List<Note> {
  // Notes returned from DB are always sorted newest first. Reduce computational load
  return when (sortingTechnique) {
    SortingTechnique.LAST_MODIFIED -> notes.sortedByDescending { note ->
      if (note.pinned) Long.MAX_VALUE
      else note.updateTimestamp
    }
    SortingTechnique.OLDEST_FIRST -> notes.sortedBy { note ->
      if (note.pinned) Long.MIN_VALUE
      else note.timestamp
    }
    SortingTechnique.ALPHABETICAL -> notes.sortedBy { note ->
      val content = note.getFullText()
      if (note.pinned || content.isBlank()) 0
      else content[0].toInt()
    }
    else -> notes.sortedByDescending { note ->
      if (note.pinned) Long.MAX_VALUE
      else note.timestamp
    }
  }
}