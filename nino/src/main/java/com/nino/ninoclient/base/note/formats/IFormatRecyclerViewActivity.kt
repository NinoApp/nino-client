package com.nino.ninoclient.base.note.formats

import android.content.Context
import com.github.bijoysingh.starter.recyclerview.MultiRecyclerViewControllerItem
import com.nino.ninoclient.base.core.format.Format

interface IFormatRecyclerViewActivity {
  fun context(): Context

  fun deleteFormat(format: Format)

  fun controllerItems(): List<MultiRecyclerViewControllerItem<Format>>

  fun moveFormat(fromPosition: Int, toPosition: Int)
}