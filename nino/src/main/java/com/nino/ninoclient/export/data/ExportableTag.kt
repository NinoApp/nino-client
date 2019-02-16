package com.nino.ninoclient.export.data

import com.nino.ninoclient.database.room.tag.Tag
import com.nino.ninoclient.core.note.generateUUID
import com.nino.ninoclient.core.tag.ITagContainer
import com.nino.ninoclient.core.tag.TagBuilder
import org.json.JSONObject
import java.io.Serializable

class ExportableTag(
    var uuid: String,
    var title: String
) : Serializable, ITagContainer {

  override fun title(): String = title

  override fun uuid(): String = uuid

  constructor(tag: Tag) : this(
      tag.uuid,
      tag.title
  )

  companion object {
    fun fromJSON(json: JSONObject): ExportableTag {
      val version = if (json.has("version")) json.getInt("version") else 1
      return when (version) {
        1 -> fromJSONObjectV1(json)
        else -> fromJSONObjectV1(json)
      }
    }

    fun fromJSONObjectV1(json: JSONObject): ExportableTag {
      return ExportableTag(
          generateUUID(),
          json["title"] as String)
    }

    fun getBestPossibleTagObject(json: JSONObject): Tag {
      return TagBuilder().copy(fromJSON(json))
    }
  }
}