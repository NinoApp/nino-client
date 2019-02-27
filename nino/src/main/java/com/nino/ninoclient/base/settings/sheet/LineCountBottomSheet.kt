package com.nino.ninoclient.base.settings.sheet

import android.app.Dialog
import com.nino.ninoclient.MainActivity
import com.nino.ninoclient.R
import com.nino.ninoclient.base.config.CoreConfig
import com.nino.ninoclient.base.support.sheets.CounterBottomSheetBase
import com.nino.ninoclient.base.support.ui.ThemedActivity


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