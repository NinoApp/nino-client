package com.maubis.scarlet.base.note.creation.activity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.View
import com.facebook.litho.ComponentContext
import com.facebook.litho.LithoView
import com.github.bijoysingh.starter.recyclerview.MultiRecyclerViewControllerItem
import com.github.bijoysingh.starter.recyclerview.RecyclerViewBuilder
import com.google.gson.JsonObject
import com.koushikdutta.ion.Ion
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.config.CoreConfig.Companion.notesDb
import com.maubis.scarlet.base.core.format.Format
import com.maubis.scarlet.base.core.format.FormatBuilder
import com.maubis.scarlet.base.core.format.FormatType
import com.maubis.scarlet.base.core.format.sectionPreservingSort
import com.maubis.scarlet.base.core.note.*
import com.maubis.scarlet.base.database.room.note.Note
import com.maubis.scarlet.base.note.*
import com.maubis.scarlet.base.note.actions.NoteOptionsBottomSheet
import com.maubis.scarlet.base.note.activity.INoteOptionSheetActivity
import com.maubis.scarlet.base.note.creation.specs.NoteViewBottomBar
import com.maubis.scarlet.base.note.creation.specs.NoteViewTopBar
import com.maubis.scarlet.base.note.formats.FormatAdapter
import com.maubis.scarlet.base.note.formats.IFormatRecyclerViewActivity
import com.maubis.scarlet.base.note.formats.getFormatControllerItems
import com.maubis.scarlet.base.note.formats.recycler.KEY_EDITABLE
import com.maubis.scarlet.base.note.formats.recycler.KEY_NOTE_COLOR
import com.maubis.scarlet.base.settings.sheet.*
import com.maubis.scarlet.base.settings.sheet.SettingsOptionsBottomSheet.Companion.KEY_MARKDOWN_ENABLED
import com.maubis.scarlet.base.settings.sheet.UISettingsOptionsBottomSheet.Companion.useNoteColorAsBackground
import com.maubis.scarlet.base.support.specs.ToolbarColorConfig
import com.maubis.scarlet.base.support.specs.ToolbarQuizConfig
import com.maubis.scarlet.base.support.ui.*
import com.maubis.scarlet.base.support.ui.ColorUtil.darkerColor
import com.maubis.scarlet.base.support.utils.bind
import kotlinx.android.synthetic.main.activity_advanced_note.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.collections.ArrayList


const val INTENT_KEY_NOTE_ID = "NOTE_ID"
const val INTENT_KEY_DISTRACTION_FREE = "DISTRACTION_FREE"


data class NoteViewColorConfig(
    var backgroundColor: Int = Color.BLACK,
    var toolbarBackgroundColor: Int = Color.BLACK,
    var toolbarIconColor: Int = Color.BLACK,
    var statusBarColor: Int = Color.BLACK)

open class ViewAdvancedNoteActivity : ThemedActivity(), INoteOptionSheetActivity, IFormatRecyclerViewActivity {

  var focusedFormat: Format? = null
  protected var note: Note? = null

  protected lateinit var context: Context
  protected lateinit var adapter: FormatAdapter
  protected lateinit var formats: MutableList<Format>
  protected lateinit var formatsView: androidx.recyclerview.widget.RecyclerView
  protected var isDistractionFree: Boolean = false

  val creationFinished = AtomicBoolean(false)
  val colorConfig = NoteViewColorConfig()
  var lastKnownNoteColor = 0

  val rootView: View by bind(R.id.root_layout)
  var questions: ArrayList<String> = ArrayList()
  var answers: ArrayList<String> = ArrayList()

  protected open val editModeValue: Boolean
    get() = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_advanced_note)
    context = this
    isDistractionFree = intent.getBooleanExtra(INTENT_KEY_DISTRACTION_FREE, false)

    setRecyclerView()

    val fab: View = findViewById(R.id.nino_fab)
    fab.visibility = View.INVISIBLE

    GlobalScope.launch(Dispatchers.IO) {
      var noteId = intent.getIntExtra(INTENT_KEY_NOTE_ID, 0)
      if (noteId == 0 && savedInstanceState != null) {
        noteId = savedInstanceState.getInt(INTENT_KEY_NOTE_ID, 0)
      }
      if (noteId != 0) {
        note = notesDb.getByID(noteId)
      }
      if (note === null) {
        note = NoteBuilder().emptyNote(sNoteDefaultColor)
      }
      GlobalScope.launch(Dispatchers.Main) {
        resetBundle()
        setNote()
        if (isDistractionFree) {
          startDistractionFreeMode()
        }
        notifyThemeChange()
        onCreationFinished()
      }
      creationFinished.set(true)

    }
  }

  override fun onResume() {
    super.onResume()
    CoreConfig.instance.startListener(this)

    if (!creationFinished.get()) {
      return
    }
    onResumeAction()
    notifyThemeChange()
  }

  protected open fun onCreationFinished() {
    val SERVER_POST_URL = "http://35.237.158.162:8000/api/analyze_text/"

    val text:String = note!!.getFullText()

    val json = JsonObject();
    json.addProperty("text", text);

    Log.v("IONViewNote", "text" + text)
    Ion.with(context)
            .load(SERVER_POST_URL)
            .setLogging("IONLOGS", Log.DEBUG)
            .setJsonObjectBody(json)
            .asJsonObject()
            .setCallback { e, res -> run {
              //Log.v("IONViewNote", res.toString())
              //Log.v("IONViewNote", e.toString())
              val questionsJsonArray = res
                      .getAsJsonArray("questions")
                      .iterator()

              val questions = ArrayList<String>()
              val answers = ArrayList<String>()
              for (jsq in questionsJsonArray) {
                val jso = jsq.asJsonObject
                val question = jso.getAsJsonPrimitive("question").asString
                val answer = jso.getAsJsonPrimitive("answer").asString
                questions.add(question)
                answers.add(answer)
              }
              notifyQuestions(questions, answers)

            } }
  }

  protected open fun onResumeAction() {
    GlobalScope.launch(Dispatchers.IO) {
      note = notesDb.getByID(intent.getIntExtra(INTENT_KEY_NOTE_ID, 0))
      when {
        note == null -> finish()
        else -> GlobalScope.launch(Dispatchers.Main) { setNote() }
      }
    }
  }

  private fun startDistractionFreeMode() {
    var uiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        or View.SYSTEM_UI_FLAG_FULLSCREEN)
    if (Build.VERSION.SDK_INT >= 19) {
      uiVisibility = (uiVisibility or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }
    window.decorView.systemUiVisibility = uiVisibility
  }

  private fun resetBundle() {
    val currentNote = note
    val bundle = Bundle()
    bundle.putBoolean(KEY_EDITABLE, editModeValue)
    bundle.putBoolean(KEY_MARKDOWN_ENABLED, CoreConfig.instance.store().get(KEY_MARKDOWN_ENABLED, true))
    bundle.putBoolean(KEY_NIGHT_THEME, CoreConfig.instance.themeController().isNightTheme())
    bundle.putInt(STORE_KEY_TEXT_SIZE, sEditorTextSize)
    bundle.putInt(KEY_NOTE_COLOR, currentNote?.color ?: sNoteDefaultColor)
    bundle.putString(INTENT_KEY_NOTE_ID, currentNote?.uuid ?: generateUUID())
    adapter.setExtra(bundle)
  }

  protected open fun setNote() {
    val currentNote = note
    if (currentNote === null) {
      return
    }

    setNoteColor(currentNote.color)
    adapter.clearItems()

    if (isDistractionFree) {
      adapter.addItem(Format(FormatType.SEPARATOR))
    }

    formats = when (editModeValue) {
      true -> currentNote.getFormats()
      false -> currentNote.getSmartFormats()
    }.toMutableList()
    adapter.addItems(formats)

    if (!editModeValue) {
      maybeAddTags()
      maybeAddEmptySpace()
    }
  }

  private fun maybeAddTags() {
    val currentNote = note
    if (currentNote === null) {
      return
    }

    val tagLabel = currentNote.getTagString()
    if (tagLabel.isEmpty()) {
      return
    }

    val format = Format(FormatType.TAG, tagLabel)
    adapter.addItem(format)
  }

  private fun maybeAddEmptySpace() {
    adapter.addItem(Format(FormatType.EMPTY))
  }

  private fun setRecyclerView() {
    adapter = FormatAdapter(this)
    formatsView = RecyclerViewBuilder(this)
        .setAdapter(adapter)
        .setView(this, R.id.advanced_note_recycler)
        .build()
  }

  open fun setFormat(format: Format) {
    // do nothing
  }

  open fun createOrChangeToNextFormat(format: Format) {
    // do nothing
  }

  open fun setFormatChecked(format: Format, checked: Boolean) {
    val trueFormats = note!!.getFormats().toMutableList()
    val truePosition = trueFormats.indexOfFirst { it.uid == format.uid }
    if (truePosition == -1) {
      return
    }

    val position = getFormatIndex(format)
    if (position == -1) {
      return
    }

    format.formatType = if (checked) FormatType.CHECKLIST_CHECKED else FormatType.CHECKLIST_UNCHECKED
    formats[position] = format
    adapter.updateItem(format, position)

    trueFormats[truePosition] = format

    note!!.description = FormatBuilder().getSmarterDescription(sectionPreservingSort(trueFormats))
    setNote()
    maybeSaveNote(true)
  }

  var optionsBottomSheet: NoteOptionsBottomSheet? = null
  fun openMoreOptions() {
    optionsBottomSheet = NoteOptionsBottomSheet.openSheet(this@ViewAdvancedNoteActivity, note!!, questions, answers)
  }

  fun openEditor() {
    note!!.openEdit(context)
  }

  protected open fun notifyToolbarColor() {
    val currentNote = note
    if (currentNote === null) {
      return
    }

    val theme = CoreConfig.instance.themeController()
    when {
      !useNoteColorAsBackground -> {
        colorConfig.backgroundColor = theme.get(ThemeColorType.BACKGROUND)
        colorConfig.toolbarIconColor = theme.get(ThemeColorType.TOOLBAR_ICON)
        colorConfig.statusBarColor = colorConfig.backgroundColor
        colorConfig.toolbarBackgroundColor = theme.get(ThemeColorType.TOOLBAR_BACKGROUND)
      }
      ColorUtil.isLightColored(currentNote.color) -> {
        colorConfig.backgroundColor = currentNote.color
        colorConfig.toolbarIconColor = theme.get(context, Theme.DARK, ThemeColorType.TOOLBAR_ICON)
        colorConfig.statusBarColor = darkerColor(currentNote.color)
        colorConfig.toolbarBackgroundColor = colorConfig.statusBarColor
      }
      else -> {
        colorConfig.backgroundColor = currentNote.color
        colorConfig.toolbarIconColor = theme.get(context, Theme.DARK, ThemeColorType.TOOLBAR_ICON)
        colorConfig.statusBarColor = darkerColor(currentNote.color)
        colorConfig.toolbarBackgroundColor = colorConfig.statusBarColor
      }
    }

    setSystemTheme(colorConfig.statusBarColor)
    rootView.setBackgroundColor(colorConfig.backgroundColor)
    formatsView.setBackgroundColor(colorConfig.backgroundColor)

    resetBundle()
    adapter.notifyDataSetChanged()

    setBottomToolbar()
    setTopToolbar()
  }

  protected open fun setBottomToolbar() {
    lithoBottomToolbar.removeAllViews()
    val componentContext = ComponentContext(this)
    lithoBottomToolbar.addView(
        LithoView.create(componentContext,
            NoteViewBottomBar.create(componentContext)
                .colorConfig(ToolbarColorConfig(colorConfig.toolbarBackgroundColor, colorConfig.toolbarIconColor))
                    .build()))
  }

  protected open fun setTopToolbar() {
    lithoTopToolbar.removeAllViews()
    val componentContext = ComponentContext(this)
    lithoTopToolbar.addView(
        LithoView.create(componentContext,
            NoteViewTopBar.create(componentContext).build()))
  }

  protected open fun setNoteColor(color: Int) {

  }

  protected fun maybeSaveNote(sync: Boolean) {
    if (note!!.getFormats().isEmpty() && note!!.isUnsaved()) {
      return
    }
    note!!.updateTimestamp = Calendar.getInstance().timeInMillis
    when (sync) {
      true -> note!!.save(context)
      false -> note!!.saveWithoutSync(context)
    }
  }

  fun notifyNoteChange() {
    notifyToolbarColor()
  }

  protected fun getFormatIndex(format: Format): Int = getFormatIndex(format.uid)

  protected fun getFormatIndex(formatUid: Int): Int {
    var position = 0
    for (fmt in formats) {
      if (fmt.uid == formatUid) {
        return position
      }
      position++
    }
    return -1
  }

  override fun notifyThemeChange() {
    notifyToolbarColor()
  }

  public override fun onSaveInstanceState(savedInstanceState: Bundle?) {
    super.onSaveInstanceState(savedInstanceState)
    if (savedInstanceState == null) {
      return
    }
    savedInstanceState.putInt(INTENT_KEY_NOTE_ID, if (note == null || note!!.uid == null) 0 else note!!.uid)
  }

  fun note() = note!!

  companion object {
    const val HANDLER_UPDATE_TIME = 1000

    fun getIntent(context: Context, note: Note): Intent {
      val intent = Intent(context, ViewAdvancedNoteActivity::class.java)
      intent.putExtra(INTENT_KEY_NOTE_ID, note.uid)
      return intent
    }
  }

  /**
   * Start : INoteOptionSheetActivity Functions
   */

  override fun updateNote(note: Note) {
    note.save(this)
    notifyNoteChange()
  }

  override fun markItem(note: Note, state: NoteState) {
    note.mark(this, state)
  }

  override fun moveItemToTrashOrDelete(note: Note) {
    note.softDelete(context)
    finish()
  }

  override fun notifyTagsChanged(note: Note) {
    setNote()
  }

  fun notifyQuestions(questions: ArrayList<String>, answers: ArrayList<String>) {
    Log.v("notifyQuestionnnnnssss", questions.size.toString())
    optionsBottomSheet?.quizBottomSheet?.notifyQuestions(questions, answers)
    this.questions = questions
    this.answers = answers
  }

  override fun getSelectMode(note: Note): String {
    return NoteState.DEFAULT.name
  }

  override fun notifyResetOrDismiss() {
    finish()
  }

  override fun lockedContentIsHidden() = false

  /**
   * End : INoteOptionSheetActivity
   */


  /**
   * Start : IFormatRecyclerView Functions
   */

  override fun context(): Context {
    return this
  }

  override fun controllerItems(): List<MultiRecyclerViewControllerItem<Format>> {
    return getFormatControllerItems()
  }

  override fun deleteFormat(format: Format) {
    // do nothing
  }

  override fun moveFormat(fromPosition: Int, toPosition: Int) {
    // do nothing
  }

  /**
   * End : IFormatRecyclerView
   */

}
