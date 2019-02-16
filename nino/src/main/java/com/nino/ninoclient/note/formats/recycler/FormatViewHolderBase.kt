package com.nino.ninoclient.note.formats.recycler

import android.content.Context
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.View
import com.github.bijoysingh.starter.recyclerview.RecyclerViewHolder
import com.nino.ninoclient.R
import com.nino.ninoclient.config.CoreConfig
import com.nino.ninoclient.core.format.Format
import com.nino.ninoclient.core.format.FormatType
import com.nino.ninoclient.note.creation.activity.INTENT_KEY_NOTE_ID
import com.nino.ninoclient.note.creation.activity.ViewAdvancedNoteActivity
import com.nino.ninoclient.settings.sheet.NoteSettingsOptionsBottomSheet.Companion.genDefaultColor
import com.nino.ninoclient.settings.sheet.SettingsOptionsBottomSheet
import com.nino.ninoclient.settings.sheet.TextSizeBottomSheet
import com.nino.ninoclient.settings.sheet.UISettingsOptionsBottomSheet.Companion.useNoteColorAsBackground
import com.nino.ninoclient.support.ui.ColorUtil
import com.nino.ninoclient.support.ui.Theme
import com.nino.ninoclient.support.ui.ThemeColorType

const val KEY_EDITABLE = "KEY_EDITABLE"
const val KEY_NOTE_COLOR = "KEY_NOTE_COLOR"

data class FormatViewHolderConfig(
    val editable: Boolean,
    val isMarkdownEnabled: Boolean,
    val fontSize: Float,
    val backgroundColor: Int,
    val secondaryTextColor: Int,
    val tertiaryTextColor: Int,
    val iconColor: Int,
    val hintTextColor: Int,
    val accentColor: Int,
    val noteUUID: String)


abstract class FormatViewHolderBase(context: Context, view: View) : RecyclerViewHolder<Format>(context, view) {

  protected val activity: ViewAdvancedNoteActivity = context as ViewAdvancedNoteActivity

  override fun populate(data: Format, extra: Bundle?) {
    val noteColor: Int = extra?.getInt(KEY_NOTE_COLOR) ?: genDefaultColor()
    val secondaryTextColor: Int
    val tertiaryTextColor: Int
    val iconColor: Int
    val hintTextColor: Int
    val theme = CoreConfig.instance.themeController()
    val isLightBackground = ColorUtil.isLightColored(noteColor)
    when {
      !useNoteColorAsBackground -> {
        secondaryTextColor = theme.get(ThemeColorType.SECONDARY_TEXT)
        tertiaryTextColor = theme.get(ThemeColorType.TERTIARY_TEXT)
        iconColor = theme.get(ThemeColorType.TOOLBAR_ICON)
        hintTextColor = theme.get(ThemeColorType.HINT_TEXT)
      }
      isLightBackground -> {
        secondaryTextColor = theme.get(context, Theme.LIGHT, ThemeColorType.SECONDARY_TEXT)
        tertiaryTextColor = theme.get(context, Theme.LIGHT, ThemeColorType.TERTIARY_TEXT)
        iconColor = theme.get(context, Theme.LIGHT, ThemeColorType.TOOLBAR_ICON)
        hintTextColor = theme.get(context, Theme.LIGHT, ThemeColorType.HINT_TEXT)
      }
      else -> {
        secondaryTextColor = theme.get(context, Theme.DARK, ThemeColorType.SECONDARY_TEXT)
        tertiaryTextColor = theme.get(context, Theme.DARK, ThemeColorType.TERTIARY_TEXT)
        iconColor = theme.get(context, Theme.DARK, ThemeColorType.TOOLBAR_ICON)
        hintTextColor = theme.get(context, Theme.DARK, ThemeColorType.HINT_TEXT)
      }
    }
    val
        config = FormatViewHolderConfig(
        editable = !(extra != null
            && extra.containsKey(KEY_EDITABLE)
            && !extra.getBoolean(KEY_EDITABLE)),
        isMarkdownEnabled = (extra == null
            || extra.getBoolean(SettingsOptionsBottomSheet.KEY_MARKDOWN_ENABLED, true)
            || data.forcedMarkdown),
        fontSize = {
          val fontSize = extra?.getInt(TextSizeBottomSheet.KEY_TEXT_SIZE, TextSizeBottomSheet.TEXT_SIZE_DEFAULT)
              ?: TextSizeBottomSheet.TEXT_SIZE_DEFAULT
          when (data.formatType) {
            FormatType.HEADING -> fontSize.toFloat() + 4
            FormatType.SUB_HEADING -> fontSize.toFloat() + 2
            else -> fontSize.toFloat()
          }
        }(),
        backgroundColor = when (data.formatType) {
          FormatType.CODE, FormatType.IMAGE -> CoreConfig.instance.themeController().get(context, R.color.code_light, R.color.code_dark)
          else -> ContextCompat.getColor(context, R.color.transparent)
        },
        secondaryTextColor = secondaryTextColor,
        tertiaryTextColor = tertiaryTextColor,
        iconColor = iconColor,
        hintTextColor = hintTextColor,
        accentColor = theme.get(ThemeColorType.ACCENT_TEXT),
        noteUUID = extra?.getString(INTENT_KEY_NOTE_ID) ?: "default")

    populate(data, config)

  }

  abstract fun populate(data: Format, config: FormatViewHolderConfig)
}
