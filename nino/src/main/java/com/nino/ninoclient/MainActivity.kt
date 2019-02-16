package com.nino.ninoclient

import android.content.BroadcastReceiver
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.View.GONE
import android.widget.GridLayout.VERTICAL
import com.facebook.litho.ComponentContext
import com.facebook.litho.LithoView
import com.github.bijoysingh.starter.recyclerview.RecyclerViewBuilder
import com.nino.ninoclient.config.CoreConfig
import com.nino.ninoclient.config.CoreConfig.Companion.notesDb
import com.nino.ninoclient.core.note.NoteState
import com.nino.ninoclient.core.note.sort
import com.nino.ninoclient.database.room.note.Note
import com.nino.ninoclient.database.room.tag.Tag
import com.nino.ninoclient.export.support.NoteExporter
import com.nino.ninoclient.export.support.PermissionUtils
import com.nino.ninoclient.main.HomeNavigationState
import com.nino.ninoclient.main.recycler.*
import com.nino.ninoclient.main.sheets.AlertBottomSheet
import com.nino.ninoclient.main.sheets.WhatsNewItemsBottomSheet
import com.nino.ninoclient.main.specs.MainActivityBottomBar
import com.nino.ninoclient.main.specs.MainActivityFolderBottomBar
import com.nino.ninoclient.main.utils.MainSnackbar
import com.nino.ninoclient.note.activity.INoteOptionSheetActivity
import com.nino.ninoclient.note.folder.FolderRecyclerItem
import com.nino.ninoclient.note.folder.sheet.CreateOrEditFolderBottomSheet
import com.nino.ninoclient.note.mark
import com.nino.ninoclient.note.recycler.NoteAppAdapter
import com.nino.ninoclient.note.recycler.NoteRecyclerItem
import com.nino.ninoclient.note.save
import com.nino.ninoclient.note.softDelete
import com.nino.ninoclient.note.tag.view.TagsAndColorPickerViewHolder
import com.nino.ninoclient.service.SyncedNoteBroadcastReceiver
import com.nino.ninoclient.service.getNoteIntentFilter
import com.nino.ninoclient.settings.sheet.LineCountBottomSheet
import com.nino.ninoclient.settings.sheet.LineCountBottomSheet.Companion.KEY_LINE_COUNT
import com.nino.ninoclient.settings.sheet.SettingsOptionsBottomSheet.Companion.KEY_MARKDOWN_ENABLED
import com.nino.ninoclient.settings.sheet.SettingsOptionsBottomSheet.Companion.KEY_MARKDOWN_HOME_ENABLED
import com.nino.ninoclient.settings.sheet.SortingOptionsBottomSheet
import com.nino.ninoclient.settings.sheet.UISettingsOptionsBottomSheet
import com.nino.ninoclient.support.SearchConfig
import com.nino.ninoclient.support.database.HouseKeeperJob
import com.nino.ninoclient.support.database.Migrator
import com.nino.ninoclient.support.recycler.RecyclerItem
import com.nino.ninoclient.support.specs.ToolbarColorConfig
import com.nino.ninoclient.support.ui.ThemeColorType
import com.nino.ninoclient.support.ui.ThemedActivity
import com.nino.ninoclient.support.unifiedFolderSearchSynchronous
import com.nino.ninoclient.support.unifiedSearchSynchronous
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.search_toolbar_main.*
import kotlinx.android.synthetic.main.toolbar_trash_info.*
import kotlinx.coroutines.*

//Todo Phase 1 => Login Page -> Empty Page -> Take picture and submit to pipeline -> get result and turn into note.
// Phase 1
// Login Page:
// 1) Move login and register activity. DONE
// 2) Change main activity to Logic activity and bd. DONE
// 3) (Bonus) Change theme to match scarlet.
// Empty Page:
// 1) Make "Add note" button open camera activity of nino. DONE
// 2) (Bonus) Change theme to match scarlet.
// Camera:
// 1) Make sure server/pipeline works and gives back processed image.
// 2) Turn image into note.

//Todo Phase 2 => Login Page -> Get notes of that user -> show them.

// Phase 2
// 1) Make sure valid users can connect.
// 2) Obtain notes of user.
// 3) Present notes in main menu.
// 4) Create new notes and save them to django.
// 5) Relog to see changes.

//Todo Phase 3 => Create Notebooks -> Move notes to other notebooks. Save notebook info to django.

// Phase 3
// 1) Make sure notes can be moved around folders.
// 2) Save notebook info of each note to django (base notebook for notebook-less notes?).

//Todo Phase 4 => Cleanup

// Phase 4
// 1) REMOVE NOTE POP UPS THAT SAY "UPGRADE TO PRO"
// 2) Remove unrelated options or edit them  (e.g. "About us")

class MainActivity : ThemedActivity(), INoteOptionSheetActivity {
  private val singleThreadDispatcher = newSingleThreadContext("singleThreadDispatcher")

  private lateinit var recyclerView: RecyclerView
  private lateinit var adapter: NoteAppAdapter
  private lateinit var snackbar: MainSnackbar

  private lateinit var receiver: BroadcastReceiver
  private lateinit var tagAndColorPicker: TagsAndColorPickerViewHolder

  var config: SearchConfig = SearchConfig(mode = HomeNavigationState.DEFAULT)
  var isInSearchMode: Boolean = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // Migrate to the newer version of the tags
    Migrator(this).start()

    config.mode = HomeNavigationState.DEFAULT

    setupRecyclerView()
    setListeners()
    notifyThemeChange()

    WhatsNewItemsBottomSheet.maybeOpenSheet(this)
  }

  fun setListeners() {
    snackbar = MainSnackbar(bottomSnackbar, { setupData() })
    deleteTrashIcon.setOnClickListener { AlertBottomSheet.openDeleteTrashSheet(this@MainActivity) }
    searchBackButton.setOnClickListener {
      onBackPressed()
    }
    searchCloseIcon.setOnClickListener { onBackPressed() }
    searchBox.addTextChangedListener(object : TextWatcher {
      override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

      }

      override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
        startSearch(charSequence.toString())
      }

      override fun afterTextChanged(editable: Editable) {

      }
    })
    tagAndColorPicker = TagsAndColorPickerViewHolder(
        this,
        tagsFlexBox,
        { tag ->
          val isTagSelected = config.tags.filter { it.uuid == tag.uuid }.isNotEmpty()
          when (isTagSelected) {
            true -> {
              config.tags.removeAll { it.uuid == tag.uuid }
              startSearch(searchBox.text.toString())
              tagAndColorPicker.notifyChanged()
            }
            false -> {
              openTag(tag)
              tagAndColorPicker.notifyChanged()
            }
          }
        },
        { color ->
          when (config.colors.contains(color)) {
            true -> config.colors.remove(color)
            false -> config.colors.add(color)
          }
          tagAndColorPicker.notifyChanged()
          startSearch(searchBox.text.toString())
        })
  }

  fun setupRecyclerView() {
    val staggeredView = UISettingsOptionsBottomSheet.useGridView
    val isTablet = resources.getBoolean(R.bool.is_tablet)

    val isMarkdownEnabled = CoreConfig.instance.store().get(KEY_MARKDOWN_ENABLED, true)
    val isMarkdownHomeEnabled = CoreConfig.instance.store().get(KEY_MARKDOWN_HOME_ENABLED, true)
    val adapterExtra = Bundle()
    adapterExtra.putBoolean(KEY_MARKDOWN_ENABLED, isMarkdownEnabled && isMarkdownHomeEnabled)
    adapterExtra.putInt(KEY_LINE_COUNT, LineCountBottomSheet.getDefaultLineCount())

    adapter = NoteAppAdapter(this, staggeredView, isTablet)
    adapter.setExtra(adapterExtra)
    recyclerView = RecyclerViewBuilder(this)
        .setView(this, R.id.recycler_view)
        .setAdapter(adapter)
        .setLayoutManager(getLayoutManager(staggeredView, isTablet))
        .build()
  }

  private fun getLayoutManager(isStaggeredView: Boolean, isTabletView: Boolean): RecyclerView.LayoutManager {
    return when {
      isTabletView || isStaggeredView -> StaggeredGridLayoutManager(2, VERTICAL)
      else -> LinearLayoutManager(this)
    }
  }

  fun notifyAdapterExtraChanged() {
    setupRecyclerView()
    resetAndSetupData()
  }

  /**
   * Start: Home Navigation Clicks
   */
  fun onHomeClick() {
    GlobalScope.launch(Dispatchers.Main) {
      config.resetMode(HomeNavigationState.DEFAULT)
      unifiedSearch()
      notifyModeChange()
    }
  }

  fun onFavouritesClick() {
    GlobalScope.launch(Dispatchers.Main) {
      config.resetMode(HomeNavigationState.FAVOURITE)
      unifiedSearch()
      notifyModeChange()
    }
  }

  fun onArchivedClick() {
    GlobalScope.launch(Dispatchers.Main) {
      config.resetMode(HomeNavigationState.ARCHIVED)
      unifiedSearch()
      notifyModeChange()
    }
  }

  fun onTrashClick() {
    GlobalScope.launch(Dispatchers.Main) {
      config.resetMode(HomeNavigationState.TRASH)
      unifiedSearch()
      notifyModeChange()
    }
  }

  fun onLockedClick() {
    config.resetMode(HomeNavigationState.LOCKED)
    GlobalScope.launch(Dispatchers.Main) {
      val items = GlobalScope.async(Dispatchers.IO) {
        val sorting = SortingOptionsBottomSheet.getSortingState()
        sort(notesDb.getNoteByLocked(true), sorting)
            .map { NoteRecyclerItem(this@MainActivity, it) }
      }
      handleNewItems(items.await())
    }
    notifyModeChange()
  }

  private fun notifyModeChange() {
    val isTrash = config.mode === HomeNavigationState.TRASH
    deleteToolbar.visibility = if (isTrash) View.VISIBLE else GONE
  }

  /**
   * End: Home Navigation Clicks
   */

  private fun handleNewItems(notes: List<RecyclerItem>) {
    adapter.clearItems()
    if (!isInSearchMode) {
      adapter.addItem(GenericRecyclerItem(RecyclerItem.Type.TOOLBAR))
    }
    if (notes.isEmpty()) {
      adapter.addItem(EmptyRecyclerItem())
      return
    }
    notes.forEach {
      adapter.addItem(it)
    }
    addInformationItem(1)
  }

  private fun addInformationItem(index: Int) {
    val informationItem = when {
      shouldShowMigrateToProAppInformationItem(this) -> getMigrateToProAppInformationItem(this)
      shouldShowSignInformationItem() -> getSignInInformationItem(this)
      shouldShowAppUpdateInformationItem() -> getAppUpdateInformationItem(this)
      shouldShowReviewInformationItem() -> getReviewInformationItem(this)
      shouldShowInstallProInformationItem() -> getInstallProInformationItem(this)
      shouldShowThemeInformationItem() -> getThemeInformationItem(this)
      shouldShowBackupInformationItem() -> getBackupInformationItem(this)
      else -> null
    }
    if (informationItem === null) {
      return
    }
    adapter.addItem(informationItem, index)
  }

  private suspend fun unifiedSearchSynchronous(): List<RecyclerItem> {
    val allItems = emptyList<RecyclerItem>().toMutableList()
    allItems.addAll(unifiedFolderSearchSynchronous(config)
        .map {
          GlobalScope.async(Dispatchers.IO) {
            var notesCount = -1
            if (config.hasFilter()) {
              val folderConfig = config.copy()
              folderConfig.folders.clear()
              folderConfig.folders.add(it)
              notesCount = unifiedSearchSynchronous(folderConfig).size
              if (notesCount == 0) {
                return@async null
              }
              folderConfig.folders.clear()
            }
            FolderRecyclerItem(
                context = this@MainActivity,
                folder = it,
                click = {
                  config.folders.clear()
                  config.folders.add(it)
                  unifiedSearch()
                  notifyFolderChange()
                },
                longClick = {
                  CreateOrEditFolderBottomSheet.openSheet(this@MainActivity, it, { _, _ -> setupData() })
                },
                selected = config.hasFolder(it),
                contents = notesCount)
          }
        }
        .map { it.await() }
        .filterNotNull())
    allItems.addAll(unifiedSearchSynchronous(config)
        .map { GlobalScope.async(Dispatchers.IO) { NoteRecyclerItem(this@MainActivity, it) } }
        .map { it.await() })
    return allItems
  }

  fun notifyFolderChange() {
    val componentContext = ComponentContext(this)
    lithoPreBottomToolbar.removeAllViews()
    if (config.folders.isEmpty()) {
      return
    }

    val folder = config.folders.first()
    lithoPreBottomToolbar.addView(LithoView.create(componentContext,
        MainActivityFolderBottomBar.create(componentContext)
            .folder(folder)
            .build()))
  }

  fun unifiedSearch() {
    GlobalScope.launch(Dispatchers.Main) {
      val items = GlobalScope.async(Dispatchers.IO) { unifiedSearchSynchronous() }
      handleNewItems(items.await())
    }
  }

  fun openTag(tag: Tag) {
    config.mode = if (config.mode == HomeNavigationState.LOCKED) HomeNavigationState.DEFAULT else config.mode
    config.tags.add(tag)
    unifiedSearch()
    notifyModeChange()
  }

  override fun onResume() {
    super.onResume()
    CoreConfig.instance.startListener(this)
    setupData()
    registerNoteReceiver()
  }

  fun resetAndSetupData() {
    config.clear()
    setupData()
  }

  fun setupData() {
    return when (config.mode) {
      HomeNavigationState.FAVOURITE -> onFavouritesClick()
      HomeNavigationState.ARCHIVED -> onArchivedClick()
      HomeNavigationState.TRASH -> onTrashClick()
      HomeNavigationState.LOCKED -> onLockedClick()
      HomeNavigationState.DEFAULT -> onHomeClick()
      else -> onHomeClick()
    }
  }

  fun setSearchMode(mode: Boolean) {
    isInSearchMode = mode
    searchToolbar.visibility = if (isInSearchMode) View.VISIBLE else View.GONE
    searchBox.setText("")

    if (isInSearchMode) {
      tryOpeningTheKeyboard()
      GlobalScope.launch(Dispatchers.Main) {
        GlobalScope.async(Dispatchers.IO) { tagAndColorPicker.reset() }.await()
        tagAndColorPicker.notifyChanged()
      }
      searchBox.requestFocus()
    } else {
      tryClosingTheKeyboard()
      resetAndSetupData()
    }
  }

  private fun startSearch(keyword: String) {
    GlobalScope.launch(singleThreadDispatcher) {
      config.text = keyword
      val items = GlobalScope.async(Dispatchers.IO) { unifiedSearchSynchronous() }
      GlobalScope.launch(Dispatchers.Main) {
        handleNewItems(items.await())
      }
    }
  }

  override fun onBackPressed() {
    when {
      isInSearchMode && searchBox.text.toString().isBlank() -> setSearchMode(false)
      isInSearchMode -> searchBox.setText("")
      config.hasFilter() -> {
        config.clear()
        onHomeClick()
        notifyFolderChange()
      }
      else -> super.onBackPressed()
    }
  }

  override fun onPause() {
    super.onPause()
    unregisterReceiver(receiver)
  }

  override fun onDestroy() {
    super.onDestroy()
    HouseKeeperJob.schedule()
  }

  override fun onStop() {
    super.onStop()
    if (PermissionUtils().getStoragePermissionManager(this).hasAllPermissions()) {
      NoteExporter().tryAutoExport()
    }
  }

  override fun notifyThemeChange() {
    setSystemTheme()

    val theme = CoreConfig.instance.themeController()
    containerLayoutMain.setBackgroundColor(getThemeColor())

    val toolbarIconColor = theme.get(ThemeColorType.TOOLBAR_ICON)
    deleteTrashIcon.setColorFilter(toolbarIconColor)
    deletesAutomatically.setTextColor(toolbarIconColor)

    setBottomToolbar()
  }

  private fun registerNoteReceiver() {
    receiver = SyncedNoteBroadcastReceiver {
      setupData()
    }
    registerReceiver(receiver, getNoteIntentFilter())
  }

  fun setBottomToolbar() {
    val componentContext = ComponentContext(this)
    lithoBottomToolbar.removeAllViews()
    lithoBottomToolbar.addView(LithoView.create(componentContext,
        MainActivityBottomBar.create(componentContext)
            .colorConfig(ToolbarColorConfig())
            .build()))
  }

  /**
   * Start : INoteOptionSheetActivity Functions
   */
  override fun updateNote(note: Note) {
    note.save(this)
    setupData()
  }

  override fun markItem(note: Note, state: NoteState) {
    note.mark(this, state)
    setupData()
  }

  override fun moveItemToTrashOrDelete(note: Note) {
    snackbar.softUndo(this, note)
    note.softDelete(this)
    setupData()
  }

  override fun notifyTagsChanged(note: Note) {
    setupData()
  }

  override fun getSelectMode(note: Note): String {
    return config.mode.name
  }

  override fun notifyResetOrDismiss() {
    setupData()
  }

  override fun lockedContentIsHidden() = true

  /**
   * End : INoteOptionSheetActivity
   */
}
