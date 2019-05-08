package com.maubis.scarlet.base.note.creation.specs

import android.util.Log
import com.facebook.litho.Component
import com.facebook.litho.ComponentContext
import com.facebook.litho.Row
import com.facebook.litho.annotations.LayoutSpec
import com.facebook.litho.annotations.OnCreateLayout
import com.facebook.litho.annotations.Prop
import com.facebook.yoga.YogaAlign
import com.maubis.scarlet.base.core.format.FormatType
import com.maubis.scarlet.base.core.note.getFormats
import com.maubis.scarlet.base.note.creation.activity.CreateNoteActivity
import com.maubis.scarlet.base.note.creation.activity.NoteViewColorConfig
import com.maubis.scarlet.base.note.getSmartFormats
import com.maubis.scarlet.base.support.specs.EmptySpec
import com.maubis.scarlet.base.support.specs.ToolbarColorConfig

@LayoutSpec
object NoteViewTopBarSpec {
  @OnCreateLayout
  fun onCreate(context: ComponentContext): Component {
    val row = Row.create(context)
        .widthPercent(100f)
        .alignItems(YogaAlign.CENTER)
    row.child(EmptySpec.create(context).heightDip(10f))
    return row.build()
  }
}

@LayoutSpec
object NoteCreationTopBarSpec {
  @OnCreateLayout
  fun onCreate(context: ComponentContext,
               @Prop colorConfig: ToolbarColorConfig ): Component {
    val activity = context.androidContext as CreateNoteActivity

    Log.v("NoteCreationTopBar", "NoteCreationTopBar created")
    val row = Row.create(context)
        .widthPercent(100f)
        .alignItems(YogaAlign.CENTER)

    if (activity.focusedFormat != null && (activity.focusedFormat!!.formatType in arrayOf(FormatType.CHECKLIST_CHECKED, FormatType.CHECKLIST_UNCHECKED, FormatType.CODE, FormatType.HEADING, FormatType.HEADING_3, FormatType.SUB_HEADING, FormatType.TEXT, FormatType.QUOTE, FormatType.NUMBERED_LIST))) {
      Log.v("NoteCreationTopBar", activity.focusedFormat!!.formatType.toString())
      Log.v("NoteCreationTopBar", "numformats" + activity.note().getFormats().size.toString())
      Log.v("NoteCreationTopBar", "num smart formats" + activity.note().getSmartFormats().size.toString())
      row.child(NoteCreationAllMarkdownsBottomBar.create(context).colorConfig(colorConfig))
    }

    row.child(EmptySpec.create(context).heightDip(10f))
    return row.build()
  }
}