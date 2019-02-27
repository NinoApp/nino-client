package com.nino.ninoclient.base.main.sheets

import android.app.Dialog
import android.view.View
import com.nino.ninoclient.R
import com.nino.ninoclient.base.support.option.OptionsItem
import com.nino.ninoclient.base.support.option.SimpleOptionsItem
import com.nino.ninoclient.base.support.sheets.ChooseOptionBottomSheetBase
import com.nino.ninoclient.base.support.ui.ThemedActivity

class GenericOptionsBottomSheet : ChooseOptionBottomSheetBase() {

  var title: String = ""
  var options: List<SimpleOptionsItem> = emptyList()

  override fun setupViewWithDialog(dialog: Dialog) {
    setOptions(dialog, getOptionItems())
  }

  private fun getOptionItems(): List<OptionsItem> {
    val getIcon = fun(isSelected: Boolean): Int = if (isSelected) R.drawable.ic_done_white_48dp else 0
    return options.map { option ->
      OptionsItem(
          title = option.title,
          subtitle = option.title,
          icon = getIcon(option.selected),
          listener = View.OnClickListener {
            option.listener()
            dismiss()
          },
          selected = option.selected)
    }
  }

  companion object {
    fun openSheet(activity: ThemedActivity, title: String, options: List<SimpleOptionsItem>) {
      val sheet = GenericOptionsBottomSheet()
      sheet.title = title
      sheet.options = options
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}