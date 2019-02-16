package com.nino.ninoclient.settings.sheet

import android.app.Dialog
import android.view.View
import com.github.bijoysingh.starter.util.IntentUtils
import com.nino.ninoclient.MainActivity
import com.nino.ninoclient.R
import com.nino.ninoclient.config.CoreConfig
import com.nino.ninoclient.export.sheet.BackupSettingsOptionsBottomSheet
import com.nino.ninoclient.main.recycler.getMigrateToProAppInformationItem
import com.nino.ninoclient.note.creation.sheet.EditorOptionsBottomSheet
import com.nino.ninoclient.support.option.OptionsItem
import com.nino.ninoclient.support.sheets.OptionItemBottomSheetBase
import com.nino.ninoclient.support.sheets.openSheet
import com.nino.ninoclient.support.utils.Flavor
import com.nino.ninoclient.support.utils.FlavourUtils
import com.nino.ninoclient.support.utils.FlavourUtils.PRO_APP_PACKAGE_NAME
import com.nino.ninoclient.support.utils.FlavourUtils.hasProAppInstalled
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class SettingsOptionsBottomSheet : OptionItemBottomSheetBase() {

  override fun setupViewWithDialog(dialog: Dialog) {
    GlobalScope.launch(Dispatchers.Main) {
      val options = GlobalScope.async(Dispatchers.IO) { getOptions() }
      setOptions(dialog, options.await())
    }
  }

  private fun getOptions(): List<OptionsItem> {
    val activity = context as MainActivity
    val options = ArrayList<OptionsItem>()

    val loginClick = CoreConfig.instance.authenticator().openLoginActivity(activity)
    val firebaseUser = CoreConfig.instance.authenticator().userId()

    val migrateToPro = getMigrateToProAppInformationItem(activity)
    options.add(OptionsItem(
        title = migrateToPro.title,
        subtitle = migrateToPro.source,
        icon = migrateToPro.icon,
        listener = View.OnClickListener {
          migrateToPro.function()
          dismiss()
        },
        visible = CoreConfig.instance.appFlavor() == Flavor.LITE && FlavourUtils.hasProAppInstalled(activity),
        selected = true
    ))
    options.add(OptionsItem(
        title = R.string.home_option_login_with_app,
        subtitle = R.string.home_option_login_with_app_subtitle,
        icon = R.drawable.ic_sign_in_options,
        listener = View.OnClickListener {
          loginClick?.run()
          dismiss()
        },
        visible = loginClick !== null && firebaseUser === null
    ))
    options.add(OptionsItem(
        title = R.string.home_option_ui_experience,
        subtitle = R.string.home_option_ui_experience_subtitle,
        icon = R.drawable.ic_action_grid,
        listener = View.OnClickListener {
          UISettingsOptionsBottomSheet.openSheet(activity)
        }
    ))
    options.add(OptionsItem(
        title = R.string.home_option_note_settings,
        subtitle = R.string.home_option_note_settings_subtitle,
        icon = R.drawable.ic_subject_white_48dp,
        listener = View.OnClickListener {
          NoteSettingsOptionsBottomSheet.openSheet(activity)
        }
    ))
    options.add(OptionsItem(
        title = R.string.home_option_editor_options_title,
        subtitle = R.string.home_option_editor_options_description,
        icon = R.drawable.ic_edit_white_48dp,
        listener = View.OnClickListener {
          openSheet(activity, EditorOptionsBottomSheet())
        }
    ))
    options.add(OptionsItem(
        title = R.string.home_option_backup_options,
        subtitle = R.string.home_option_backup_options_subtitle,
        icon = R.drawable.ic_export,
        listener = View.OnClickListener {
          BackupSettingsOptionsBottomSheet.openSheet(activity)
        }
    ))
    options.add(OptionsItem(
        title = R.string.home_option_about,
        subtitle = R.string.home_option_about_subtitle,
        icon = R.drawable.ic_info,
        listener = View.OnClickListener {
          AboutSettingsOptionsBottomSheet.openSheet(activity)
        }
    ))
    options.add(OptionsItem(
        title = R.string.home_option_install_pro_app,
        subtitle = R.string.home_option_install_pro_app_details,
        icon = R.drawable.ic_favorite_white_48dp,
        listener = View.OnClickListener {
          IntentUtils.openAppPlayStore(context, PRO_APP_PACKAGE_NAME)
          dismiss()
        },
        visible = CoreConfig.instance.appFlavor() == Flavor.LITE && !hasProAppInstalled(activity)
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
        title = R.string.home_option_delete_notes_and_more,
        subtitle = R.string.home_option_delete_notes_and_more_details,
        icon = R.drawable.ic_delete_permanently,
        listener = View.OnClickListener {
          DeleteAndMoreOptionsBottomSheet.openSheet(activity)
        }
    ))
    options.add(OptionsItem(
        title = R.string.home_option_logout_of_app,
        subtitle = R.string.home_option_logout_of_app_subtitle,
        icon = R.drawable.ic_sign_in_options,
        listener = View.OnClickListener {
          CoreConfig.instance.authenticator().logout()
          dismiss()
        },
        visible = firebaseUser !== null
    ))
    return options
  }

  override fun getLayout(): Int = R.layout.bottom_sheet_options

  companion object {

    const val KEY_MARKDOWN_ENABLED = "KEY_MARKDOWN_ENABLED"
    const val KEY_MARKDOWN_HOME_ENABLED = "KEY_MARKDOWN_HOME_ENABLED"

    fun openSheet(activity: MainActivity) {
      val sheet = SettingsOptionsBottomSheet()

      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}