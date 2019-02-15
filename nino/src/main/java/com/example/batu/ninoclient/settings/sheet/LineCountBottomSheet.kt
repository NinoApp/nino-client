package com.example.batu.ninoclient.settings.sheet

import android.app.Dialog
import com.example.batu.ninoclient.MainActivity
import com.example.batu.ninoclient.R
import com.example.batu.ninoclient.config.CoreConfig
import com.example.batu.ninoclient.support.sheets.CounterBottomSheetBase
import com.example.batu.ninoclient.support.ui.ThemedActivity


class LineCountBottomSheet : CounterBottomSheetBase() {
  override fun getMinCountLimit(): Int = LINE_COUNT_MIN

  override fun getMaxCountLimit(): Int = LINE_COUNT_MAX

  override fun getDefaultCount(): Int = getDefaultLineCount()


  override fun onCountChange(dialog: Dialog, activity: ThemedActivity, count: Int) {
    CoreConfig.instance.store().put(KEY_LINE_COUNT, count)
    (activity as MainActivity).notifyAdapterExtraChanged()
  }

  override fun getBackgroundCardViewIds(): Array<Int> = arrayOf(R.id.counter_card)

  companion object {
    const val KEY_LINE_COUNT = "KEY_LINE_COUNT"
    const val LINE_COUNT_DEFAULT = 7
    const val LINE_COUNT_MIN = 2
    const val LINE_COUNT_MAX = 15

    fun openSheet(activity: MainActivity) {
      val sheet = LineCountBottomSheet()

      sheet.show(activity.supportFragmentManager, sheet.tag)
    }

    fun getDefaultLineCount(): Int = CoreConfig.instance.store().get(KEY_LINE_COUNT, LINE_COUNT_DEFAULT)
  }
}