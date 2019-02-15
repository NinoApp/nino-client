package com.example.batu.ninoclient.config

import android.content.Context
import android.support.v7.app.AppCompatActivity
import com.github.bijoysingh.starter.prefs.Store
import com.github.bijoysingh.starter.prefs.VersionedStore
import com.example.batu.ninoclient.config.auth.IAuthenticator
import com.example.batu.ninoclient.config.auth.NullAuthenticator
import com.example.batu.ninoclient.config.remote.IRemoteConfigFetcher
import com.example.batu.ninoclient.config.remote.NullRemoteConfigFetcher
import com.example.batu.ninoclient.core.folder.IFolderActor
import com.example.batu.ninoclient.core.folder.MaterialFolderActor
import com.example.batu.ninoclient.core.note.INoteActor
import com.example.batu.ninoclient.core.note.MaterialNoteActor
import com.example.batu.ninoclient.core.tag.ITagActor
import com.example.batu.ninoclient.core.tag.MaterialTagActor
import com.example.batu.ninoclient.database.FoldersProvider
import com.example.batu.ninoclient.database.NotesProvider
import com.example.batu.ninoclient.database.TagsProvider
import com.example.batu.ninoclient.database.room.AppDatabase
import com.example.batu.ninoclient.database.room.folder.Folder
import com.example.batu.ninoclient.database.room.note.Note
import com.example.batu.ninoclient.database.room.tag.Tag
import com.example.batu.ninoclient.export.remote.FolderRemoteDatabase
import com.example.batu.ninoclient.export.support.ExternalFolderSync
import com.example.batu.ninoclient.support.ui.IThemeManager
import com.example.batu.ninoclient.support.ui.ThemeManager
import com.example.batu.ninoclient.support.utils.Flavor
import com.example.batu.ninoclient.support.utils.ImageCache
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