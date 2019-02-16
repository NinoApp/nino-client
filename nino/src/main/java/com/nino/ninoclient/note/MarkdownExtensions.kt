package com.nino.ninoclient.note

import com.maubis.markdown.segmenter.MarkdownSegment
import com.maubis.markdown.segmenter.MarkdownSegmentType
import com.nino.ninoclient.core.format.Format
import com.nino.ninoclient.core.format.FormatType

fun MarkdownSegment.toFormat(): Format {
  return Format(type().toFormatType(), strip())
}

fun MarkdownSegmentType.toFormatType(): FormatType {
  return when (this) {
    MarkdownSegmentType.INVALID -> FormatType.EMPTY
    MarkdownSegmentType.HEADING_1 -> FormatType.HEADING
    MarkdownSegmentType.HEADING_2 -> FormatType.SUB_HEADING
    MarkdownSegmentType.HEADING_3 -> FormatType.HEADING_3
    MarkdownSegmentType.NORMAL -> FormatType.TEXT
    MarkdownSegmentType.CODE -> FormatType.CODE
    MarkdownSegmentType.BULLET_1 -> FormatType.BULLET_1
    MarkdownSegmentType.BULLET_2 -> FormatType.BULLET_2
    MarkdownSegmentType.BULLET_3 -> FormatType.BULLET_3
    MarkdownSegmentType.QUOTE -> FormatType.QUOTE
    MarkdownSegmentType.SEPARATOR -> FormatType.SEPARATOR
    MarkdownSegmentType.CHECKLIST_UNCHECKED -> FormatType.CHECKLIST_UNCHECKED
    MarkdownSegmentType.CHECKLIST_CHECKED -> FormatType.CHECKLIST_CHECKED
  }
}