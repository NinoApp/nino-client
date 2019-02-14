package com.maubis.scarlet.base.settings.sheet

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.widget.TextView
import com.github.bijoysingh.starter.async.MultiAsyncTask
import com.maubis.markdown.Markdown
import com.maubis.scarlet.base.MainActivity
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.support.ui.ThemeColorType
import com.maubis.scarlet.base.support.ui.ThemedBottomSheetFragment


class OpenSourceBottomSheet : ThemedBottomSheetFragment() {
  override fun getBackgroundView(): Int {
    return R.id.container_layout
  }

  override fun setupView(dialog: Dialog?) {
    super.setupView(dialog)
    if (dialog == null) {
      return
    }

    val openSource = dialog.findViewById<TextView>(R.id.open_source)
    val library = dialog.findViewById<TextView>(R.id.library_list)
    val contribute = dialog.findViewById<TextView>(R.id.contribute)

    val activity = themedActivity()
    MultiAsyncTask.execute(object : MultiAsyncTask.Task<String> {
      override fun run(): String {
        try {
          val pInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0)
          return pInfo.versionName
        } catch (e: Exception) {

        }
        return ""
      }

      override fun handle(result: String) {
        val appName = getString(R.string.app_name)
        val creatorName = getString(R.string.maubis_apps)

        val openSourceDetails = getString(R.string.about_page_description_os, appName, creatorName)
        openSource.text = openSourceDetails

        library.text = Markdown.render(LIBRARY_DETAILS_MD)

        contribute.setOnClickListener {
          themedContext().startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_URL)))
          dismiss()
        }
      }
    })

    val textColor = CoreConfig.instance.themeController().get(ThemeColorType.TERTIARY_TEXT)
    openSource.setTextColor(textColor)
    library.setTextColor(textColor)

    val aboutAppTitle = dialog.findViewById<TextView>(R.id.about_app_title)
    val libraryTitle = dialog.findViewById<TextView>(R.id.library_title)

    val titleTextColor = CoreConfig.instance.themeController().get(ThemeColorType.SECTION_HEADER)
    aboutAppTitle.setTextColor(titleTextColor)
    libraryTitle.setTextColor(titleTextColor)

    makeBackgroundTransparent(dialog, R.id.root_layout)
  }

  override fun getLayout(): Int = R.layout.bottom_sheet_open_source

  override fun getBackgroundCardViewIds(): Array<Int> = arrayOf(
      R.id.about_app_card, R.id.library_card)

  companion object {

    val GITHUB_URL = "https://github.com/BijoySingh/Material-Notes-Android-App"
    val LIBRARY_DETAILS_MD = "#### Android Support Libraries\n" +
        "- `'com.android.support.appcompat-v7'`\n" +
        "- `'com.android.support.recyclerview-v7'`\n" +
        "- `'com.android.support.cardview-v7'`\n" +
        "- `'com.android.support.support-v4'`\n" +
        "- `'com.android.support.design'`\n" +
        "- `'com.android.support.constraint'`\n" +
        "#### Android Architecture Room Library\n" +
        "- `'android.arch.persistence.room'`\n" +
        "#### Internal Support Libraries\n" +
        "- `'com.github.bijoysingh.android-basics'`\n" +
        "- `'com.github.bijoysingh.ui-basics'`\n" +
        "- `'com.github.bijoysingh.floating-bubble'`\n" +
        "#### Kotlin Support\n" +
        "- `'org.jetbrains.kotlin'`\n" +
        "#### Reprint: Fingerprint Library\n" +
        "- `'com.github.ajalt.reprint'`\n" +
        "#### Tutorial Tap Target Prompt\n" +
        "- `'uk.co.samuelwall:material-tap-target-prompt'`\n" +
        "#### Markwon: Markdown Library\n" +
        "- `'ru.noties:markwon'`\n" +
        "#### Google Firebase Support Library\n" +
        "- `'com.google.firebase:firebase-auth'`\n" +
        "- `'com.google.firebase:firebase-database'`\n" +
        "#### Google Play Services Library\n" +
        "- `'com.google.android.gms:play-services-auth'`\n" +
        "#### Shortcuts Gradle Plugin\n" +
        "- `'com.github.zellius:android-shortcut-gradle-plugin'`\n" +
        "#### Google Flexbox Library\n" +
        "- `'com.google.android:flexbox'`\n"

    fun openSheet(activity: MainActivity) {
      val sheet = OpenSourceBottomSheet()
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}