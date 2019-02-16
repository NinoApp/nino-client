package com.nino.ninoclient.note.actions

import android.app.Dialog
import android.os.Build
import android.speech.tts.TextToSpeech
import android.widget.ImageView
import android.widget.TextView
import com.nino.ninoclient.R
import com.nino.ninoclient.config.CoreConfig
import com.nino.ninoclient.database.room.note.Note
import com.nino.ninoclient.note.getUnreliablyStrippedText
import com.nino.ninoclient.support.ui.ThemeColorType
import com.nino.ninoclient.support.ui.ThemedActivity
import com.nino.ninoclient.support.ui.ThemedBottomSheetFragment

class TextToSpeechBottomSheet : ThemedBottomSheetFragment() {

  var note: Note? = null
  var textToSpeech: TextToSpeech? = null

  override fun setupView(dialog: Dialog?) {
    super.setupView(dialog)
    if (dialog == null || note == null) {
      return
    }

    val nonNullNote = note!!
    val title = dialog.findViewById<TextView>(R.id.options_title)
    title.setTextColor(CoreConfig.instance.themeController().get(ThemeColorType.SECONDARY_TEXT))

    val speakPlayPause = dialog.findViewById<ImageView>(R.id.speak_play_pause)
    speakPlayPause.setColorFilter(CoreConfig.instance.themeController().get(ThemeColorType.TOOLBAR_ICON))
    speakPlayPause.setOnClickListener {
      val tts = textToSpeech
      if (tts === null) {
        return@setOnClickListener
      }

      when {
        tts.isSpeaking -> {
          tts.stop()
          speakPlayPause.setImageResource(R.drawable.ic_action_play_sound)
        }
        else -> {
          speak(nonNullNote)
          speakPlayPause.setImageResource(R.drawable.ic_action_stop)
        }
      }
    }

    textToSpeech = TextToSpeech(themedContext(), TextToSpeech.OnInitListener {
      speak(nonNullNote)
      speakPlayPause.setImageResource(R.drawable.ic_action_stop)
    })
    makeBackgroundTransparent(dialog, R.id.root_layout)
  }

  fun speak(note: Note) {
    if (Build.VERSION.SDK_INT >= 21) {
      textToSpeech?.speak(note.getUnreliablyStrippedText(themedContext()), TextToSpeech.QUEUE_FLUSH, null, "NOTE")
    } else {
      textToSpeech?.speak(note.getUnreliablyStrippedText(themedContext()), TextToSpeech.QUEUE_FLUSH, null)
    }
  }

  override fun getBackgroundView(): Int = R.id.container_layout

  override fun getBackgroundCardViewIds(): Array<Int> = arrayOf(R.id.speak_note_card)

  override fun getLayout(): Int = R.layout.bottom_sheet_speak_note

  override fun onPause() {
    super.onPause()
    textToSpeech?.stop()
  }

  override fun onDestroy() {
    super.onDestroy()
    textToSpeech?.shutdown()
  }

  companion object {
    fun openSheet(activity: ThemedActivity, note: Note) {
      val sheet = TextToSpeechBottomSheet()
      sheet.note = note
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}