package com.maubis.scarlet.base.note.creation.sheet

import android.app.Dialog
import android.widget.Toast
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

var curQuestionId = 1
var showAnswer = false

class QuizBottomSheet : LithoBottomSheet() {
  var questions = ArrayList<String>()
  var answers = ArrayList<String>()
  var isLoaded = false

  fun notifyQuestions(questions: ArrayList<String>, answers: ArrayList<String>) {
      this.questions = questions
      this.answers = answers
      this.isLoaded = true
  }

    fun showAnswer() {
        showAnswer = true
        reset(context!!, dialog)
    }

    fun hideAnswer() {
        showAnswer = false
        reset(context!!, dialog)
    }

  override fun getComponent(componentContext: ComponentContext, dialog: Dialog): Component {

      val component = Column.create(componentContext)
              .widthPercent(100f);
      if (!isLoaded)
          component.child(BottomSheetBar.create(componentContext)
                  .secondaryActionRes(R.string.quiz_loading)
                  .onPrimaryClick {
                      hideAnswer()
                  }.paddingDip(YogaEdge.VERTICAL, 8f))
      if (questions.size > 0){
              component.child(CounterChooser.create(componentContext)
                      .value(curQuestionId)
                      .minValue(1)
                      .maxValue(questions.size)
                      .onValueChange { value ->
                          curQuestionId = value
                          showAnswer = false
                          reset(context!!, dialog)
                      }
                      .paddingDip(YogaEdge.VERTICAL, 16f))
        component.paddingDip(YogaEdge.VERTICAL, 8f)
        .paddingDip(YogaEdge.HORIZONTAL, 20f)
        .child(getLithoBottomSheetTitle(componentContext)
            .textRes(R.string.generate_quiz)
            .marginDip(YogaEdge.HORIZONTAL, 0f))
        .child(Text.create(componentContext)
                .marginDip(YogaEdge.BOTTOM, 16f)
                .textSizeRes(R.dimen.font_size_normal)
                .text(this.questions.get(curQuestionId - 1))
                .textColor(CoreConfig.instance.themeController().get(ThemeColorType.TERTIARY_TEXT)))
        if (showAnswer)
            component.child(Text.create(componentContext)
                .marginDip(YogaEdge.BOTTOM, 16f)
                .textSizeRes(R.dimen.font_size_large)
                .text(this.answers.get(curQuestionId - 1))
                .textColor(CoreConfig.instance.themeController().get(ThemeColorType.PRIMARY_TEXT)))

        if (!showAnswer)
            component.child(BottomSheetBar.create(componentContext)
                .primaryActionRes(R.string.quiz_show_answer)
                .onPrimaryClick {
                    showAnswer()
                }.paddingDip(YogaEdge.VERTICAL, 8f))
          else
            component.child(BottomSheetBar.create(componentContext)
                    .primaryActionRes(R.string.quiz_hide_answer)
                    .onPrimaryClick {
                        hideAnswer()
                    }
                    .paddingDip(YogaEdge.VERTICAL, 8f))
        } else {
          Toast.makeText(context, R.string.quiz_no_questions, Toast.LENGTH_SHORT).show()
          dismiss()
      }
    return component.build()
  }
}