package com.maubis.scarlet.base.settings.sheet

import android.app.Dialog
import com.facebook.litho.ComponentContext
import com.github.bijoysingh.starter.util.IntentUtils
import com.maubis.scarlet.base.MainActivity
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.export.sheet.BackupSettingsOptionsBottomSheet
import com.maubis.scarlet.base.main.recycler.getMigrateToProAppInformationItem
import com.maubis.scarlet.base.note.creation.sheet.EditorOptionsBottomSheet
import com.maubis.scarlet.base.support.sheets.LithoOptionBottomSheet
import com.maubis.scarlet.base.support.sheets.LithoOptionsItem
import com.maubis.scarlet.base.support.sheets.openSheet
import com.maubis.scarlet.base.support.utils.Flavor
import com.maubis.scarlet.base.support.utils.FlavourUtils
import com.maubis.scarlet.base.support.utils.FlavourUtils.PRO_APP_PACKAGE_NAME
import com.maubis.scarlet.base.support.utils.FlavourUtils.hasProAppInstalled
import com.maubis.scarlet.base.widget.sheet.WidgetOptionsBottomSheet

class SettingsOptionsBottomSheet : LithoOptionBottomSheet() {
  override fun title(): Int = R.string.home_option_sheet_title

  override fun getOptions(componentContext: ComponentContext, dialog: Dialog): List<LithoOptionsItem> {
    val activity = context as MainActivity
    val options = ArrayList<LithoOptionsItem>()

    val loginClick = CoreConfig.instance.authenticator().openLoginActivity(activity)
    val firebaseUser = CoreConfig.instance.authenticator().userId()

    val migrateToPro = getMigrateToProAppInformationItem(activity)

    options.add(LithoOptionsItem(
        title = R.string.home_option_login_with_app,
        subtitle = R.string.home_option_login_with_app_subtitle,
        icon = R.drawable.ic_sign_in_options,
        listener = {
          loginClick?.run()
          dismiss()
        },
        visible = loginClick !== null && firebaseUser === null
    ))
    options.add(LithoOptionsItem(
        title = R.string.home_option_ui_experience,
        subtitle = R.string.home_option_ui_experience_subtitle,
        icon = R.drawable.ic_action_grid,
        listener = {
          UISettingsOptionsBottomSheet.openSheet(activity)
        }
    ))
      options.add(LithoOptionsItem(
              title = R.string.home_option_security,
              subtitle = R.string.home_option_security_subtitle,
              icon = R.drawable.ic_option_security,
              listener = {
                  SecurityOptionsBottomSheet.openSheet(activity)
                  dismiss()
              }
      ))

      /*
    options.add(LithoOptionsItem(
        title = R.string.home_option_editor_options_title,
        subtitle = R.string.home_option_editor_options_description,
        icon = R.drawable.ic_edit_white_48dp,
        listener = {
          openSheet(activity, EditorOptionsBottomSheet())
        }
    ))

    options.add(LithoOptionsItem(
        title = R.string.home_option_backup_options,
        subtitle = R.string.home_option_backup_options_subtitle,
        icon = R.drawable.ic_export,
        listener = {
          openSheet(activity, BackupSettingsOptionsBottomSheet())
        }
    ))
    options.add(LithoOptionsItem(
        title = R.string.home_option_widget_options_title,
        subtitle = R.string.home_option_widget_options_description,
        icon = R.drawable.icon_widget,
        listener = {
          openSheet(activity, WidgetOptionsBottomSheet())
        }
    ))

    options.add(LithoOptionsItem(
        title = R.string.home_option_about,
        subtitle = R.string.home_option_about_subtitle,
        icon = R.drawable.ic_info,
        listener = {
          openSheet(activity, AboutUsBottomSheet())
        }
    ))*/
    options.add(LithoOptionsItem(
        title = R.string.home_option_delete_notes_and_more,
        subtitle = R.string.home_option_delete_notes_and_more_details,
        icon = R.drawable.ic_delete_permanently,
        listener = {
          openSheet(activity, DeleteAndMoreOptionsBottomSheet())
        }
    ))
    options.add(LithoOptionsItem(
        title = R.string.home_option_logout_of_app,
        subtitle = R.string.home_option_logout_of_app_subtitle,
        icon = R.drawable.ic_sign_in_options,
        listener = {
          CoreConfig.instance.authenticator().logout()
          dismiss()
        },
        visible = firebaseUser !== null
    ))
    return options
  }

  companion object {

    const val KEY_MARKDOWN_ENABLED = "KEY_MARKDOWN_ENABLED"
    const val KEY_MARKDOWN_HOME_ENABLED = "KEY_MARKDOWN_HOME_ENABLED"

    fun openSheet(activity: MainActivity) {
      val sheet = SettingsOptionsBottomSheet()

      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}