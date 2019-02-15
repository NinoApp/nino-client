package com.example.batu.ninoclient.note.formats

import android.content.Context
import com.github.bijoysingh.starter.recyclerview.MultiRecyclerViewControllerItem
import com.example.batu.ninoclient.core.format.Format

interface IFormatRecyclerViewActivity {
  fun context(): Context

  fun deleteFormat(format: Format)

  fun controllerItems(): List<MultiRecyclerViewControllerItem<Format>>

  fun moveFormat(fromPosition: Int, toPosition: Int)
}