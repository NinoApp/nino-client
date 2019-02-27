package com.nino.ninoclient.base.note.formats

import com.github.bijoysingh.starter.recyclerview.MultiRecyclerViewControllerItem
import com.nino.ninoclient.R
import com.nino.ninoclient.base.core.format.Format
import com.nino.ninoclient.base.core.format.FormatType
import com.nino.ninoclient.base.note.formats.recycler.*
import java.util.*

fun getFormatControllerItems(): List<MultiRecyclerViewControllerItem<Format>> {
  val list = ArrayList<MultiRecyclerViewControllerItem<Format>>()
  list.add(
      MultiRecyclerViewControllerItem.Builder<Format>()
          .viewType(FormatType.TAG.ordinal)
          .layoutFile(R.layout.item_format_tag)
          .holderClass(FormatTextViewHolder::class.java)
          .build())
  list.add(
      MultiRecyclerViewControllerItem.Builder<Format>()
          .viewType(FormatType.TEXT.ordinal)
          .layoutFile(R.layout.item_format_text)
          .holderClass(FormatTextViewHolder::class.java)
          .build())
  list.add(
      MultiRecyclerViewControllerItem.Builder<Format>()
          .viewType(FormatType.HEADING.ordinal)
          .layoutFile(R.layout.item_format_heading)
          .holderClass(FormatTextViewHolder::class.java)
          .build())
  list.add(
      MultiRecyclerViewControllerItem.Builder<Format>()
          .viewType(FormatType.SUB_HEADING.ordinal)
          .layoutFile(R.layout.item_format_heading)
          .holderClass(FormatTextViewHolder::class.java)
          .build())
  list.add(
      MultiRecyclerViewControllerItem.Builder<Format>()
          .viewType(FormatType.HEADING_3.ordinal)
          .layoutFile(R.layout.item_format_heading)
          .holderClass(FormatTextViewHolder::class.java)
          .build())
  list.add(
      MultiRecyclerViewControllerItem.Builder<Format>()
          .viewType(FormatType.QUOTE.ordinal)
          .layoutFile(R.layout.item_format_quote)
          .holderClass(FormatTextViewHolder::class.java)
          .build())
  list.add(
      MultiRecyclerViewControllerItem.Builder<Format>()
          .viewType(FormatType.CODE.ordinal)
          .layoutFile(R.layout.item_format_code)
          .holderClass(FormatTextViewHolder::class.java)
          .build())
  list.add(
      MultiRecyclerViewControllerItem.Builder<Format>()
          .viewType(FormatType.CHECKLIST_CHECKED.ordinal)
          .layoutFile(R.layout.item_format_list)
          .holderClass(FormatListViewHolder::class.java)
          .build())
  list.add(
      MultiRecyclerViewControllerItem.Builder<Format>()
          .viewType(FormatType.CHECKLIST_UNCHECKED.ordinal)
          .layoutFile(R.layout.item_format_list)
          .holderClass(FormatListViewHolder::class.java)
          .build())
  list.add(
      MultiRecyclerViewControllerItem.Builder<Format>()
          .viewType(FormatType.IMAGE.ordinal)
          .layoutFile(R.layout.item_format_image)
          .holderClass(FormatImageViewHolder::class.java)
          .build())
  list.add(
      MultiRecyclerViewControllerItem.Builder<Format>()
          .viewType(FormatType.SEPARATOR.ordinal)
          .layoutFile(R.layout.item_format_separator)
          .holderClass(FormatSeparatorViewHolder::class.java)
          .build())
  list.add(
      MultiRecyclerViewControllerItem.Builder<Format>()
          .viewType(FormatType.BULLET_1.ordinal)
          .layoutFile(R.layout.item_format_bullet)
          .holderClass(FormatBulletViewHolder::class.java)
          .build())
  list.add(
      MultiRecyclerViewControllerItem.Builder<Format>()
          .viewType(FormatType.BULLET_2.ordinal)
          .layoutFile(R.layout.item_format_bullet)
          .holderClass(FormatBulletViewHolder::class.java)
          .build())
  list.add(
      MultiRecyclerViewControllerItem.Builder<Format>()
          .viewType(FormatType.BULLET_3.ordinal)
          .layoutFile(R.layout.item_format_bullet)
          .holderClass(FormatBulletViewHolder::class.java)
          .build())
  list.add(
      MultiRecyclerViewControllerItem.Builder<Format>()
          .viewType(FormatType.EMPTY.ordinal)
          .layoutFile(R.layout.item_format_fab_space)
          .holderClass(NullFormatHolder::class.java)
          .build())
  return list
}
