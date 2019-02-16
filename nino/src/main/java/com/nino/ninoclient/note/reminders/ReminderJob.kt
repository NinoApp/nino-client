package com.nino.ninoclient.note.reminders

import com.evernote.android.job.Job
import com.evernote.android.job.JobManager
import com.evernote.android.job.JobRequest
import com.evernote.android.job.util.support.PersistableBundleCompat
import com.nino.ninoclient.core.note.Reminder
import com.nino.ninoclient.core.note.ReminderInterval
import com.nino.ninoclient.core.note.getReminderV2
import com.nino.ninoclient.core.note.setReminderV2
import com.nino.ninoclient.note.saveWithoutSync
import com.nino.ninoclient.notification.NotificationConfig
import com.nino.ninoclient.notification.NotificationHandler
import com.nino.ninoclient.notification.REMINDER_NOTIFICATION_CHANNEL_ID
import com.nino.ninoclient.config.CoreConfig.Companion.notesDb
import java.util.*
import java.util.concurrent.TimeUnit


class ReminderJob : Job() {

  override fun onRunJob(params: Params): Job.Result {
    val noteUUID = params.extras.getString(EXTRA_KEY_NOTE_UUID, "")
    val note = notesDb.getByUUID(noteUUID)
    if (note === null) {
      return Job.Result.SUCCESS
    }

    val handler = NotificationHandler(context)
    handler.openNotification(NotificationConfig(note, REMINDER_NOTIFICATION_CHANNEL_ID))

    try {
      val reminder = note.getReminderV2()
      if (reminder?.interval == ReminderInterval.DAILY) {
        val reminderV2 = Reminder(
            0,
            nextJobTimestamp(reminder.timestamp, System.currentTimeMillis()),
            ReminderInterval.DAILY)
        reminderV2.uid = scheduleJob(note.uuid, reminderV2)
        note.setReminderV2(reminderV2)
        note.saveWithoutSync(context)
      } else {
        note.meta = ""
        note.saveWithoutSync(context)
      }
    } catch (e: Exception) {
    }

    return Job.Result.SUCCESS
  }

  companion object {
    val TAG = "reminder_job"
    val EXTRA_KEY_NOTE_UUID = "note_uuid"

    fun scheduleJob(noteUuid: String, reminder: Reminder): Int {
      val extras = PersistableBundleCompat()
      extras.putString(EXTRA_KEY_NOTE_UUID, noteUuid)

      var deltaTime = reminder.timestamp - Calendar.getInstance().timeInMillis
      if (reminder.interval == ReminderInterval.DAILY && deltaTime > TimeUnit.DAYS.toMillis(1)) {
        deltaTime = deltaTime % TimeUnit.DAYS.toMillis(1)
      }

      return JobRequest.Builder(ReminderJob.TAG)
          .setExact(deltaTime)
          .setExtras(extras)
          .build()
          .schedule()
    }

    fun nextJobTimestamp(timestamp: Long, currentTimestamp: Long): Long {
      return when {
        timestamp > currentTimestamp -> timestamp
        else -> {
          var tempTimestamp = timestamp
          while (tempTimestamp <= currentTimestamp) {
            tempTimestamp += TimeUnit.DAYS.toMillis(1)
          }
          tempTimestamp
        }
      }
    }

    fun cancelJob(uid: Int) {
      JobManager.instance().cancel(uid);
    }
  }
}