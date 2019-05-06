package com.maubis.scarlet.base.note.creation.specs

import android.graphics.Color
import com.facebook.litho.*
import com.facebook.litho.annotations.*
import com.facebook.litho.widget.EmptyComponent
import com.facebook.litho.widget.HorizontalScroll
import com.facebook.yoga.YogaAlign
import com.facebook.yoga.YogaEdge
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.core.format.FormatType
import com.maubis.scarlet.base.core.format.MarkdownType
import com.maubis.scarlet.base.note.copy
import com.maubis.scarlet.base.note.creation.activity.CreateNoteActivity
import com.maubis.scarlet.base.note.creation.activity.ViewAdvancedNoteActivity
import com.maubis.scarlet.base.note.creation.sheet.MarkdownHelpBottomSheet
import com.maubis.scarlet.base.note.creation.sheet.sEditorMarkdownDefault
import com.maubis.scarlet.base.note.share
import com.maubis.scarlet.base.settings.sheet.UISettingsOptionsBottomSheet
import com.maubis.scarlet.base.support.sheets.openSheet
import com.maubis.scarlet.base.support.specs.EmptySpec
import com.maubis.scarlet.base.support.specs.ToolbarColorConfig
import com.maubis.scarlet.base.support.specs.bottomBarCard
import com.maubis.scarlet.base.support.specs.bottomBarRoundIcon
import android.widget.Toast
import android.support.v4.app.ActivityCompat.startActivityForResult
import android.speech.RecognizerIntent
import android.content.Intent
import android.os.Handler
import android.support.annotation.ColorInt
import com.maubis.scarlet.base.core.note.getFormats
import com.maubis.scarlet.base.iink.IInkActivity
import java.util.*


enum class NoteCreateBottomBarType {
  DEFAULT_SEGMENTS,
  DEFAULT_MARKDOWNS,
  ALL_SEGMENTS,
  ALL_MARKDOWNS,
  NINO_SPECIAL,
  OPTIONS
}

@LayoutSpec
object NoteCreationBottomBarSpec {

  @OnCreateInitialState
  fun onCreateInitialState(
      context: ComponentContext,
      state: StateValue<NoteCreateBottomBarType>) {
    state.set(NoteCreateBottomBarType.NINO_SPECIAL)
  }

  @OnCreateLayout
  fun onCreate(context: ComponentContext,
               @Prop colorConfig: ToolbarColorConfig,
               @State state: NoteCreateBottomBarType): Component {

      val activity = context.androidContext as CreateNoteActivity

      val row = Row.create(context)
        .widthPercent(100f)
        .paddingDip(YogaEdge.HORIZONTAL, 4f)
        .alignItems(YogaAlign.CENTER)


    /*
    if (state != NoteCreateBottomBarType.MAIN) {
      row.child(bottomBarRoundIcon(context, colorConfig)
              .iconRes(R.drawable.ic_chevron_left)
              .onClick { }
              .isClickDisabled(true)
              .clickHandler(NoteCreationBottomBar.onStateChangeClick(context, NoteCreateBottomBarType.MAIN)))
          //.child(EmptySpec.create(context).heightDip(1f).flexGrow(1f))
    }
    */

    val content = when (state) {
      NoteCreateBottomBarType.OPTIONS ->
        NoteCreationOptionsBottomBar.create(context)
                .colorConfig(colorConfig)
                .flexGrow(1f)

      NoteCreateBottomBarType.DEFAULT_SEGMENTS ->
        NoteCreationSegmentsBottomBar.create(context)
            .colorConfig(colorConfig)
            .flexGrow(1f)
            .toggleButtonClick(NoteCreationBottomBar.onStateChangeClick(context, NoteCreateBottomBarType.ALL_SEGMENTS))
      NoteCreateBottomBarType.DEFAULT_MARKDOWNS -> NoteCreationMarkdownsBottomBar.create(context)
          .colorConfig(colorConfig)
          .flexGrow(1f)
          .toggleButtonClick(NoteCreationBottomBar.onStateChangeClick(context, NoteCreateBottomBarType.ALL_MARKDOWNS))
      NoteCreateBottomBarType.ALL_SEGMENTS -> HorizontalScroll.create(context)
          .flexGrow(1f)
          .contentProps(NoteCreationAllSegmentsBottomBar.create(context).colorConfig(colorConfig))
      NoteCreateBottomBarType.NINO_SPECIAL->
        NoteCreationNinoSpecialBottomBar.create(context)
                .colorConfig(colorConfig)
                .flexGrow(1f)
                .toggleButtonClick(NoteCreationBottomBar.onStateChangeClick(context, NoteCreateBottomBarType.ALL_SEGMENTS))
      NoteCreateBottomBarType.ALL_MARKDOWNS -> HorizontalScroll.create(context)
          .flexGrow(1f)
          .contentProps(NoteCreationAllMarkdownsBottomBar.create(context).colorConfig(colorConfig))
    }
    row.child(content)
            .child(EmptySpec.create(context).heightDip(1f).flexGrow(1f))

    row.child(NoteCreationMainBottomBar.create(context)
            .colorConfig(colorConfig)
            //.flexGrow(1f)
            .state(state)
            .optionsClick(NoteCreationBottomBar.onStateChangeClick(context, NoteCreateBottomBarType.OPTIONS))
            .ninoSpecialClick(NoteCreationBottomBar.onStateChangeClick(context, NoteCreateBottomBarType.NINO_SPECIAL))
            .segmentsClick(NoteCreationBottomBar.onStateChangeClick(context, NoteCreateBottomBarType.DEFAULT_SEGMENTS))
            .markdownsClick(NoteCreationBottomBar.onStateChangeClick(context, NoteCreateBottomBarType.DEFAULT_MARKDOWNS))
    )
       .child(bottomBarRoundIcon(context, colorConfig)
                    .iconRes(R.drawable.ic_done_white_48dp)
                    .iconColor(Color.GREEN)
                    .onClick {
                      activity.onSave()
                    })

    return bottomBarCard(context, row.build(), colorConfig).build()
  }

  @OnEvent(ClickEvent::class)
  fun onStateChangeClick(context: ComponentContext,
                         @State state: NoteCreateBottomBarType,
                         @Param nextState: NoteCreateBottomBarType) {
    NoteCreationBottomBar.onStateChange(context, nextState)
  }

  @OnUpdateState
  fun onStateChange(state: StateValue<NoteCreateBottomBarType>, @Param nextState: NoteCreateBottomBarType) {
    state.set(nextState)
  }
}

@LayoutSpec
object NoteCreationMainBottomBarSpec {
  @OnCreateLayout
  fun onCreate(context: ComponentContext,
               @Prop colorConfig: ToolbarColorConfig,
               @Prop optionsClick: EventHandler<ClickEvent>,
               @Prop ninoSpecialClick: EventHandler<ClickEvent>,
               @Prop segmentsClick: EventHandler<ClickEvent>,
               @Prop markdownsClick: EventHandler<ClickEvent>,
               @Prop state: NoteCreateBottomBarType): Component {
    val activity = context.androidContext as CreateNoteActivity
    return Row.create(context)
            .alignItems(YogaAlign.CENTER)
/*            .child(bottomBarRoundIcon(context, colorConfig)
                    .iconRes(R.drawable.ic_more_options)
                    .bgColor(Color.TRANSPARENT)
                    .iconColor(activity.note().color)
                    .onClick { }
                    .isClickDisabled(true)
                    .clickHandler(optionsClick))
            .child(EmptySpec.create(context).heightDip(1f).flexGrow(1f))*/
            .child(bottomBarRoundIcon(context, colorConfig)
                    .bgColor( if (state.equals(NoteCreateBottomBarType.NINO_SPECIAL)) Color.GREEN else Color.TRANSPARENT)
                    .iconRes(R.drawable.ic_whats_new)
                    .onClick { }
                    .isClickDisabled(true)
                    .clickHandler(ninoSpecialClick))
            .child(bottomBarRoundIcon(context, colorConfig)
                    .bgColor( if (state.equals(NoteCreateBottomBarType.DEFAULT_SEGMENTS) || state.equals(NoteCreateBottomBarType.ALL_SEGMENTS) ) Color.GREEN else Color.TRANSPARENT)
                    .iconRes(R.drawable.ic_action_grid)
                    .onClick { }
                    .isClickDisabled(true)
                    .clickHandler(segmentsClick))
            .child(bottomBarRoundIcon(context, colorConfig)
                    .bgColor( if (state.equals(NoteCreateBottomBarType.DEFAULT_MARKDOWNS) || state.equals(NoteCreateBottomBarType.ALL_MARKDOWNS) ) Color.GREEN else Color.TRANSPARENT)
                    .iconRes(R.drawable.icon_realtime_markdown)
                    .onClick { }
                    .isClickDisabled(true)
                    .clickHandler(markdownsClick))
            .child(bottomBarRoundIcon(context, colorConfig)
                    .iconRes(R.drawable.ic_more_options)
                    .bgColor(if (state.equals(NoteCreateBottomBarType.OPTIONS)) Color.GREEN else Color.TRANSPARENT)
                    .onClick { }
                    .isClickDisabled(true)
                    .clickHandler(optionsClick))
            .build()
  }
}

@LayoutSpec
object NoteCreationOptionsBottomBarSpec {
  @OnCreateLayout
  fun onCreate(context: ComponentContext,
               @Prop colorConfig: ToolbarColorConfig): Component {
    val activity = context.androidContext as CreateNoteActivity
    return Row.create(context)
            .alignItems(YogaAlign.CENTER)
            .child(bottomBarRoundIcon(context, colorConfig)
                    .iconRes(R.drawable.ic_undo_history)
                    .onClick { activity.onHistoryClick(true) })
            .child(bottomBarRoundIcon(context, colorConfig)
                    .bgColor(activity.note().color)
                    .bgAlpha(255)
                    .iconRes(R.drawable.ic_empty)
                    .onClick { activity.onColorChangeClick() }
                    .showBorder(true)
                    .iconMarginHorizontalRes(R.dimen.toolbar_round_small_icon_margin_horizontal)
                    .iconSizeRes(R.dimen.toolbar_round_small_icon_size))
            .child(bottomBarRoundIcon(context, colorConfig)
                    .iconRes(R.drawable.ic_redo_history)
                    .onClick { activity.onHistoryClick(false) })
            .build()
  }
}



@LayoutSpec
object NoteCreationNinoSpecialBottomBarSpec {
  fun getSpeechInput(activity: CreateNoteActivity) {

    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())

    if (intent.resolveActivity(activity.packageManager) != null) {

      startActivityForResult(activity, intent, 10, null)

    } else {
      Toast.makeText(activity, "Your Device Don't Support Speech Input", Toast.LENGTH_SHORT).show()
    }
  }

  @OnCreateLayout
  fun onCreate(context: ComponentContext,
               @Prop colorConfig: ToolbarColorConfig,
               @Prop toggleButtonClick: EventHandler<ClickEvent>): Component {
    val activity = context.androidContext as CreateNoteActivity
    activity.packageManager
    return Row.create(context)
            .alignItems(YogaAlign.CENTER)
            .child(bottomBarRoundIcon(context, colorConfig)
                    .iconRes(R.drawable.imgly_sticker_emoticons_star)
                    .onClick {
                      // SMART NOTE
                    })
            .child(bottomBarRoundIcon(context, colorConfig)
                    .iconRes(R.drawable.voice_recognition)
                    .iconColor(Color.WHITE)
                    .onClick {
                      getSpeechInput(activity)
                    })
            .child(bottomBarRoundIcon(context, colorConfig)
                    .iconRes(R.drawable.icon_realtime_markdown)
                    .onClick {
                      // drawing to text block

                      val handler = Handler()
                      handler.postDelayed(Runnable {
                        openIinkActivity(activity, "Text")
                      }, 100)
                    })
            .child(bottomBarRoundIcon(context, colorConfig)
                    .iconRes(R.drawable.ic_formats_logo)
                    .onClick {
                      // drawing to math equation in image format
                      activity.addEmptyItem(FormatType.IMAGE)
                      val handler = Handler()
                      handler.postDelayed(Runnable {
                        openIinkActivity(activity, "Math")
                      }, 100)
                    })
            .child(bottomBarRoundIcon(context, colorConfig)
                    .iconRes(R.drawable.ic_action_grid)
                    .onClick {
                      // drawing to diagrams in image format
                      activity.addEmptyItem(FormatType.IMAGE)
                      val handler = Handler()
                      handler.postDelayed(Runnable {
                      openIinkActivity(activity, "Diagram")
                    }, 100)
                    })
            .child(bottomBarRoundIcon(context, colorConfig)
                    .iconRes(R.drawable.ic_border_color_white_24dp)
                    .onClick {
                      // direct drawing in image format
                      activity.addEmptyItem(FormatType.IMAGE)
                      val handler = Handler()
                      handler.postDelayed(Runnable {
                      openIinkActivity(activity, "Drawing")
                      }, 100)
                    })
            .build()
  }

  fun openIinkActivity(activity: CreateNoteActivity, type:String) {

    val intent = Intent(activity, IInkActivity::class.java)
    intent.putExtra("iink_type", type)

    var requestCode = -1

    when(type) {
      "Text" -> requestCode = 801
      "Math" -> requestCode = 802
      "Diagram" -> requestCode = 803
      "Drawing" -> requestCode = 804
    }

    startActivityForResult(activity, intent, requestCode, null)

  }

}

@LayoutSpec
object NoteCreationSegmentsBottomBarSpec {
  @OnCreateLayout
  fun onCreate(context: ComponentContext,
               @Prop colorConfig: ToolbarColorConfig,
               @Prop toggleButtonClick: EventHandler<ClickEvent>): Component {
    val activity = context.androidContext as CreateNoteActivity
    return Row.create(context)
            .alignItems(YogaAlign.CENTER)
            .child(bottomBarRoundIcon(context, colorConfig)
                    .iconRes(R.drawable.ic_title_white_48dp)
                    .onClick { activity.addEmptyItemAtFocused(FormatType.HEADING) })
            .child(bottomBarRoundIcon(context, colorConfig)
                    .iconRes(R.drawable.ic_subject_white_48dp)
                    .onClick { activity.addEmptyItemAtFocused(FormatType.TEXT) })
            .child(bottomBarRoundIcon(context, colorConfig)
                    .iconRes(R.drawable.ic_check_box_white_24dp)
                    .onClick { activity.addEmptyItemAtFocused(FormatType.CHECKLIST_UNCHECKED) })
            .child(bottomBarRoundIcon(context, colorConfig)
                    .iconRes(R.drawable.ic_image_gallery)
                    .onClick { activity.addEmptyItemAtFocused(FormatType.IMAGE) })
            .child(bottomBarRoundIcon(context, colorConfig)
                    .iconRes(R.drawable.ic_more_horiz_white_48dp)
                    .onClick { }
                    .isClickDisabled(true)
                    .clickHandler(toggleButtonClick))
            .build()
  }
}

@LayoutSpec
object NoteCreationAllSegmentsBottomBarSpec {
  @OnCreateLayout
  fun onCreate(context: ComponentContext,
               @Prop colorConfig: ToolbarColorConfig): Component {
    val activity = context.androidContext as CreateNoteActivity
    return Row.create(context)
        .alignSelf(YogaAlign.CENTER)
        .alignItems(YogaAlign.CENTER)
        .child(bottomBarRoundIcon(context, colorConfig)
            .iconRes(R.drawable.ic_title_white_48dp)
            .onClick { activity.addEmptyItemAtFocused(FormatType.HEADING) })
        .child(bottomBarRoundIcon(context, colorConfig)
            .iconRes(R.drawable.ic_subject_white_48dp)
            .onClick { activity.addEmptyItemAtFocused(FormatType.TEXT) })
        .child(bottomBarRoundIcon(context, colorConfig)
            .iconRes(R.drawable.ic_check_box_white_24dp)
            .onClick { activity.addEmptyItemAtFocused(FormatType.CHECKLIST_UNCHECKED) })
        .child(bottomBarRoundIcon(context, colorConfig)
                .iconRes(R.drawable.ic_image_gallery)
                .onClick { activity.addEmptyItemAtFocused(FormatType.IMAGE) })
        .child(bottomBarRoundIcon(context, colorConfig)
                .iconRes(R.drawable.ic_title_white_48dp)
                .iconPaddingRes(R.dimen.toolbar_round_icon_padding_subsize)
                .onClick { activity.addEmptyItemAtFocused(FormatType.SUB_HEADING) })
        .child(bottomBarRoundIcon(context, colorConfig)
                .iconRes(R.drawable.ic_format_separator)
                .onClick { activity.addEmptyItemAtFocused(FormatType.SEPARATOR) })
        .child(bottomBarRoundIcon(context, colorConfig)
            .iconRes(R.drawable.ic_code_white_48dp)
            .onClick { activity.addEmptyItemAtFocused(FormatType.CODE) })
        .child(bottomBarRoundIcon(context, colorConfig)
                .iconRes(R.drawable.ic_format_quote_white_48dp)
                .onClick { activity.addEmptyItemAtFocused(FormatType.QUOTE) })
        .build()
  }
}


@LayoutSpec
object NoteCreationMarkdownsBottomBarSpec {
  @OnCreateLayout
  fun onCreate(context: ComponentContext,
               @Prop colorConfig: ToolbarColorConfig,
               @Prop toggleButtonClick: EventHandler<ClickEvent>): Component {
    val activity = context.androidContext as CreateNoteActivity
    return Row.create(context)
            .alignItems(YogaAlign.CENTER)
            .child(bottomBarRoundIcon(context, colorConfig)
                    .iconRes(R.drawable.ic_title_white_48dp)
                    .onClick { activity.triggerMarkdown(MarkdownType.HEADER) })
            .child(bottomBarRoundIcon(context, colorConfig)
                    .iconRes(R.drawable.ic_markdown_bold)
                    .onClick { activity.triggerMarkdown(MarkdownType.BOLD) })
            .child(bottomBarRoundIcon(context, colorConfig)
                    .iconRes(R.drawable.ic_markdown_italics)
                    .onClick { activity.triggerMarkdown(MarkdownType.ITALICS) })
            .child(bottomBarRoundIcon(context, colorConfig)
                    .iconRes(R.drawable.ic_format_list_bulleted_white_48dp)
                    .onClick { activity.triggerMarkdown(MarkdownType.UNORDERED) })
            .child(bottomBarRoundIcon(context, colorConfig)
                    .iconRes(R.drawable.ic_more_horiz_white_48dp)
                    .onClick { }
                    .isClickDisabled(true)
                    .clickHandler(toggleButtonClick))
            .build()
  }
}

@LayoutSpec
object NoteCreationAllMarkdownsBottomBarSpec {
  @OnCreateLayout
  fun onCreate(context: ComponentContext,
               @Prop colorConfig: ToolbarColorConfig): Component {
    val activity = context.androidContext as CreateNoteActivity
    return Row.create(context)
        .alignSelf(YogaAlign.CENTER)
        .alignItems(YogaAlign.CENTER)
        .child(bottomBarRoundIcon(context, colorConfig)
            .iconRes(R.drawable.ic_title_white_48dp)
            .onClick { activity.triggerMarkdown(MarkdownType.HEADER) })
          .child(bottomBarRoundIcon(context, colorConfig)
                  .iconRes(R.drawable.ic_markdown_bold)
                  .onClick { activity.triggerMarkdown(MarkdownType.BOLD) })
          .child(bottomBarRoundIcon(context, colorConfig)
                  .iconRes(R.drawable.ic_markdown_italics)
                  .onClick { activity.triggerMarkdown(MarkdownType.ITALICS) })
          .child(bottomBarRoundIcon(context, colorConfig)
                  .iconRes(R.drawable.ic_format_list_bulleted_white_48dp)
                  .onClick { activity.triggerMarkdown(MarkdownType.UNORDERED) })
        .child(bottomBarRoundIcon(context, colorConfig)
            .iconRes(R.drawable.ic_title_white_48dp)
            .iconPaddingRes(R.dimen.toolbar_round_icon_padding_subsize)
            .onClick { activity.triggerMarkdown(MarkdownType.SUB_HEADER) })
        .child(bottomBarRoundIcon(context, colorConfig)
                .iconRes(R.drawable.ic_check_box_white_24dp)
                .onClick { activity.triggerMarkdown(MarkdownType.CHECKLIST_UNCHECKED) })
        .child(bottomBarRoundIcon(context, colorConfig)
            .iconRes(R.drawable.ic_markdown_underline)
            .onClick { activity.triggerMarkdown(MarkdownType.UNDERLINE) })
        .child(bottomBarRoundIcon(context, colorConfig)
            .iconRes(R.drawable.ic_code_white_48dp)
            .onClick { activity.triggerMarkdown(MarkdownType.CODE_BLOCK) })
        .child(bottomBarRoundIcon(context, colorConfig)
            .iconRes(R.drawable.icon_code_block)
            .onClick { activity.triggerMarkdown(MarkdownType.CODE) })
        .child(bottomBarRoundIcon(context, colorConfig)
            .iconRes(R.drawable.ic_markdown_strikethrough)
            .onClick { activity.triggerMarkdown(MarkdownType.STRIKE_THROUGH) })
        .build()
  }
}

@LayoutSpec
object NoteViewBottomBarSpec {
  @OnCreateLayout
  fun onCreate(context: ComponentContext,
               @Prop colorConfig: ToolbarColorConfig): Component {
    val activity = context.androidContext as ViewAdvancedNoteActivity
    val row = Row.create(context)
        .widthPercent(100f)
        .alignItems(YogaAlign.CENTER)
    row.child(bottomBarRoundIcon(context, colorConfig)
        .bgColor(Color.TRANSPARENT)
        .iconRes(R.drawable.ic_apps_white_48dp)
        .onClick { activity.openMoreOptions() })
    row.child(EmptySpec.create(context).heightDip(1f).flexGrow(1f))

    row.child(bottomBarRoundIcon(context, colorConfig)
        .iconRes(R.drawable.icon_delete)
        .onClick { activity.moveItemToTrashOrDelete(activity.note()) })
    row.child(bottomBarRoundIcon(context, colorConfig)
        .iconRes(R.drawable.ic_content_copy_white_48dp)
        .onClick { activity.note().copy(activity) })
    row.child(bottomBarRoundIcon(context, colorConfig)
        .iconRes(R.drawable.ic_share_white_48dp)
        .onClick { activity.note().share(activity) })


    row.child(EmptySpec.create(context).heightDip(1f).flexGrow(1f))
    row.child(bottomBarRoundIcon(context, colorConfig)
        .bgColor(Color.TRANSPARENT)
        .iconRes(R.drawable.ic_edit_white_48dp)
        .onClick { activity.openEditor() })
    return bottomBarCard(context, row.build(), colorConfig).build()
  }
}
