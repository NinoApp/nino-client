package com.nino.ninoclient.base.settings.sheet

import android.app.Dialog
import android.view.View
import com.nino.ninoclient.MainActivity
import com.nino.ninoclient.R
import com.nino.ninoclient.base.config.CoreConfig
import com.nino.ninoclient.base.main.sheets.InstallProUpsellBottomSheet
import com.nino.ninoclient.base.settings.sheet.LineCountBottomSheet.Companion.getDefaultLineCount
import com.nino.ninoclient.base.settings.sheet.SortingOptionsBottomSheet.Companion.getSortingState
import com.nino.ninoclient.base.settings.sheet.SortingOptionsBottomSheet.Companion.getSortingTechniqueLabel
import com.nino.ninoclient.base.settings.sheet.TextSizeBottomSheet.Companion.getDefaultTextSize
import com.nino.ninoclient.base.support.utils.Flavor

import com.nino.ninoclient.base.support.option.OptionsItem
import com.nino.ninoclient.base.support.sheets.OptionItemBottomSheetBase
import com.nino.ninoclient.base.support.ui.KEY_APP_THEME
import com.nino.ninoclient.base.support.ui.Theme
import com.nino.ninoclient.base.support.ui.ThemeColorType
import com.nino.ninoclient.base.support.ui.ThemeManager

class UISettingsOptionsBottomSheet : OptionItemBottomSheetBase() {

  override fun setupViewWithDialog(dialog: Dialog) {
    setOptions(dialog, getOptions())
  }

  private fun getOptions(): List<OptionsItem> {
    val activity = context as MainActivity
    val options = ArrayList<OptionsItem>()
    val flavor = CoreConfig.instance.appFlavor()
    options.add(OptionsItem(
        title = R.string.home_option_enable_night_mode,
        subtitle = R.string.home_option_enable_night_mode_subtitle,
        icon = R.drawable.night_mode_white_48dp,
        listener = View.OnClickListener {
          CoreConfig.instance.store().put(KEY_APP_THEME, Theme.DARK.name)
          CoreConfig.instance.themeController().notifyChange(activity)
          activity.notifyThemeChange()
          dismiss()
        },
        visible = !CoreConfig.instance.themeController().isNightTheme() && flavor != Flavor.PRO
    ))
    options.add(OptionsItem(
        title = R.string.home_option_enable_day_mode,
        subtitle = R.string.home_option_enable_day_mode_subtitle,
        icon = R.drawable.ic_action_day_mode,
        listener = View.OnClickListener {
          CoreConfig.instance.store().put(KEY_APP_THEME, Theme.LIGHT.name)
          CoreConfig.instance.themeController().notifyChange(activity)
          activity.notifyThemeChange()
          dismiss()
        },
        visible = CoreConfig.instance.themeController().isNightTheme() && flavor != Flavor.PRO
    ))
    options.add(OptionsItem(
        title = R.string.home_option_theme_color,
        subtitle = R.string.home_option_theme_color_subtitle,
        icon = if (CoreConfig.instance.themeController().isNightTheme()) R.drawable.night_mode_white_48dp else R.drawable.ic_action_day_mode,
        listener = View.OnClickListener {
          if (flavor == Flavor.PRO) {
            ColorPickerBottomSheet.openSheet(activity, object : ColorPickerBottomSheet.ColorPickerDefaultController {
              override fun getSheetTitle(): Int = R.string.theme_page_title

              override fun getColorList(): IntArray = resources.getIntArray(R.array.theme_color)

              override fun onColorSelected(color: Int) {
                val theme = ThemeManager.getThemeByBackgroundColor(activity, color)
                CoreConfig.instance.store().put(KEY_APP_THEME, theme.name)
                CoreConfig.instance.themeController().notifyChange(activity)
                activity.notifyThemeChange()
                reset(dialog)
                resetBackground(dialog)
                makeBackgroundTransparent(dialog, R.id.root_layout)
              }

              override fun getSelectedColor(): Int = CoreConfig.instance.themeController().get(ThemeColorType.BACKGROUND)
            })
          } else {
            InstallProUpsellBottomSheet.openSheet(activity)
            dismiss()
          }
        },
        visible = flavor != Flavor.NONE,
        actionIcon = if (flavor == Flavor.PRO) 0 else R.drawable.ic_rating
    ))
    val isTablet = resources.getBoolean(R.bool.is_tablet)
    options.add(OptionsItem(
        title = R.string.home_option_enable_list_view,
        subtitle = R.string.home_option_enable_list_view_subtitle,
        icon = R.drawable.ic_action_list,
        listener = View.OnClickListener {
          useGridView = false
          activity.notifyAdapterExtraChanged()
          dismiss()
        },
        visible = !isTablet && useGridView
    ))
    options.add(OptionsItem(
        title = R.string.home_option_enable_grid_view,
        subtitle = R.string.home_option_enable_grid_view_subtitle,
        icon = R.drawable.ic_action_grid,
        listener = View.OnClickListener {
          useGridView = true
          activity.notifyAdapterExtraChanged()
          dismiss()
        },
        visible = !isTablet && !useGridView
    ))
    options.add(OptionsItem(
        title = R.string.home_option_order_notes,
        subtitle = getSortingTechniqueLabel(getSortingState()),
        icon = R.drawable.ic_sort,
        listener = View.OnClickListener {
          SortingOptionsBottomSheet.openSheet(activity, { activity.setupData() })
          dismiss()
        }
    ))
    options.add(OptionsItem(
        title = R.string.note_option_font_size,
        subtitle = 0,
        content = activity.getString(R.string.note_option_font_size_subtitle, getDefaultTextSize()),
        icon = R.drawable.ic_title_white_48dp,
        listener = View.OnClickListener {
          if (flavor == Flavor.PRO) {
            TextSizeBottomSheet.openSheet(activity)
          } else {
            InstallProUpsellBottomSheet.openSheet(activity)
          }
          dismiss()
        },
        visible = flavor != Flavor.NONE,
        actionIcon = if (flavor == Flavor.PRO) 0 else R.drawable.ic_rating
    ))
    options.add(OptionsItem(
        title = R.string.note_option_number_lines,
        subtitle = 0,
        content = activity.getString(R.string.note_option_number_lines_subtitle, getDefaultLineCount()),
        icon = R.drawable.ic_action_list,
        listener = View.OnClickListener {
          LineCountBottomSheet.openSheet(activity)
          dismiss()
        }
    ))
    options.add(OptionsItem(
        title = R.string.ui_options_note_background_color,
        subtitle = when (useNoteColorAsBackground) {
          true -> R.string.ui_options_note_background_color_settings_note
          false -> R.string.ui_options_note_background_color_settings_theme
        },
        icon = R.drawable.ic_action_color,
        listener = View.OnClickListener {
          if (flavor != Flavor.PRO) {
            InstallProUpsellBottomSheet.openSheet(activity)
            return@OnClickListener
          }

          useNoteColorAsBackground = !useNoteColorAsBackground
          reset(dialog)
        },
        visible = flavor != Flavor.NONE,
        actionIcon = if (flavor == Flavor.PRO) 0 else R.drawable.ic_rating
    ))
      /*
    options.add(OptionsItem(
        title = R.string.markdown_sheet_home_markdown_support,
        subtitle = R.string.markdown_sheet_home_markdown_support_subtitle,
        icon = R.drawable.ic_markdown_logo,
        listener = View.OnClickListener {
          sMarkdownEnabledHome = !sMarkdownEnabledHome
          dismiss()
        },
        actionIcon = if (sMarkdownEnabledHome) 0 else R.drawable.ic_check_box_white_24dp
    ))
      */
    return options
  }

  companion object {

    const val KEY_LIST_VIEW = "KEY_LIST_VIEW"
    const val KEY_NOTE_VIEWER_BG_COLOR = "KEY_NOTE_VIEWER_BG_COLOR"
    const val KEY_MARKDOWN_HOME_ENABLED = "KEY_MARKDOWN_HOME_ENABLED"

    fun openSheet(activity: MainActivity) {
      val sheet = UISettingsOptionsBottomSheet()
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }

    var useGridView: Boolean
      get() = CoreConfig.instance.store().get(KEY_LIST_VIEW, true)
      set(isGrid) = CoreConfig.instance.store().put(KEY_LIST_VIEW, isGrid)

    var useNoteColorAsBackground: Boolean
      get() = CoreConfig.instance.store().get(KEY_NOTE_VIEWER_BG_COLOR, false)
      set(value) = CoreConfig.instance.store().put(KEY_NOTE_VIEWER_BG_COLOR, value)

    var sMarkdownEnabledHome: Boolean
      get() = CoreConfig.instance.store().get(KEY_MARKDOWN_HOME_ENABLED, true)
      set(value) = CoreConfig.instance.store().put(KEY_MARKDOWN_HOME_ENABLED, value)
  }
}