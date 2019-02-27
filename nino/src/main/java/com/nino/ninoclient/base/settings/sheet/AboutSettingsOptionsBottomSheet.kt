package com.nino.ninoclient.base.settings.sheet

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.view.View
import com.github.bijoysingh.starter.util.IntentUtils
import com.nino.ninoclient.MainActivity
import com.nino.ninoclient.R
import com.nino.ninoclient.base.config.CoreConfig
import com.nino.ninoclient.base.main.sheets.WhatsNewItemsBottomSheet
import com.nino.ninoclient.base.support.utils.Flavor
import com.nino.ninoclient.base.support.option.OptionsItem
import com.nino.ninoclient.base.support.sheets.OptionItemBottomSheetBase

class AboutSettingsOptionsBottomSheet : OptionItemBottomSheetBase() {

  override fun setupViewWithDialog(dialog: Dialog) {
    setOptions(dialog, getOptions())
  }

  private fun getOptions(): List<OptionsItem> {
    val activity = context as MainActivity
    val options = ArrayList<OptionsItem>()
    options.add(OptionsItem(
        title = R.string.home_option_about_page,
        subtitle = R.string.home_option_about_page_subtitle,
        icon = R.drawable.ic_info,
        listener = View.OnClickListener {
          AboutUsBottomSheet.openSheet(activity)
          dismiss()
        }
    ))
    options.add(OptionsItem(
        title = R.string.home_option_open_source_page,
        subtitle = R.string.home_option_open_source_page_subtitle,
        icon = R.drawable.ic_code_white_48dp,
        listener = View.OnClickListener {
          OpenSourceBottomSheet.openSheet(activity)
          dismiss()
        }
    ))
    options.add(OptionsItem(
        title = R.string.material_notes_privacy_policy,
        subtitle = R.string.material_notes_privacy_policy_subtitle,
        icon = R.drawable.ic_privacy_policy,
        listener = View.OnClickListener {
          activity.startActivity(Intent(
              Intent.ACTION_VIEW,
              Uri.parse(PRIVACY_POLICY_LINK)))
          dismiss()
        },
        visible = CoreConfig.instance.appFlavor() != Flavor.NONE

    ))
    options.add(OptionsItem(
        title = R.string.home_option_rate_and_review,
        subtitle = R.string.home_option_rate_and_review_subtitle,
        icon = R.drawable.ic_rating,
        listener = View.OnClickListener {
          IntentUtils.openAppPlayStore(activity)
          dismiss()
        }
    ))
    options.add(OptionsItem(
        title = R.string.whats_new_title,
        subtitle = R.string.whats_new_subtitle,
        icon = R.drawable.ic_whats_new,
        listener = View.OnClickListener {
          WhatsNewItemsBottomSheet.openSheet(activity)
          dismiss()
        }
    ))
    options.add(OptionsItem(
        title = R.string.home_option_fill_survey,
        subtitle = R.string.home_option_fill_survey_subtitle,
        icon = R.drawable.ic_note_white_48dp,
        listener = View.OnClickListener {
          activity.startActivity(Intent(
              Intent.ACTION_VIEW,
              Uri.parse(SURVEY_LINK)))
          dismiss()
        }
    ))
    return options
  }

  override fun getLayout(): Int = R.layout.bottom_sheet_options

  companion object {

    const val SURVEY_LINK = "https://goo.gl/forms/UbE2lARpp89CNIbl2"
    const val PRIVACY_POLICY_LINK = "https://www.iubenda.com/privacy-policy/8213521"

    fun openSheet(activity: MainActivity) {
      val sheet = AboutSettingsOptionsBottomSheet()

      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}