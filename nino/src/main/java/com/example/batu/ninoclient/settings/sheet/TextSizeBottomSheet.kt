package com.example.batu.ninoclient.settings.sheet

import android.app.Dialog
import android.support.v7.widget.CardView
import android.util.TypedValue
import android.widget.TextView
import com.example.batu.ninoclient.MainActivity
import com.example.batu.ninoclient.R
import com.example.batu.ninoclient.config.CoreConfig
import com.example.batu.ninoclient.support.sheets.CounterBottomSheetBase
import com.example.batu.ninoclient.support.ui.ThemeColorType
import com.example.batu.ninoclient.support.ui.ThemedActivity

class TextSizeBottomSheet : CounterBottomSheetBase() {
  override fun getMinCountLimit(): Int = TEXT_SIZE_MIN

  override fun getMaxCountLimit(): Int = TEXT_SIZE_MAX

  override fun getDefaultCount(): Int = getDefaultTextSize()

  override fun onCountChange(
      dialog: Dialog,
      activity: ThemedActivity,
      count: Int) {
    CoreConfig.instance.store().put(KEY_TEXT_SIZE, count)
    updateExample(dialog.findViewById<TextView>(R.id.options_example), count)
  }

  fun updateExample(example: TextView, fontSize: Int) {
    example.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize.toFloat())
  }

  override fun setupFurther(dialog: Dialog) {
    val exampleCard = dialog.findViewById<CardView>(R.id.font_size_card)
    exampleCard.setCardBackgroundColor(CoreConfig.instance.themeController().get(themedContext(), R.color.material_grey_200, R.color.material_grey_700))
    val example = dialog.findViewById<TextView>(R.id.options_example)
    updateExample(example, getDefaultTextSize())
    example.setTextColor(CoreConfig.instance.themeController().get(ThemeColorType.SECONDARY_TEXT))
  }

  override fun getLayout(): Int = R.layout.bottom_sheet_text_size

  override fun getBackgroundCardViewIds(): Array<Int> = arrayOf(R.id.counter_card, R.id.font_size_card)

  companion object {
    const val KEY_TEXT_SIZE = "KEY_TEXT_SIZE"
    const val TEXT_SIZE_DEFAULT = 16
    const val TEXT_SIZE_MIN = 12
    const val TEXT_SIZE_MAX = 24

    fun openSheet(activity: MainActivity) {
      val sheet = TextSizeBottomSheet()
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }

    fun getDefaultTextSize(): Int = CoreConfig.instance.store().get(KEY_TEXT_SIZE, TEXT_SIZE_DEFAULT)
  }
}