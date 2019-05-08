package com.maubis.scarlet.base.note.creation.activity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.speech.RecognizerIntent
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ItemTouchHelper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.facebook.litho.ComponentContext
import com.facebook.litho.LithoView
import com.google.gson.JsonObject
import com.koushikdutta.ion.Ion
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.config.CoreConfig.Companion.foldersDb
import com.maubis.scarlet.base.core.format.Format
import com.maubis.scarlet.base.core.format.FormatBuilder
import com.maubis.scarlet.base.core.format.FormatType
import com.maubis.scarlet.base.core.format.MarkdownType
import com.maubis.scarlet.base.core.note.*
import com.maubis.scarlet.base.core.note.NoteImage.Companion.deleteIfExist
import com.maubis.scarlet.base.core.tag.TagBuilder
import com.maubis.scarlet.base.database.room.note.Note
import com.maubis.scarlet.base.nino.CameraActivity
import com.maubis.scarlet.base.note.*
import com.maubis.scarlet.base.note.creation.specs.NoteCreationBottomBar
import com.maubis.scarlet.base.note.creation.specs.NoteCreationTopBar
import com.maubis.scarlet.base.note.formats.recycler.FormatImageViewHolder
import com.maubis.scarlet.base.note.formats.recycler.FormatTextViewHolder
import com.maubis.scarlet.base.note.tag.save
import com.maubis.scarlet.base.settings.sheet.ColorPickerBottomSheet
import com.maubis.scarlet.base.settings.sheet.ColorPickerDefaultController
import com.maubis.scarlet.base.settings.sheet.UISettingsOptionsBottomSheet
import com.maubis.scarlet.base.support.recycler.SimpleItemTouchHelper
import com.maubis.scarlet.base.support.specs.ToolbarColorConfig
import kotlinx.android.synthetic.main.activity_advanced_note.*
import pl.aprilapps.easyphotopicker.DefaultCallback
import pl.aprilapps.easyphotopicker.EasyImage
import smartdevelop.ir.eram.showcaseviewlib.GuideView
import smartdevelop.ir.eram.showcaseviewlib.config.DismissType
import smartdevelop.ir.eram.showcaseviewlib.config.Gravity
import smartdevelop.ir.eram.showcaseviewlib.listener.GuideListener
import java.io.File
import java.util.*

open class CreateNoteActivity : ViewAdvancedNoteActivity() {

  private var active = false
  private var maxUid = 0

  private var historyIndex = 0
  private var historySize = 0L

  val history: MutableList<Note> = emptyList<Note>().toMutableList()

  override val editModeValue: Boolean get() = true

  var ninoUid = 0
  var ninoRequest = false
  val NINO_REQUEST = 434


  var guideViewOrder = 0
  lateinit var guideViews : Array<GuideView>

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setTouchListener()
    startHandler()

    if (getSupportActionBar() != null) {
      getSupportActionBar()?.setDisplayHomeAsUpEnabled(false);
      getSupportActionBar()?.setHomeButtonEnabled(false);
    }

  }

  fun initGuideViews() {
    val tutorialEnabled = CoreConfig.instance.store().get(UISettingsOptionsBottomSheet.KEY_TUTORIAL_ENABLED, true)

    if (tutorialEnabled) {
      guideViews = arrayOf(
              buildGuideView("Markdown", "You may format your texts through the markdown bar.", R.id.lithoTopToolbar),
              buildGuideView("Segments", "You may add segments through the segments bar.", R.id.lithoBottomToolbar)
      )


      guideViews[0].show()
      CoreConfig.instance.store().put(UISettingsOptionsBottomSheet.KEY_TUTORIAL_ENABLED, false)
    }


  }

  fun buildGuideView(title: String, contentText: String, targetViewId: Int) : GuideView {
    val gv = GuideView.Builder(this)
            .setTitle(title)
            .setContentText(contentText)
            .setGravity(Gravity.center) //optional
            .setDismissType(DismissType.anywhere) //optional - default DismissType.targetView
            .setTargetView(this.findViewById(targetViewId))
            .setGuideListener(getGuideListener(guideViewOrder + 1))
            .build()

    guideViewOrder += 1

    return gv
  }

  fun getGuideListener(idx: Int) : GuideListener {

    Log.v("GET guide listener ", idx.toString())
    return GuideListener {
      if (idx < guideViews.size) {
        tryClosingTheKeyboard()
        guideViews[idx].show()
      }
    }
  }
  
  override fun onCreationFinished() {
    super.onCreationFinished()
    history.add(NoteBuilder().copy(note!!))
    setFolderFromIntent()
    initGuideViews()
  }

  private fun setFolderFromIntent() {
    if (intent === null) {
      return
    }
    val folderUuid = intent.getStringExtra(INTENT_KEY_FOLDER)
    if (folderUuid === null || folderUuid.isBlank()) {
      return
    }
    val folder = foldersDb.getByUUID(folderUuid)
    if (folder === null) {
      return
    }
    note!!.folder = folder.uuid
  }

  private fun setTouchListener() {
    val callback = SimpleItemTouchHelper(adapter)
    val touchHelper = ItemTouchHelper(callback)
    touchHelper.attachToRecyclerView(formatsView)
  }

  override fun setNote() {
    super.setNote()
    maxUid = formats.size + 1

    val isEmpty = formats.isEmpty()
    when {
      isEmpty -> {
        addEmptyItem(0, FormatType.HEADING)
        addEmptyItem(FormatType.CHECKLIST_UNCHECKED)
        addDefaultItem()
      }
      !formats[0].text.startsWith("# ") &&
          formats[0].formatType !== FormatType.HEADING
          && formats[0].formatType !== FormatType.IMAGE -> {
        addEmptyItem(0, FormatType.HEADING)
      }
    }
  }

  protected open fun addDefaultItem() {
    addEmptyItem(FormatType.TEXT)
  }

  override fun notifyToolbarColor() {
    super.notifyToolbarColor()
    setBottomToolbar()
  }

  override fun setTopToolbar() {
    lithoTopToolbar.removeAllViews()
    val componentContext = ComponentContext(this)
    lithoTopToolbar.addView(
        LithoView.create(componentContext,
            NoteCreationTopBar.create(componentContext)
                    .colorConfig(ToolbarColorConfig(colorConfig.toolbarBackgroundColor, colorConfig.toolbarIconColor))
                    .build()))
  }

  override fun setBottomToolbar() {
    val componentContext = ComponentContext(this)
    lithoBottomToolbar.removeAllViews()
    lithoBottomToolbar.addView(
        LithoView.create(
            componentContext,
            NoteCreationBottomBar.create(componentContext)
                .colorConfig(ToolbarColorConfig(colorConfig.toolbarBackgroundColor, colorConfig.toolbarIconColor))
                .build()))
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    EasyImage.handleActivityResult(requestCode, resultCode, data, this, object : DefaultCallback() {
      override fun onImagePicked(imageFile: File?, source: EasyImage.ImageSource?, type: Int) {
        if (imageFile == null) {
          return
        }

        if(ninoRequest){
          val intent = Intent(context, CameraActivity::class.java)
          intent.putExtra("img_path", imageFile?.absolutePath)
          intent.putExtra("type", type)
          startActivityForResult(intent, NINO_REQUEST)
        }else{
          val targetFile = NoteImage(context).renameOrCopy(note!!, imageFile)
          val index = getFormatIndex(type)
          triggerImageLoaded(index, targetFile)
        }
      }

      override fun onImagePickerError(e: Exception, source: EasyImage.ImageSource, type: Int) {
        //Some error handling
      }
    })
    if (requestCode == NINO_REQUEST) {
      if(resultCode == RESULT_OK){
        ninoRequest = false
        val uri = Uri.parse(data?.getStringExtra("result_uri"))
        val text = data!!.getStringExtra("text")

        val targetFile = NoteImage(context).renameOrCopy(note!!, File(uri?.getPath()))

        val handler = Handler()
        handler.postDelayed({
        val index = getFormatIndex(data.getIntExtra("type", ninoUid+0))
        triggerImageLoaded(index, targetFile, text)
      }, 1000)
      }
    } else if (requestCode == 10) {
      Log.v("CreateNoteActivity", "request with requestCode 10")
      Log.v("CreateNoteActivity", "ResultCode " + resultCode.toString())


      if (resultCode == RESULT_OK && data != null) {
        val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
        Log.v("CreateNoteActivity", result.toString())
        val res = result[0]

        insertTextSegment(res)
      }

    } else if (requestCode == 801) {

      if (resultCode == RESULT_OK && data != null) {
        val res = data.getStringExtra("result");
        Log.v("CreateNoteActivity", res.toString())

        insertTextSegment(res)
      }
    } else if (requestCode > 801 && requestCode < 805) {
      if (resultCode == RESULT_OK && data != null) {


          val uri = Uri.parse(data.getStringExtra("result_uri"))
          Log.v("nino uri", uri?.getPath().toString())
          val targetFile = NoteImage(context).renameOrCopy(note!!, File(uri.getPath()))
        val handler = Handler()
          handler.postDelayed({
          val index = getFormatIndex(data.getIntExtra("type", ninoUid + 0))
          Log.v("iink ninoUid index", ninoUid.toString() + " " + index.toString())

            val handler = Handler()

          handler.postDelayed({
            Log.v(" can read targetFile", targetFile.canRead().toString())
            Log.v(" can  targetFile", targetFile.toURI().path.toString())

            triggerImageLoaded(index, targetFile)
          }, 1000)

          }, 1000)

      }
    }
  }

  private fun retryTriggerImageLoaded(position: Int, file: File) {
    val handler = Handler()
    handler.postDelayed(Runnable {

      if (file.canRead())
        triggerImageLoaded(position, file)
      else
        retryTriggerImageLoaded(position, file)

    }, 100)
  }

  private fun insertTextSegment(res: String) {
    this.addEmptyItemAtFocused(FormatType.TEXT)
    var position = formats.size - 1
    if(focusedFormat != null){
      position = getFormatIndex(focusedFormat!!) + 1
    }

    val handler = Handler()
    handler.postDelayed(Runnable {
      val holder = findTextViewHolderAtPosition(position) ?: return@Runnable
      holder.requestSpeechToTextAction(res)
    }, 100)

  }

  fun smartTagging() {
    val isSmartTaggingEnabled = CoreConfig.instance.store().get(UISettingsOptionsBottomSheet.KEY_SMART_TAGGING_ENABLED, true)
    if (isSmartTaggingEnabled) {
        val SERVER_POST_URL = "http://35.237.158.162:8000/api/analyze_text/"
        val text:String = note!!.getFullText()
        Log.v("NoteExtensions", "entered smartTagging with text: " + text )
        val json = JsonObject();
        json.addProperty("text", text);

        Ion.with(applicationContext)
              .load(SERVER_POST_URL)
              .setLogging("IONLOGS", Log.DEBUG)
              .setJsonObjectBody(json)
              .asJsonObject()
              .setCallback { e, res ->
                run {
                  val entitylist = res
                          .getAsJsonArray("entitylist")
                          .iterator()

                  Log.v("CreateNoteActivity", "ion result: " + res.toString())

                  val tb = TagBuilder()
                  for (jse in entitylist) {
                    val tag = tb.emptyTag()

                    tag.title = jse.asString
                    tag.uuid = jse.asString
                    Log.v("CreateNoteActivity", "tagtitle: " + tag.title)
                    tag.save()
                    note!!.addTag(tag)

                  }
                  notifyTagsChanged(note!!)
                }
              }
      }
  }

  override fun onPause() {
    super.onPause()

    active = false
    maybeUpdateNoteWithoutSync()
    val destroyed = destroyIfNeeded()
    if (!destroyed && !note!!.disableBackup) {
      note!!.saveToSync(this)
    }
  }

  fun onSave() {
    onBackPressed()
    val destroyed = destroyIfNeeded()
    if (!destroyed){
      smartTagging()
    }



  }

  override fun onBackPressed() {
    super.onBackPressed()
    tryClosingTheKeyboard()
  }

  override fun onResume() {
    super.onResume()
    active = true
  }

  override fun onResumeAction() {
    // do nothing
  }

  private fun destroyIfNeeded(): Boolean {
    if (note!!.isUnsaved()) {
      return true
    }
    if (note!!.getFormats().isEmpty()) {
      note!!.delete(this)
      return true
    }
    return false
  }

  protected fun maybeUpdateNoteWithoutSync() {
    val currentNote = note
    if (currentNote === null) {
      return
    }

    val vLastNoteInstance = history.getOrNull(historyIndex) ?: currentNote
    currentNote.description = FormatBuilder().getSmarterDescription(formats)

    // Ignore update if nothing changed. It allows for one undo per few seconds
    if (currentNote.isEqual(vLastNoteInstance)) {
      return
    }

    addNoteToHistory(NoteBuilder().copy(currentNote))
    currentNote.updateTimestamp = Calendar.getInstance().timeInMillis
    maybeSaveNote(false)
  }

  @Synchronized
  private fun addNoteToHistory(note: Note) {
    while (historyIndex != history.size - 1) {
      history.removeAt(historyIndex)
    }

    history.add(note)
    historySize += note.description.length
    historyIndex += 1

    // 0.5MB limit on history
    if (historySize >= 1024 * 512 || history.size >= 15) {
      val item = history.removeAt(0)
      historySize -= item.description.length
      historyIndex -= 1
    }
  }


  private fun startHandler() {
    val handler = Handler()
    handler.postDelayed(object : Runnable {
      override fun run() {
        if (active) {
          maybeUpdateNoteWithoutSync()
          handler.postDelayed(this, HANDLER_UPDATE_TIME.toLong())
        }
      }
    }, HANDLER_UPDATE_TIME.toLong())
  }

  fun addEmptyItem(type: FormatType) {
    addEmptyItem(formats.size, type)
  }

  private fun addEmptyItem(position: Int, type: FormatType) {
    val format = Format(type)
    format.uid = maxUid + 1
    maxUid++

    ninoUid = format.uid + 0

    formats.add(position, format)
    adapter.addItem(format, position)
  }

  fun addEmptyItemAtFocused(type: FormatType) {
    if (focusedFormat == null) {
      addEmptyItem(type)
      return
    }

    val position = getFormatIndex(focusedFormat!!)
    if (position == -1) {
      addEmptyItem(type)
      return
    }

    val newPosition = position + 1
    addEmptyItem(newPosition, type)
    formatsView.layoutManager?.scrollToPosition(newPosition)
    focus(newPosition)
  }

  fun focus(position: Int) {
    val handler = Handler()
    handler.postDelayed(Runnable {
      val holder = findTextViewHolderAtPosition(position) ?: return@Runnable
      holder.requestEditTextFocus()
    }, 100)
  }

  fun triggerMarkdown(markdownType: MarkdownType) {
    if (focusedFormat == null) {
      return
    }

    val position = getFormatIndex(focusedFormat!!)
    if (position == -1) {
      return
    }

    val handler = Handler()
    handler.postDelayed(Runnable {
      val holder = findTextViewHolderAtPosition(position) ?: return@Runnable
      holder.requestMarkdownAction(markdownType)
    }, 100)
  }

  fun triggerImageLoaded(position: Int, file: File, ninoText: String = "") {
    if (position == -1) {
      return
    }

    val holder = findImageViewHolderAtPosition(position) ?: return
    holder.populateFile(file)

    val formatToChange = formats[position]
    if (!formatToChange.text.isBlank()) {
      val noteImage = NoteImage(context)
      deleteIfExist(noteImage.getFile(note!!.uuid, formatToChange.text.split("<Nino> ")[0]))
    }
    if (ninoText.isNotEmpty())
      formatToChange.text = file.name + "<Nino> " + ninoText
    else
      formatToChange.text = file.name

    setFormat(formatToChange)
  }

  fun onHistoryClick(undo: Boolean) {
    when (undo) {
      true -> {
        historyIndex = if (historyIndex == 0) 0 else (historyIndex - 1)
        note = NoteBuilder().copy(history.get(historyIndex))
        setNote()
      }
      false -> {
        val maxHistoryIndex = history.size - 1
        historyIndex = if (historyIndex == maxHistoryIndex) maxHistoryIndex else (historyIndex + 1)
        note = NoteBuilder().copy(history.get(historyIndex))
        setNote()
      }
    }
  }

  fun onColorChangeClick() {
    val config = ColorPickerDefaultController(
        title = R.string.choose_note_color,
        colors = listOf(resources.getIntArray(R.array.bright_colors), resources.getIntArray(R.array.bright_colors_accent)),
        selectedColor = note!!.color,
        onColorSelected = { color ->
          setNoteColor(color)
        }
    )
    com.maubis.scarlet.base.support.sheets.openSheet(this, ColorPickerBottomSheet().apply { this.config = config })
  }

  private fun findTextViewHolderAtPosition(position: Int): FormatTextViewHolder? {
    val holder = findViewHolderAtPositionAggressively(position)
    return if (holder !== null && holder is FormatTextViewHolder) holder else null
  }

  private fun findImageViewHolderAtPosition(position: Int): FormatImageViewHolder? {
    val holder = findViewHolderAtPositionAggressively(position)
    val bool = holder is FormatImageViewHolder
    return if (holder !== null && holder is FormatImageViewHolder) holder else null
  }

  fun getSpeechInput() {
    val activity = this
    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())

    if (intent.resolveActivity(activity.packageManager) != null) {

      ActivityCompat.startActivityForResult(activity, intent, 10, null)

    } else {
      Toast.makeText(activity, "Your Device Don't Support Speech Input", Toast.LENGTH_SHORT).show()
    }
  }

  private fun findViewHolderAtPositionAggressively(position: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder? {
    var holder: androidx.recyclerview.widget.RecyclerView.ViewHolder? = formatsView.findViewHolderForAdapterPosition(position)
    if (holder == null) {
      holder = formatsView.findViewHolderForLayoutPosition(position)
      if (holder == null) {
        return null
      }
    }
    return holder
  }

  override fun setNoteColor(color: Int) {
    if (lastKnownNoteColor == color) {
      return
    }
    note!!.color = color
    notifyToolbarColor()
    lastKnownNoteColor = color
  }

  override fun setFormat(format: Format) {
    val position = getFormatIndex(format)
    if (position == -1) {
      return
    }
    formats[position] = format
  }

  override fun moveFormat(fromPosition: Int, toPosition: Int) {
    if (fromPosition < toPosition) {
      for (i in fromPosition until toPosition) {
        Collections.swap(formats, i, i + 1)
      }
    } else {
      for (i in fromPosition downTo toPosition + 1) {
        Collections.swap(formats, i, i - 1)
      }
    }
    maybeUpdateNoteWithoutSync()
  }

  override fun deleteFormat(format: Format) {
    val position = getFormatIndex(format)
    if (position < 0) {
      return
    }
    focusedFormat = if (focusedFormat == null || focusedFormat!!.uid == format.uid) null else focusedFormat
    formats.removeAt(position)
    adapter.removeItem(position)
    maybeUpdateNoteWithoutSync()
  }

  override fun setFormatChecked(format: Format, checked: Boolean) {
    // do nothing
  }

  override fun createOrChangeToNextFormat(format: Format) {
    val position = getFormatIndex(format)
    if (position == -1) {
      return
    }

    val isCheckList =
        (format.formatType === FormatType.CHECKLIST_UNCHECKED
            || format.formatType === FormatType.CHECKLIST_CHECKED)
    val newPosition = position + 1
    when {
      isCheckList -> addEmptyItemAtFocused(FormatBuilder().getNextFormatType(FormatType.CHECKLIST_UNCHECKED))
      newPosition < formats.size -> focus(position + 1)
      else -> addEmptyItemAtFocused(FormatBuilder().getNextFormatType(format.formatType))
    }
  }

  companion object {
    private const val INTENT_KEY_FOLDER = "key_folder"

    fun getNewNoteIntent(
        context: Context,
        folder: String = ""): Intent {
      val intent = Intent(context, CreateNoteActivity::class.java)
      intent.putExtra(INTENT_KEY_FOLDER, folder)
      return intent
    }

    fun getNewChecklistNoteIntent(
        context: Context,
        folder: String = ""): Intent {
      val intent = Intent(context, CreateListNoteActivity::class.java)
      intent.putExtra(INTENT_KEY_FOLDER, folder)
      return intent
    }
  }
}
