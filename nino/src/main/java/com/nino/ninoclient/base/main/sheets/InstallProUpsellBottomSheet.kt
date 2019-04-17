package com.nino.ninoclient.base.main.sheets

import android.app.Dialog
import android.view.View
import android.widget.TextView
import com.github.bijoysingh.starter.util.IntentUtils
import com.nino.ninoclient.R
import com.nino.ninoclient.base.config.CoreConfig
import com.nino.ninoclient.base.support.ui.ThemeColorType
import com.nino.ninoclient.base.support.ui.ThemedActivity
import com.nino.ninoclient.base.support.ui.ThemedBottomSheetFragment


class InstallProUpsellBottomSheet : ThemedBottomSheetFragment() {
  override fun getBackgroundView(): Int {
    return R.id.container_layout
  }

  override fun setupView(dialog: Dialog?) {
    super.setupView(dialog)
    if (dialog == null) {
      return
    }

    val optionsTitle = dialog.findViewById<TextView>(R.id.options_title)
    optionsTitle.setTextColor(CoreConfig.instance.themeController().get(ThemeColorType.SECONDARY_TEXT))

    val whyInstallPro = dialog.findViewById<TextView>(R.id.why_install_pro)
    val whyInstallProDetails = dialog.findViewById<TextView>(R.id.why_install_pro_details)
    val installPro = dialog.findViewById<View>(R.id.install_pro_app)

    val textColor = CoreConfig.instance.themeController().get(ThemeColorType.TERTIARY_TEXT)
    whyInstallProDetails.setTextColor(textColor)

    val titleTextColor = CoreConfig.instance.themeController().get(ThemeColorType.SECTION_HEADER)
    whyInstallPro.setTextColor(titleTextColor)

    installPro.setOnClickListener {
      IntentUtils.openAppPlayStore(context, "com.bijoysingh.quicknote.pro")
      dismiss()
    }

    makeBackgroundTransparent(dialog, R.id.root_layout)
  }

  override fun getBackgroundCardViewIds() = arrayOf(R.id.content_card)

  override fun getLayout(): Int = R.layout.bottom_sheet_install_pro_upsell

  companion object {
    fun openSheet(activity: ThemedActivity) {
      val sheet = InstallProUpsellBottomSheet()
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}