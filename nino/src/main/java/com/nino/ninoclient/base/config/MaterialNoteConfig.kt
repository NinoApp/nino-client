package com.nino.ninoclient.base.config

import android.content.Context
import android.support.v7.app.AppCompatActivity
import com.github.bijoysingh.starter.prefs.Store
import com.github.bijoysingh.starter.prefs.VersionedStore
import com.nino.ninoclient.base.config.auth.IAuthenticator
import com.nino.ninoclient.base.config.auth.NullAuthenticator
import com.nino.ninoclient.base.config.remote.IRemoteConfigFetcher
import com.nino.ninoclient.base.config.remote.NullRemoteConfigFetcher
import com.nino.ninoclient.base.core.folder.IFolderActor
import com.nino.ninoclient.base.core.folder.MaterialFolderActor
import com.nino.ninoclient.base.core.note.INoteActor
import com.nino.ninoclient.base.core.note.MaterialNoteActor
import com.nino.ninoclient.base.core.tag.ITagActor
import com.nino.ninoclient.base.core.tag.MaterialTagActor
import com.nino.ninoclient.base.database.FoldersProvider
import com.nino.ninoclient.base.database.NotesProvider
import com.nino.ninoclient.base.database.TagsProvider
import com.nino.ninoclient.base.database.room.AppDatabase
import com.nino.ninoclient.base.database.room.folder.Folder
import com.nino.ninoclient.base.database.room.note.Note
import com.nino.ninoclient.base.database.room.tag.Tag
import com.nino.ninoclient.base.export.remote.FolderRemoteDatabase
import com.nino.ninoclient.base.support.ui.IThemeManager
import com.nino.ninoclient.base.support.ui.ThemeManager
import com.nino.ninoclient.base.support.utils.Flavor
import com.nino.ninoclient.base.support.utils.ImageCache
import java.lang.ref.WeakReference

const val USER_PREFERENCES_STORE_NAME = "USER_PREFERENCES";
const val USER_PREFERENCES_VERSION = 1;

open class MaterialNoteConfig(context: Context) : CoreConfig(context) {
  val db = AppDatabase.createDatabase(context)

  val notesProvider = NotesProvider()
  val tagsProvider = TagsProvider()
  val foldersProvider = FoldersProvider()
  val store = VersionedStore.get(context, USER_PREFERENCES_STORE_NAME, USER_PREFERENCES_VERSION)
  val appTheme = ThemeManager()
  val externalFolderSync = FolderRemoteDatabase(WeakReference(context))
  val imageCache = ImageCache(context)

  override fun database(): AppDatabase = db

  override fun authenticator(): IAuthenticator = NullAuthenticator()

  override fun notesDatabase(): NotesProvider = notesProvider

  override fun tagsDatabase(): TagsProvider = tagsProvider

  override fun noteActions(note: Note): INoteActor = MaterialNoteActor(note)

  override fun tagActions(tag: Tag): ITagActor = MaterialTagActor(tag)

  override fun foldersDatabase(): FoldersProvider = foldersProvider

  override fun folderActions(folder: Folder): IFolderActor = MaterialFolderActor(folder)

  override fun themeController(): IThemeManager = appTheme

  override fun remoteConfigFetcher(): IRemoteConfigFetcher = NullRemoteConfigFetcher()

  override fun startListener(activity: AppCompatActivity) {}

  override fun appFlavor(): Flavor = Flavor.NONE

  override fun store(): Store = store

  override fun externalFolderSync(): FolderRemoteDatabase = externalFolderSync

  override fun imageCache(): ImageCache = imageCache
}