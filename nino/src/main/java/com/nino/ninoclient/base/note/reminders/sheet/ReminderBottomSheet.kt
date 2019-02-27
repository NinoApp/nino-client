package com.nino.ninoclient.base.note.reminders.sheet

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.view.View.GONE
import android.widget.TextView
import com.github.bijoysingh.starter.util.DateFormatter
import com.github.bijoysingh.uibasics.views.UIActionView
import com.nino.ninoclient.R
import com.nino.ninoclient.base.config.CoreConfig
import com.nino.ninoclient.base.database.room.note.Note
import com.nino.ninoclient.base.core.note.*
import com.nino.ninoclient.base.main.sheets.GenericOptionsBottomSheet
import com.nino.ninoclient.base.note.reminders.ReminderJob
import com.nino.ninoclient.base.note.saveWithoutSync
import com.nino.ninoclient.base.support.option.SimpleOptionsItem
import com.nino.ninoclient.base.support.ui.ThemeColorType
import com.nino.ninoclient.base.support.ui.ThemedActivity
import com.nino.ninoclient.base.support.ui.ThemedBottomSheetFragment
import java.util.*


class ReminderBottomSheet : ThemedBottomSheetFragment() {

  var selectedNote: Note? = null
  var reminder: Reminder = Reminder(
      0,
      Calendar.getInstance().timeInMillis,
      ReminderInterval.ONCE)

  override fun getBackgroundView(): Int {
    return R.id.container_layout
  }

  override fun setupView(dialog: Dialog?) {
    super.setupView(dialog)
    if (dialog == null) {
      return
    }

    val note = selectedNote
    if (note === null) {
      return
    }

    val calendar = Calendar.getInstance()
    reminder = note.getReminderV2() ?: Reminder(
        0,
        calendar.timeInMillis,
        ReminderInterval.ONCE)

    val isNewReminder = reminder.uid == 0
    if (isNewReminder) {
      calendar.set(Calendar.HOUR_OF_DAY, 8)
      calendar.set(Calendar.MINUTE, 0)
      calendar.set(Calendar.SECOND, 0)
      if (Calendar.getInstance().after(calendar)) {
        calendar.add(Calendar.HOUR_OF_DAY, 24)
      }
      reminder.timestamp = calendar.timeInMillis
    }
    setColors()
    setContent(reminder)
    setListeners(note, isNewReminder)
    makeBackgroundTransparent(dialog, R.id.root_layout)
  }

  fun setListeners(note: Note, isNewReminder: Boolean) {
    val reminderDate = dialog.findViewById<UIActionView>(R.id.reminder_date)
    val reminderTime = dialog.findViewById<UIActionView>(R.id.reminder_time)
    val reminderRepeat = dialog.findViewById<UIActionView>(R.id.reminder_repeat)

    reminderDate.setOnClickListener {
      if (reminder.interval == ReminderInterval.ONCE) {
        openDatePickerDialog()
      }
    }
    reminderTime.setOnClickListener {
      openTimePickerDialog()
    }
    reminderRepeat.setOnClickListener {
      openFrequencyDialog()
    }

    val removeAlarm = dialog.findViewById<TextView>(R.id.remove_alarm)
    val setAlarm = dialog.findViewById<TextView>(R.id.set_alarm)
    if (isNewReminder) {
      removeAlarm.visibility = GONE
    }
    removeAlarm.setOnClickListener {
      ReminderJob.cancelJob(reminder.uid)

      note.meta = ""
      note.saveWithoutSync(themedContext())

      dismiss()
    }
    setAlarm.setOnClickListener {
      if (Calendar.getInstance().after(reminder.toCalendar())) {
        dismiss()
        return@setOnClickListener
      }

      val uid = ReminderJob.scheduleJob(note.uuid, reminder)
      if (uid == -1) {
        dismiss()
        return@setOnClickListener
      }

      reminder.uid = uid

      note.setReminderV2(reminder)
      note.saveWithoutSync(themedContext())

      dismiss()
    }
  }

  fun getReminderIntervalLabel(interval: ReminderInterval): Int {
    return when (interval) {
      ReminderInterval.ONCE -> R.string.reminder_frequency_once
      ReminderInterval.DAILY -> R.string.reminder_frequency_daily
    }
  }

  fun openFrequencyDialog() {
    val isSelected = fun(interval: ReminderInterval): Boolean = interval == reminder!!.interval
    GenericOptionsBottomSheet.openSheet(
        themedActivity() as ThemedActivity,
        getString(R.string.reminder_sheet_repeat),
        arrayListOf(
            SimpleOptionsItem(
                title = getReminderIntervalLabel(ReminderInterval.ONCE),
                listener = {
                  reminder.interval = ReminderInterval.ONCE
                  setContent(reminder)
                },
                selected = isSelected(ReminderInterval.ONCE)
            ),
            SimpleOptionsItem(
                title = getReminderIntervalLabel(ReminderInterval.DAILY),
                listener = {
                  reminder.interval = ReminderInterval.DAILY
                  setContent(reminder)
                },
                selected = isSelected(ReminderInterval.DAILY)
            )
        ))
  }

  fun openDatePickerDialog() {
    val calendar = reminder.toCalendar()
    val dialog = DatePickerDialog(
        themedContext(),
        R.style.DialogTheme,
        DatePickerDialog.OnDateSetListener { _, year, month, day ->
          calendar.set(Calendar.YEAR, year)
          calendar.set(Calendar.MONTH, month)
          calendar.set(Calendar.DAY_OF_MONTH, day)
          reminder.timestamp = calendar.timeInMillis
          setContent(reminder)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH))
    dialog.show()
  }

  fun openTimePickerDialog() {
    val calendar = reminder.toCalendar()
    val dialog = TimePickerDialog(
        themedContext(),
        R.style.DialogTheme,
        TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
          calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
          calendar.set(Calendar.MINUTE, minute)
          calendar.set(Calendar.SECOND, 0)
          reminder.timestamp = calendar.timeInMillis
          setContent(reminder)
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        false)
    dialog.show()
  }

  fun setContent(reminder: Reminder) {
    val reminderDate = dialog.findViewById<UIActionView>(R.id.reminder_date)
    val reminderTime = dialog.findViewById<UIActionView>(R.id.reminder_time)
    val reminderRepeat = dialog.findViewById<UIActionView>(R.id.reminder_repeat)

    val date = Date(reminder.timestamp)
    reminderRepeat.setSubtitle(getReminderIntervalLabel(reminder.interval))
    reminderTime.setSubtitle(DateFormatter.getDate(DateFormatter.Formats.HH_MM_A.format, date))
    reminderDate.setSubtitle(DateFormatter.getDate(DateFormatter.Formats.DD_MMM_YYYY.format, date))
    reminderDate.alpha = if (reminder.interval == ReminderInterval.ONCE) 1.0f else 0.5f
  }

  fun setColors() {
    val reminderDate = dialog.findViewById<UIActionView>(R.id.reminder_date)
    val reminderTime = dialog.findViewById<UIActionView>(R.id.reminder_time)
    val reminderRepeat = dialog.findViewById<UIActionView>(R.id.reminder_repeat)

    val iconColor = CoreConfig.instance.themeController().get(ThemeColorType.TOOLBAR_ICON)
    val textColor = CoreConfig.instance.themeController().get(ThemeColorType.TERTIARY_TEXT)
    val titleColor = CoreConfig.instance.themeController().get(ThemeColorType.SECTION_HEADER)

    reminderDate.setTitleColor(titleColor)
    reminderDate.setSubtitleColor(textColor)
    reminderDate.setImageTint(iconColor)
    reminderDate.setActionTint(iconColor)

    reminderTime.setTitleColor(titleColor)
    reminderTime.setSubtitleColor(textColor)
    reminderTime.setImageTint(iconColor)
    reminderTime.setActionTint(iconColor)

    reminderRepeat.setTitleColor(titleColor)
    reminderRepeat.setSubtitleColor(textColor)
    reminderRepeat.setImageTint(iconColor)
    reminderRepeat.setActionTint(iconColor)
  }

  override fun getLayout(): Int = R.layout.bottom_sheet_reminder

  override fun getBackgroundCardViewIds(): Array<Int> = arrayOf(R.id.card_layout)

  companion object {
    fun openSheet(activity: ThemedActivity, note: Note) {
      val sheet = ReminderBottomSheet()
      sheet.selectedNote = note
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}