package com.nino.ninoclient.config

import android.content.Context
import android.graphics.Typeface
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.app.AppCompatActivity
import com.github.ajalt.reprint.core.Reprint
import com.github.bijoysingh.starter.prefs.Store
import com.maubis.markdown.MarkdownConfig.Companion.config
import com.nino.ninoclient.R
import com.nino.ninoclient.config.auth.IAuthenticator
import com.nino.ninoclient.config.remote.IRemoteConfigFetcher
import com.nino.ninoclient.core.folder.IFolderActor
import com.nino.ninoclient.core.note.INoteActor
import com.nino.ninoclient.core.tag.ITagActor
import com.nino.ninoclient.database.FoldersProvider
import com.nino.ninoclient.database.NotesProvider
import com.nino.ninoclient.database.TagsProvider
import com.nino.ninoclient.database.room.AppDatabase
import com.nino.ninoclient.database.room.folder.Folder
import com.nino.ninoclient.database.room.note.Note
import com.nino.ninoclient.database.room.tag.Tag
import com.nino.ninoclient.export.remote.FolderRemoteDatabase
import com.nino.ninoclient.support.ui.IThemeManager
import com.nino.ninoclient.support.utils.Flavor
import com.nino.ninoclient.support.utils.ImageCache

abstract class CoreConfig(context: Context) {

  init {
    Reprint.initialize(context)
    config.spanConfig.headingTypeface = ResourcesCompat.getFont(context, R.font.monserrat) ?: Typeface.DEFAULT
    FONT_MONSERRAT = config.spanConfig.headingTypeface
    FONT_OPEN_SANS = ResourcesCompat.getFont(context, R.font.open_sans) ?: Typeface.DEFAULT
  }

  abstract fun database(): AppDatabase

  abstract fun authenticator(): IAuthenticator

  abstract fun notesDatabase(): NotesProvider

  abstract fun tagsDatabase(): TagsProvider

  abstract fun foldersDatabase(): FoldersProvider

  abstract fun noteActions(note: Note): INoteActor

  abstract fun tagActions(tag: Tag): ITagActor

  abstract fun folderActions(folder: Folder): IFolderActor

  abstract fun themeController(): IThemeManager

  abstract fun remoteConfigFetcher(): IRemoteConfigFetcher

  abstract fun startListener(activity: AppCompatActivity)

  abstract fun appFlavor(): Flavor

  abstract fun store(): Store

  abstract fun externalFolderSync(): FolderRemoteDatabase

  abstract fun imageCache(): ImageCache

  companion object {
    lateinit var instance: CoreConfig
    val notesDb get() = instance.notesDatabase()
    val tagsDb get() = instance.tagsDatabase()
    val foldersDb get() = instance.foldersDatabase()

    var FONT_MONSERRAT: Typeface = Typeface.DEFAULT
    var FONT_OPEN_SANS: Typeface = Typeface.DEFAULT
  }
}