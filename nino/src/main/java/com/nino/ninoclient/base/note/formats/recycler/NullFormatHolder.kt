package com.nino.ninoclient.base.note.formats.recycler

import android.content.Context
import android.os.Bundle
import android.view.View
import com.github.bijoysingh.starter.recyclerview.RecyclerViewHolder
import com.nino.ninoclient.base.core.format.Format

class NullFormatHolder(context: Context, itemView: View) : RecyclerViewHolder<Format>(context, itemView) {

  override fun populate(data: Format, extra: Bundle) {
  }
}
