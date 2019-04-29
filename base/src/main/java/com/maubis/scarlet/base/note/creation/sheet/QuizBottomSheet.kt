package com.maubis.scarlet.base.note.creation.sheet

import android.app.Dialog
import com.facebook.litho.Column
import com.facebook.litho.Component
import com.facebook.litho.ComponentContext
import com.facebook.litho.widget.Text
import com.facebook.yoga.YogaEdge
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.support.sheets.LithoBottomSheet
import com.maubis.scarlet.base.support.sheets.getLithoBottomSheetTitle
import com.maubis.scarlet.base.support.specs.BottomSheetBar
import com.maubis.scarlet.base.support.specs.CounterChooser
import com.maubis.scarlet.base.support.ui.ThemeColorType

const val STORE_KEY_TEXT_SIZE = "KEY_TEXT_SIZE"
const val TEXT_SIZE_DEFAULT = 16
const val TEXT_SIZE_MIN = 12
const val TEXT_SIZE_MAX = 24

var sEditorTextSize: Int
  get() = CoreConfig.instance.store().get(STORE_KEY_TEXT_SIZE, TEXT_SIZE_DEFAULT)
  set(value) = CoreConfig.instance.store().put(STORE_KEY_TEXT_SIZE, value)

class QuizBottomSheet : LithoBottomSheet() {
  var questions = ArrayList<String>()
  var answers = ArrayList<String>()

  fun notifyQuestions(questions: ArrayList<String>, answers: ArrayList<String>) {
      this.questions = questions
      this.answers = answers
  }

  override fun getComponent(componentContext: ComponentContext, dialog: Dialog): Component {

      val component = Column.create(componentContext)
              .widthPercent(100f);
      if (questions.size > 0){
              component.child(CounterChooser.create(componentContext)
                      .value(sEditorTextSize)
                      .minValue(1)
                      .maxValue(questions.size)
                      .onValueChange { value ->
                          sEditorTextSize = value
                          reset(context!!, dialog)
                      }
                      .paddingDip(YogaEdge.VERTICAL, 16f))
        }
        component.paddingDip(YogaEdge.VERTICAL, 8f)
        .paddingDip(YogaEdge.HORIZONTAL, 20f)
        .child(getLithoBottomSheetTitle(componentContext)
            .textRes(R.string.note_option_font_size)
            .marginDip(YogaEdge.HORIZONTAL, 0f))
        .child(Text.create(componentContext)
            .textSizeDip(sEditorTextSize.toFloat())
            .marginDip(YogaEdge.BOTTOM, 16f)
            .textRes(R.string.note_option_font_size_example)
            .textColor(CoreConfig.instance.themeController().get(ThemeColorType.TERTIARY_TEXT)))
        .child(BottomSheetBar.create(componentContext)
            .primaryActionRes(R.string.import_export_layout_exporting_done)
            .onPrimaryClick {
              dismiss()
            }.paddingDip(YogaEdge.VERTICAL, 8f))
    return component.build()
  }
}