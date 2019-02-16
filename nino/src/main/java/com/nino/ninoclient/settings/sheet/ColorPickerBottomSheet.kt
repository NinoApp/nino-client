package com.nino.ninoclient.settings.sheet

import android.app.Dialog
import android.view.View
import android.widget.TextView
import com.google.android.flexbox.FlexboxLayout
import com.nino.ninoclient.R
import com.nino.ninoclient.settings.view.ColorView
import com.nino.ninoclient.support.ui.ThemedActivity
import com.nino.ninoclient.support.ui.ThemedBottomSheetFragment

class ColorPickerBottomSheet : ThemedBottomSheetFragment() {

  var colorChosen: Int = 0
  var defaultController: ColorPickerDefaultController? = null

  override fun setupView(dialog: Dialog?) {
    super.setupView(dialog)
    if (dialog == null) {
      return
    }

    val controller = defaultController
    if (controller === null) {
      dismiss()
      return
    }

    val colorPicker = dialog.findViewById<FlexboxLayout>(R.id.flexbox_layout)
    setColorsList(colorPicker, controller)

    val accentColorCard = dialog.findViewById<View>(R.id.accent_color_card)
    accentColorCard.visibility = View.GONE
    val colorPickerAccent = dialog.findViewById<FlexboxLayout>(R.id.flexbox_layout_accent)
    colorPickerAccent.visibility = View.GONE

    val optionsTitle = dialog.findViewById<TextView>(R.id.options_title)
    optionsTitle.setText(controller.getSheetTitle())
    optionsTitle.setOnClickListener {
      dismiss()
    }

    makeBackgroundTransparent(dialog, R.id.root_layout)
  }

  override fun getBackgroundCardViewIds() = arrayOf(
      R.id.accent_color_card,
      R.id.core_color_card)

  override fun getBackgroundView(): Int {
    return R.id.container_layout
  }

  private fun setColorsList(colorSelectorLayout: FlexboxLayout,
                            controller: ColorPickerDefaultController) {
    colorSelectorLayout.removeAllViews()
    colorChosen = when {
      colorChosen != 0 -> colorChosen
      else -> controller.getSelectedColor()
    }

    for (color in controller.getColorList()) {
      val item = ColorView(themedContext())
      item.setColor(color, colorChosen == color)
      item.setOnClickListener {
        colorChosen = color
        controller.onColorSelected(colorChosen)
        setColorsList(colorSelectorLayout, controller)
      }
      colorSelectorLayout.addView(item)
    }
  }

  override fun getLayout(): Int = R.layout.bottom_sheet_flexbox_layout

  interface ColorPickerDefaultController {
    fun getSheetTitle(): Int

    fun getColorList(): IntArray

    fun onColorSelected(color: Int)

    fun getSelectedColor(): Int
  }

  companion object {
    fun openSheet(activity: ThemedActivity,
                  picker: ColorPickerDefaultController) {
      val sheet = ColorPickerBottomSheet()
      sheet.defaultController = picker
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}