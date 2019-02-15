package com.example.batu.ninoclient.settings.sheet

import android.app.Dialog
import android.view.View
import com.example.batu.ninoclient.MainActivity
import com.example.batu.ninoclient.R
import com.example.batu.ninoclient.config.CoreConfig
import com.example.batu.ninoclient.support.option.OptionsItem
import com.example.batu.ninoclient.support.sheets.OptionItemBottomSheetBase

class NoteSettingsOptionsBottomSheet : OptionItemBottomSheetBase() {

  override fun setupViewWithDialog(dialog: Dialog) {
    setOptions(dialog, getOptions())
  }

  private fun getOptions(): List<OptionsItem> {
    val activity = context as MainActivity
    val options = ArrayList<OptionsItem>()
    options.add(OptionsItem(
        title = R.string.note_option_default_color,
        subtitle = R.string.note_option_default_color_subtitle,
        icon = R.drawable.ic_action_color,
        listener = View.OnClickListener {
          ColorPickerBottomSheet.openSheet(
              activity,
              object : ColorPickerBottomSheet.ColorPickerDefaultController {
                override fun getSheetTitle(): Int = R.string.choose_note_color

                override fun getColorList(): IntArray = activity.resources.getIntArray(R.array.bright_colors)

                override fun onColorSelected(color: Int) {
                  CoreConfig.instance.store().put(KEY_NOTE_DEFAULT_COLOR, color)
                }

                override fun getSelectedColor(): Int {
                  return genDefaultColor()
                }
              }
          )
          dismiss()
        }
    ))
    options.add(OptionsItem(
        title = R.string.home_option_security,
        subtitle = R.string.home_option_security_subtitle,
        icon = R.drawable.ic_option_security,
        listener = View.OnClickListener {
          SecurityOptionsBottomSheet.openSheet(activity)
          dismiss()
        }
    ))
    return options
  }

  override fun getLayout(): Int = R.layout.bottom_sheet_options

  companion object {

    const val KEY_NOTE_DEFAULT_COLOR = "KEY_NOTE_DEFAULT_COLOR"

    fun openSheet(activity: MainActivity) {
      val sheet = NoteSettingsOptionsBottomSheet()
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }

    fun genDefaultColor(): Int {
      return CoreConfig.instance.store().get(KEY_NOTE_DEFAULT_COLOR, (0xFFD32F2F).toInt())
    }
  }
}