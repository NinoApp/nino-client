package com.example.batu.ninoclient.note.folder

import android.content.Context
import android.os.Bundle
import android.support.v7.widget.CardView
import android.view.View
import android.widget.TextView
import com.github.bijoysingh.starter.recyclerview.RecyclerViewHolder
import com.github.bijoysingh.uibasics.views.UITextView
import com.example.batu.ninoclient.R
import com.example.batu.ninoclient.support.recycler.RecyclerItem

class FolderRecyclerHolder(context: Context, view: View) : RecyclerViewHolder<RecyclerItem>(context, view) {

  protected val view: CardView
  protected val label: UITextView
  protected val title: TextView
  protected val timestamp: TextView

  init {
    this.view = view as CardView
    title = view.findViewById(R.id.title)
    timestamp = view.findViewById(R.id.timestamp)
    label = view.findViewById(R.id.ui_information_title)
  }

  override fun populate(itemData: RecyclerItem, extra: Bundle?) {
    val item = itemData as FolderRecyclerItem
    title.text = item.title
    title.setTextColor(item.titleColor)

    label.setText(item.label)
    label.setImageTint(item.labelColor)
    label.setTextColor(item.labelColor)

    timestamp.text = item.timestamp
    timestamp.setTextColor(item.timestampColor)

    view.setCardBackgroundColor(item.folder.color)
    view.setOnClickListener {
      item.click()
    }
    view.setOnLongClickListener {
      item.longClick()
      return@setOnLongClickListener false
    }

    when (item.selected) {
      true -> {
        view.alpha = 0.5f
        label.visibility = View.GONE
        timestamp.visibility = View.GONE
        title.minLines = 1
      }
      false -> {
        view.alpha = 1.0f
        label.visibility = View.VISIBLE
        timestamp.visibility = View.VISIBLE
        title.minLines = 1
      }
    }
    view.alpha = if (item.selected) 0.5f else 1.0f
  }
}