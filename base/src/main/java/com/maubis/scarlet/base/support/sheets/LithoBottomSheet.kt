package com.maubis.scarlet.base.support.sheets

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.support.design.widget.BottomSheetDialogFragment
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ScrollView
import com.facebook.litho.Column
import com.facebook.litho.Component
import com.facebook.litho.ComponentContext
import com.facebook.litho.LithoView
import com.facebook.litho.widget.Image
import com.facebook.litho.widget.Text
import com.facebook.litho.widget.VerticalScroll
import com.facebook.yoga.YogaAlign
import com.facebook.yoga.YogaEdge
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.config.CoreConfig.Companion.FONT_MONSERRAT
import com.maubis.scarlet.base.support.ui.ThemeColorType

fun openSheet(activity: AppCompatActivity, sheet: LithoBottomSheet) {
  sheet.show(activity.supportFragmentManager, sheet.tag)
}

fun getLithoBottomSheetTitle(context: ComponentContext): Text.Builder {
  return Text.create(context)
      .textSizeRes(R.dimen.font_size_xxxlarge)
      .typeface(CoreConfig.FONT_MONSERRAT)
      .marginDip(YogaEdge.HORIZONTAL, 20f)
      .marginDip(YogaEdge.TOP, 18f)
      .marginDip(YogaEdge.BOTTOM, 8f)
      .textStyle(Typeface.BOLD)
      .textColor(CoreConfig.instance.themeController().get(ThemeColorType.PRIMARY_TEXT))
}

fun getLithoBottomSheetButton(context: ComponentContext): Text.Builder {
  return Text.create(context)
      .typeface(FONT_MONSERRAT)
      .textSizeRes(R.dimen.font_size_large)
      .paddingDip(YogaEdge.VERTICAL, 12f)
      .paddingDip(YogaEdge.HORIZONTAL, 24f)
      .textColorRes(R.color.light_secondary_text)
      .backgroundRes(R.drawable.accent_rounded_bg)
}

abstract class LithoBottomSheet : BottomSheetDialogFragment() {
  override fun setupDialog(dialog: Dialog, style: Int) {
    val localContext = context
    if (localContext === null) {
      dismiss()
      return
    }

    reset(localContext, dialog)
  }

  fun reset(localContext: Context, dialog: Dialog) {
    val componentContext = ComponentContext(localContext)
    getFullComponent(componentContext, dialog, getComponent(componentContext, dialog))
  }

  fun getFullComponent(componentContext: ComponentContext, dialog: Dialog, childComponent: Component) {
    val topHandle = when (CoreConfig.instance.themeController().isNightTheme()) {
      true -> R.drawable.bottom_sheet_top_handle_dark
      false -> R.drawable.bottom_sheet_top_handle_light
    }

    val baseComponent = Column.create(componentContext)
        .paddingDip(YogaEdge.VERTICAL, 16f)
        .widthPercent(100f)
        .alignItems(YogaAlign.CENTER)
        .backgroundColor(CoreConfig.instance.themeController().get(ThemeColorType.BACKGROUND))
        .child(
            Image.create(componentContext)
                .drawableRes(topHandle)
                .widthDip(72f)
                .heightDip(6f)
                .alpha(0.8f)
                .marginDip(YogaEdge.BOTTOM, 8f)
                .build()
        )
        .child(VerticalScroll.create(componentContext)
            .nestedScrollingEnabled(true)
            .childComponent(childComponent))
        .build()

    val contentView = LithoView.create(componentContext.androidContext, baseComponent)
    dialog.setContentView(contentView)

    val parentView = contentView.parent
    if (parentView is View) {
      parentView.setBackgroundColor(Color.TRANSPARENT)
    }
  }

  abstract fun getComponent(componentContext: ComponentContext, dialog: Dialog): Component
}