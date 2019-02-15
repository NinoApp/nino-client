package com.example.batu.ninoclient.export.recycler

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.TextView
import com.github.bijoysingh.starter.recyclerview.RecyclerViewHolder
import com.github.bijoysingh.starter.util.DateFormatter
import com.github.bijoysingh.starter.util.LocaleManager
import com.example.batu.ninoclient.R
import com.example.batu.ninoclient.config.CoreConfig
import com.example.batu.ninoclient.export.activity.ImportNoteActivity
import com.example.batu.ninoclient.support.recycler.RecyclerItem
import com.example.batu.ninoclient.support.ui.ThemeColorType
import java.io.File

class FileImportViewHolder(context: Context, root: View)
  : RecyclerViewHolder<RecyclerItem>(context, root) {

  private val fileName: TextView = findViewById(R.id.file_name)
  private val filePath: TextView = findViewById(R.id.file_path)
  private val fileDate: TextView = findViewById(R.id.file_date)
  private val fileSize: TextView = findViewById(R.id.file_size)

  init {
    val theme = CoreConfig.instance.themeController()
    fileName.setTextColor(theme.get(ThemeColorType.SECONDARY_TEXT))
    filePath.setTextColor(theme.get(ThemeColorType.HINT_TEXT))
    fileDate.setTextColor(theme.get(ThemeColorType.TERTIARY_TEXT))
    fileSize.setTextColor(theme.get(ThemeColorType.TERTIARY_TEXT))
  }

  override fun populate(data: RecyclerItem, extra: Bundle?) {
    val item = data as FileRecyclerItem
    fileName.text = item.name
    filePath.text = getPath(item)
    fileDate.text = getSubtitleText(item.file)
    fileSize.text = getMetaText(item.file)

    root.setOnClickListener {
      (context as ImportNoteActivity).select(item)
    }
    root.setBackgroundColor(
        if (item.selected) CoreConfig.instance.themeController().get(
            context, R.color.material_grey_100, R.color.dark_hint_text) else Color.TRANSPARENT)
  }

  private fun getPath(item: FileRecyclerItem): String {
    var path = item.path.removePrefix(Environment.getExternalStorageDirectory().absolutePath)
    path = path.removeSuffix(item.name)
    return path
  }

  private fun getSubtitleText(file: File): String {
    return DateFormatter.getDate("dd MMM yy \u00B7 hh:mm a", file.lastModified())
  }

  private fun getMetaText(file: File): String {
    var stringResource = R.string.file_size_kb
    var fileSize = file.length() / 1024.0
    if (fileSize > 1024) {
      fileSize = fileSize / 1024.0
      stringResource = R.string.file_size_mb
    }
    if (fileSize > 1024) {
      fileSize = fileSize / 1024.0
      stringResource = R.string.file_size_gb
    }
    return context.getString(stringResource, LocaleManager.toString(fileSize, 2))
  }
}