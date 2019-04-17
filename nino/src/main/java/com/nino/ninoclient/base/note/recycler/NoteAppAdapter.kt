package com.nino.ninoclient.base.note.recycler

import android.content.Context
import com.github.bijoysingh.starter.recyclerview.MultiRecyclerViewAdapter
import com.github.bijoysingh.starter.recyclerview.MultiRecyclerViewControllerItem
import com.nino.ninoclient.R
import com.nino.ninoclient.base.export.recycler.FileImportViewHolder
import com.nino.ninoclient.base.main.recycler.EmptyRecyclerHolder
import com.nino.ninoclient.base.main.recycler.InformationRecyclerHolder
import com.nino.ninoclient.base.main.recycler.ToolbarMainRecyclerHolder
import com.nino.ninoclient.base.note.folder.FolderRecyclerHolder
import com.nino.ninoclient.base.note.selection.recycler.SelectableNoteRecyclerViewHolder
import com.nino.ninoclient.base.support.recycler.RecyclerItem
import java.util.*

class NoteAppAdapter : MultiRecyclerViewAdapter<RecyclerItem> {

  @JvmOverloads constructor(context: Context, staggered: Boolean = false, isTablet: Boolean = false) : super(context, getRecyclerItemControllerList(staggered, isTablet)) {}

  constructor(context: Context, list: List<MultiRecyclerViewControllerItem<RecyclerItem>>) : super(context, list) {}

  override fun getItemViewType(position: Int): Int {
    return items[position].type.ordinal
  }
}

fun getRecyclerItemControllerList(
    staggered: Boolean,
    isTablet: Boolean): List<MultiRecyclerViewControllerItem<RecyclerItem>> {
  val list = ArrayList<MultiRecyclerViewControllerItem<RecyclerItem>>()
  list.add(MultiRecyclerViewControllerItem.Builder<RecyclerItem>()
      .viewType(RecyclerItem.Type.NOTE.ordinal)
      .layoutFile(if (staggered && !isTablet) R.layout.item_note_staggered else R.layout.item_note)
      .holderClass(NoteRecyclerHolder::class.java)
      .build())
  list.add(MultiRecyclerViewControllerItem.Builder<RecyclerItem>()
      .viewType(RecyclerItem.Type.EMPTY.ordinal)
      .layoutFile(R.layout.item_no_notes)
      .holderClass(EmptyRecyclerHolder::class.java)
      .build())
  list.add(MultiRecyclerViewControllerItem.Builder<RecyclerItem>()
      .viewType(RecyclerItem.Type.INFORMATION.ordinal)
      .layoutFile(R.layout.item_information)
      .holderClass(InformationRecyclerHolder::class.java)
      .build())
  list.add(MultiRecyclerViewControllerItem.Builder<RecyclerItem>()
      .viewType(RecyclerItem.Type.FILE.ordinal)
      .layoutFile(R.layout.item_import_file)
      .holderClass(FileImportViewHolder::class.java)
      .build())
  list.add(MultiRecyclerViewControllerItem.Builder<RecyclerItem>()
      .viewType(RecyclerItem.Type.FOLDER.ordinal)
      .layoutFile(R.layout.item_folder)
      .holderClass(FolderRecyclerHolder::class.java)
      .build())
  list.add(MultiRecyclerViewControllerItem.Builder<RecyclerItem>()
      .viewType(RecyclerItem.Type.TOOLBAR.ordinal)
      .layoutFile(R.layout.toolbar_main)
      .holderClass(ToolbarMainRecyclerHolder::class.java)
      .build())
  return list
}

fun getSelectableRecyclerItemControllerList(
    staggered: Boolean,
    isTablet: Boolean): List<MultiRecyclerViewControllerItem<RecyclerItem>> {
  val list = ArrayList<MultiRecyclerViewControllerItem<RecyclerItem>>()
  list.add(MultiRecyclerViewControllerItem.Builder<RecyclerItem>()
      .viewType(RecyclerItem.Type.NOTE.ordinal)
      .layoutFile(if (staggered && !isTablet) R.layout.item_note_staggered else R.layout.item_note)
      .holderClass(SelectableNoteRecyclerViewHolder::class.java)
      .build())
  list.add(MultiRecyclerViewControllerItem.Builder<RecyclerItem>()
      .viewType(RecyclerItem.Type.EMPTY.ordinal)
      .layoutFile(R.layout.item_no_notes)
      .holderClass(EmptyRecyclerHolder::class.java)
      .spanSize(2)
      .build())
  return list
}