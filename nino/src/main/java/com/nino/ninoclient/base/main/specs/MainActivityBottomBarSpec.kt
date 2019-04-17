package com.nino.ninoclient.base.main.specs

import android.content.Intent
import android.graphics.Color
import android.text.Layout
import com.nino.ninoclient.CameraActivity
import com.facebook.litho.Component
import com.facebook.litho.ComponentContext
import com.facebook.litho.Row
import com.facebook.litho.annotations.LayoutSpec
import com.facebook.litho.annotations.OnCreateLayout
import com.facebook.litho.annotations.Prop
import com.facebook.litho.widget.Text
import com.facebook.yoga.YogaAlign
import com.facebook.yoga.YogaEdge
import com.nino.ninoclient.MainActivity
import com.nino.ninoclient.R
import com.nino.ninoclient.base.config.CoreConfig.Companion.FONT_MONSERRAT
import com.nino.ninoclient.base.core.folder.FolderBuilder
import com.nino.ninoclient.base.database.room.folder.Folder
import com.nino.ninoclient.base.main.sheets.HomeNavigationBottomSheet
import com.nino.ninoclient.base.note.folder.sheet.CreateOrEditFolderBottomSheet
import com.nino.ninoclient.base.settings.sheet.NoteSettingsOptionsBottomSheet
import com.nino.ninoclient.base.support.specs.EmptySpec
import com.nino.ninoclient.base.support.specs.ToolbarColorConfig
import com.nino.ninoclient.base.support.specs.bottomBarCard
import com.nino.ninoclient.base.support.specs.bottomBarRoundIcon
import com.nino.ninoclient.base.support.ui.ColorUtil

@LayoutSpec
object MainActivityBottomBarSpec {
  @OnCreateLayout
  fun onCreate(context: ComponentContext,
               @Prop colorConfig: ToolbarColorConfig): Component {
    val activity = context.androidContext as MainActivity
    val row = Row.create(context)
        .widthPercent(100f)
        .alignItems(YogaAlign.CENTER)
        .paddingDip(YogaEdge.HORIZONTAL, 4f)
    row.child(bottomBarRoundIcon(context, colorConfig)
        .bgColor(Color.TRANSPARENT)
        .iconRes(R.drawable.ic_apps_white_48dp)
        .onClick {
          HomeNavigationBottomSheet.openSheet(activity)
        })
    row.child(EmptySpec.create(context).heightDip(1f).flexGrow(1f))

    row.child(bottomBarRoundIcon(context, colorConfig)
        .iconRes(R.drawable.icon_add_notebook)
        .onClick {
          CreateOrEditFolderBottomSheet.openSheet(
              activity,
              FolderBuilder().emptyFolder(NoteSettingsOptionsBottomSheet.genDefaultColor()),
              { _, _ -> activity.setupData() })
        })/*
    row.child(bottomBarRoundIcon(context, colorConfig)
        .iconRes(R.drawable.icon_add_list)
        .onClick {
          val intent = CreateNoteActivity.getNewChecklistNoteIntent(
              activity,
              activity.config.folders.firstOrNull()?.uuid ?: "")
          activity.startActivity(intent)
        })*/
    row.child(bottomBarRoundIcon(context, colorConfig)
        .iconRes(R.drawable.icon_add_note)
        .onClick {
            val intent = Intent(activity, CameraActivity::class.java)
            activity.startActivity(intent)/*
          val intent = CreateNoteActivity.getNewNoteIntent(
              activity,
              activity.config.folders.firstOrNull()?.uuid ?: "")
          activity.startActivity(intent)*/
        })
    return bottomBarCard(context, row.build(), colorConfig).build()
  }
}

@LayoutSpec
object MainActivityFolderBottomBarSpec {
  @OnCreateLayout
  fun onCreate(context: ComponentContext, @Prop folder: Folder): Component {
    val colorConfig = ToolbarColorConfig(
        toolbarBackgroundColor = folder.color,
        toolbarIconColor = when (ColorUtil.isLightColored(folder.color)) {
          true -> context.getColor(R.color.dark_tertiary_text)
          false -> context.getColor(R.color.light_secondary_text)
        }
    )
    val activity = context.androidContext as MainActivity
    val row = Row.create(context)
        .widthPercent(100f)
        .alignItems(YogaAlign.CENTER)
        .paddingDip(YogaEdge.HORIZONTAL, 4f)
    row.child(bottomBarRoundIcon(context, colorConfig)
        .bgColor(Color.TRANSPARENT)
        .iconRes(R.drawable.ic_close_white_48dp)
        .onClick {
          activity.config.folders.clear()
          activity.unifiedSearch()
          activity.notifyFolderChange()
        })
    row.child(Text.create(context)
        .typeface(FONT_MONSERRAT)
        .textAlignment(Layout.Alignment.ALIGN_CENTER)
        .flexGrow(1f)
        .text(folder.title)
        .textSizeRes(R.dimen.font_size_normal)
        .textColor(colorConfig.toolbarIconColor))
    row.child(bottomBarRoundIcon(context, colorConfig)
        .iconRes(R.drawable.ic_more_options)
        .onClick {
          if (activity.config.folders.isEmpty()) {
            return@onClick
          }
          CreateOrEditFolderBottomSheet.openSheet(activity, folder, { _, _ -> activity.setupData() })
        })
    return bottomBarCard(context, row.build(), colorConfig).build()
  }
}
