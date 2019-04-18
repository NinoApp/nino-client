package com.maubis.scarlet.base.core.format

import com.maubis.markdown.segmenter.TextSegmenter
import com.maubis.scarlet.base.note.toFormat
import com.maubis.scarlet.base.note.toRawFormat
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class FormatBuilder {
  val KEY_NOTE = "note"

  @Throws(JSONException::class)
  fun fromJson(json: JSONObject): Format {
    val format = Format()
    format.formatType = FormatType.valueOf(json.getString("format"))
    format.text = json.getString("text")
    return format
  }

  fun getDescription(formats: List<Format>): String {
    val array = JSONArray()
    for (format in formats) {
      val json = format.toJson()
      if (json != null) array.put(json)
    }

    val cache = HashMap<String, Any>()
    cache[KEY_NOTE] = array
    return JSONObject(cache).toString()
  }

  fun getSmarterDescription(formats: List<Format>): String {
    val extractedFormats = emptyList<Format>().toMutableList()
    for (format in formats) {
      if (format.formatType != FormatType.TEXT) {
        extractedFormats.add(format)
        continue
      }

      val moreFormats = TextSegmenter(format.text).get().map { it.toRawFormat() }
      var lastFormat: Format? = null
      for (rawFormat in moreFormats) {
        val isCheckedType = (rawFormat.formatType == FormatType.CHECKLIST_UNCHECKED || rawFormat.formatType == FormatType.CHECKLIST_CHECKED)
        if (lastFormat != null && !isCheckedType) {
          lastFormat.text += "\n"
          lastFormat.text += rawFormat.text
          continue
        }

        if (!isCheckedType) {
          rawFormat.formatType = FormatType.TEXT
          lastFormat = rawFormat
          continue
        }

        if (lastFormat != null) {
          extractedFormats.add(lastFormat)
          lastFormat = null
        }

        rawFormat.text = rawFormat.text
            .removePrefix("[] ")
            .removePrefix("[x] ")
            .removePrefix("[ ] ")
            .removePrefix("[X] ")
        extractedFormats.add(rawFormat)
      }
      if (lastFormat !== null) {
        extractedFormats.add(lastFormat)
      }

    }
    return getDescription(extractedFormats)
  }
  fun getFormats(note: String): List<Format> {
    val formats = ArrayList<Format>()
    try {
      val json = JSONObject(note)
      val array = json.getJSONArray(KEY_NOTE)
      for (index in 0 until array.length()) {
        try {
          val format = fromJson(array.getJSONObject(index))
          format.uid = formats.size
          formats.add(format)
        } catch (innerException: JSONException) {
        }
      }
    } catch (exception: Exception) {
    }
    return formats
  }

  fun getNextFormatType(type: FormatType): FormatType {
    when (type) {
      FormatType.NUMBERED_LIST -> return FormatType.NUMBERED_LIST
      FormatType.HEADING -> return FormatType.SUB_HEADING
      FormatType.CHECKLIST_CHECKED, FormatType.CHECKLIST_UNCHECKED -> return FormatType.CHECKLIST_UNCHECKED
      FormatType.IMAGE, FormatType.SMART_NOTE, FormatType.SUB_HEADING, FormatType.CODE, FormatType.QUOTE, FormatType.TEXT -> return FormatType.TEXT
      else -> return FormatType.TEXT
    }
  }
}
