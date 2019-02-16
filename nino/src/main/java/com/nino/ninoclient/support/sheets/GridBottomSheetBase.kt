package com.nino.ninoclient.support.sheets

import android.app.Dialog
import android.view.View
import android.widget.GridLayout
import android.widget.TextView
import com.github.bijoysingh.uibasics.views.UILabelView
import com.nino.ninoclient.R
import com.nino.ninoclient.config.CoreConfig
import com.nino.ninoclient.support.option.OptionsItem
import com.nino.ninoclient.support.ui.ThemeColorType
import com.nino.ninoclient.support.ui.ThemedBottomSheetFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

abstract class GridBottomSheetBase : ThemedBottomSheetFragment() {

  override fun setupView(dialog: Dialog?) {
    super.setupView(dialog)
    if (dialog == null) {
      return
    }
    setupViewWithDialog(dialog)
    makeBackgroundTransparent(dialog, R.id.root_layout)
  }

  abstract fun setupViewWithDialog(dialog: Dialog)

  override fun getBackgroundView(): Int {
    return R.id.container_layout
  }

  override fun getBackgroundCardViewIds(): Array<Int> = arrayOf(R.id.grid_card)

  fun setOptionTitle(dialog: Dialog, title: Int) {
    GlobalScope.launch(Dispatchers.Main) {
      val titleView = dialog.findViewById<TextView>(R.id.options_title)
      titleView.setTextColor(CoreConfig.instance.themeController().get(ThemeColorType.SECONDARY_TEXT))
      titleView.setText(title)
    }
  }

  fun setOptions(dialog: Dialog, options: List<OptionsItem>) {
    val layoutGrid = dialog.findViewById<GridLayout>(R.id.grid_layout);
    setOptions(layoutGrid, options)
  }

  fun setOptions(layoutGrid: GridLayout, options: List<OptionsItem>) {
    layoutGrid.columnCount = if (resources.getBoolean(R.bool.is_tablet)) 4 else 3
    for (option in options) {
      if (!option.visible) {
        continue
      }

      val contentView = View.inflate(context, R.layout.layout_grid_item, null) as UILabelView
      contentView.setText(option.title)
      contentView.setImageResource(option.icon)
      contentView.setTextColor(getOptionsTitleColor(option.selected))
      contentView.setImageTint(getOptionsTitleColor(option.selected))

      if (!option.invalid) {
        contentView.setOnClickListener(option.listener)
      } else {
        contentView.alpha = 0.4f
      }
      layoutGrid.addView(contentView)
    }
  }

  override fun getLayout(): Int = R.layout.bottom_sheet_grid_layout
}