package com.nino.ninoclient.note.creation.activity

import com.nino.ninoclient.core.format.FormatType

class CreateListNoteActivity : CreateNoteActivity() {

  override fun addDefaultItem() {
    addEmptyItem(FormatType.CHECKLIST_UNCHECKED)
    addEmptyItem(FormatType.CHECKLIST_UNCHECKED)
    addEmptyItem(FormatType.CHECKLIST_UNCHECKED)
    addEmptyItem(FormatType.CHECKLIST_UNCHECKED)
    addEmptyItem(FormatType.CHECKLIST_UNCHECKED)
    addEmptyItem(FormatType.CHECKLIST_UNCHECKED)
    addEmptyItem(FormatType.CHECKLIST_UNCHECKED)
    addEmptyItem(FormatType.CHECKLIST_UNCHECKED)
    addEmptyItem(FormatType.CHECKLIST_UNCHECKED)
  }
}
