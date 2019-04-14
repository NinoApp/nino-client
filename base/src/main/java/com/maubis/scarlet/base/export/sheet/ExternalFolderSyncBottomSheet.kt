package com.maubis.scarlet.base.export.sheet

import android.app.Dialog
import android.graphics.Typeface
import com.facebook.litho.Column
import com.facebook.litho.Component
import com.facebook.litho.ComponentContext
import com.facebook.litho.widget.Text
import com.facebook.yoga.YogaEdge
import com.maubis.scarlet.base.MainActivity
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.config.CoreConfig.Companion.FONT_MONSERRAT
import com.maubis.scarlet.base.export.support.sExternalFolderSync
import com.maubis.scarlet.base.export.support.sFolderSyncBackupLocked
import com.maubis.scarlet.base.export.support.sFolderSyncPath
import com.maubis.scarlet.base.support.sheets.LithoBottomSheet
import com.maubis.scarlet.base.support.sheets.LithoOptionsItem
import com.maubis.scarlet.base.support.sheets.OptionItemLayout
import com.maubis.scarlet.base.support.sheets.getLithoBottomSheetTitle
import com.maubis.scarlet.base.support.specs.BottomSheetBar
import com.maubis.scarlet.base.support.specs.separatorSpec
import com.maubis.scarlet.base.support.ui.ThemeColorType
import com.maubis.scarlet.base.support.ui.ThemedActivity


class ExternalFolderSyncBottomSheet : LithoBottomSheet() {

  override fun getComponent(componentContext: ComponentContext, dialog: Dialog): Component {
    val activity = componentContext.androidContext as ThemedActivity

    val component = Column.create(componentContext)
        .widthPercent(100f)
        .paddingDip(YogaEdge.VERTICAL, 8f)
        .child(getLithoBottomSheetTitle(componentContext)
            .textRes(R.string.import_export_layout_folder_sync_title)
            .paddingDip(YogaEdge.HORIZONTAL, 20f)
            .marginDip(YogaEdge.HORIZONTAL, 0f))
        .child(Text.create(componentContext)
            .textSizeRes(R.dimen.font_size_large)
            .textRes(R.string.import_export_layout_folder_sync_description)
            .paddingDip(YogaEdge.HORIZONTAL, 20f)
            .textColor(CoreConfig.instance.themeController().get(ThemeColorType.TERTIARY_TEXT)))
        .child(separatorSpec(componentContext).alpha(0.5f))
        .child(Text.create(componentContext)
            .textSizeRes(R.dimen.font_size_xlarge)
            .typeface(FONT_MONSERRAT)
            .textRes(R.string.import_export_layout_folder_sync_folder)
            .paddingDip(YogaEdge.HORIZONTAL, 20f)
            .textColor(CoreConfig.instance.themeController().get(ThemeColorType.SECTION_HEADER)))
        .child(Text.create(componentContext)
            .textSizeRes(R.dimen.font_size_large)
            .text(sFolderSyncPath)
            .typeface(Typeface.MONOSPACE)
            .paddingDip(YogaEdge.HORIZONTAL, 20f)
            .paddingDip(YogaEdge.VERTICAL, 8f)
            .textColor(CoreConfig.instance.themeController().get(ThemeColorType.TERTIARY_TEXT)))
        .child(separatorSpec(componentContext).alpha(0.5f))

    getOptions(componentContext).forEach {
      if (it.visible) {
        component.child(OptionItemLayout.create(componentContext)
            .option(it)
            .onClick {
              it.listener()
              reset(componentContext.androidContext, dialog)
            })
      }
    }

    component.child(BottomSheetBar.create(componentContext)
        .primaryActionRes(if (sExternalFolderSync) R.string.import_export_layout_folder_sync_disable else R.string.import_export_layout_folder_sync_enable)
        .isActionNegative(sExternalFolderSync)
        .onPrimaryClick {
          sExternalFolderSync = !sExternalFolderSync
          reset(componentContext.androidContext, dialog)
        }
        .paddingDip(YogaEdge.HORIZONTAL, 20f)
        .paddingDip(YogaEdge.VERTICAL, 8f))
    return component.build()
  }

  fun getOptions(componentContext: ComponentContext): List<LithoOptionsItem> {
    val activity = componentContext.androidContext as MainActivity
    val options = ArrayList<LithoOptionsItem>()
    options.add(LithoOptionsItem(
        title = R.string.import_export_locked,
        subtitle = R.string.import_export_locked_details,
        icon = R.drawable.ic_action_lock,
        listener = { sFolderSyncBackupLocked = !sFolderSyncBackupLocked },
        isSelectable = true,
        selected = sFolderSyncBackupLocked
    ))
    return options
  }

}