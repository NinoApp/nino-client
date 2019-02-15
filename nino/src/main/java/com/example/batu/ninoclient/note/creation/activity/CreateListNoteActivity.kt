package com.example.batu.ninoclient.note.creation.activity

import com.example.batu.ninoclient.core.format.FormatType

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
