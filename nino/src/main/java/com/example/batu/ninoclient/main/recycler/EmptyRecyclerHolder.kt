package com.example.batu.ninoclient.main.recycler

import android.content.Context
import android.os.Bundle
import android.view.View
import com.github.bijoysingh.starter.recyclerview.RecyclerViewHolder
import com.github.bijoysingh.starter.util.IntentUtils
import com.example.batu.ninoclient.note.creation.activity.CreateNoteActivity
import com.example.batu.ninoclient.support.recycler.RecyclerItem

class EmptyRecyclerHolder(context: Context, itemView: View) : RecyclerViewHolder<RecyclerItem>(context, itemView) {

  override fun populate(data: RecyclerItem, extra: Bundle) {
    setFullSpan()
    itemView.setOnClickListener {
      IntentUtils.startActivity(context, CreateNoteActivity::class.java)
    }
  }
}
