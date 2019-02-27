package com.nino.ninoclient.base.note.formats.recycler

import android.content.Context
import android.view.View
import android.widget.ImageView
import com.nino.ninoclient.R
import com.nino.ninoclient.base.core.format.Format
import com.nino.ninoclient.base.note.creation.sheet.FormatActionBottomSheet
import com.nino.ninoclient.base.note.creation.sheet.sEditorMoveHandles
import com.nino.ninoclient.base.support.ui.visibility

class FormatSeparatorViewHolder(context: Context, view: View) : FormatViewHolderBase(context, view) {

  val separator: View = root.findViewById(R.id.separator)
  val actionMove: ImageView = root.findViewById(R.id.action_move_icon)

  override fun populate(data: Format, config: FormatViewHolderConfig) {

    separator.setBackgroundColor(config.hintTextColor)

    actionMove.setColorFilter(config.iconColor)
    actionMove.visibility = visibility(config.editable)
    actionMove.setOnClickListener {
      FormatActionBottomSheet.openSheet(activity, config.noteUUID, data)
    }
    if (config.editable && !sEditorMoveHandles) {
      actionMove.visibility = View.INVISIBLE
    }
  }
}