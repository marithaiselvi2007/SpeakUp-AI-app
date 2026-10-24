package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class PracticeReminderReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_PRACTICE_REMINDER = "com.example.action.PRACTICE_REMINDER"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d("PracticeReminder", "Received broadcast with action: $action")

        val pendingResult = goAsync()

        try {
            // Handle User Tapping DONE / STOP on Notification
            if (action == ScheduleNotificationManager.ACTION_DISMISS_NOTIFICATION) {
                Log.d("PracticeReminder", "DONE / STOP tapped on notification. Dismissing reminder.")
                ScheduleNotificationManager.handleAlarmOrNotificationDismissed(context)
                return
            }

            // Handle Follow-up reminder trigger
            if (action == ScheduleNotificationManager.ACTION_FOLLOW_UP_REMINDER) {
                val hasAttendedPracticeToday = ScheduleNotificationManager.hasUserAttendedPracticeToday(context)
                val isActivelyPracticing = com.example.viewmodel.SpeakingPracticeViewModel.isActivelyPracticing
                if (hasAttendedPracticeToday || isActivelyPracticing) {
                    Log.d("PracticeReminder", "User completed practice before follow-up triggered. Skipping follow-up.")
                    return
                }
                Log.d("PracticeReminder", "Triggering scheduled follow-up reminder...")
                ScheduleNotificationManager.showReminderNotification(context, isFollowUp = true)
                ScheduleNotificationManager.triggerPersistentAlarm(context)
                return
            }

            val prefs = context.getSharedPreferences(ScheduleNotificationManager.PREFS_NAME, Context.MODE_PRIVATE)
            val effectiveUid = com.example.data.remote.FirebaseSafeManager.getAuth(context)?.currentUser?.uid
                ?: prefs.getString(ScheduleNotificationManager.KEY_CURRENT_UID, "guest_user")
                ?: "guest_user"

            val isAlarmEnabled = prefs.getBoolean("${ScheduleNotificationManager.KEY_ALARM_ENABLED}_$effectiveUid",
                prefs.getBoolean(ScheduleNotificationManager.KEY_ALARM_ENABLED, true))
            val practiceDays = prefs.getStringSet("${ScheduleNotificationManager.KEY_PRACTICE_DAYS}_$effectiveUid", null)
                ?: prefs.getStringSet(ScheduleNotificationManager.KEY_PRACTICE_DAYS, null)
                ?: emptySet()
            val preferredTime = prefs.getString("${ScheduleNotificationManager.KEY_PREFERRED_TIME}_$effectiveUid", null)
                ?: prefs.getString(ScheduleNotificationManager.KEY_PREFERRED_TIME, "07:00 PM")
                ?: "07:00 PM"

            if (!isAlarmEnabled) {
                Log.d("PracticeReminder", "Alarm is disabled for user $effectiveUid. Skipping.")
                return
            }

            if (action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_MY_PACKAGE_REPLACED) {
                Log.d("PracticeReminder", "Device boot completed or package replaced. Rescheduling practice alarm for user $effectiveUid...")
                ScheduleNotificationManager.schedulePracticeReminder(
                    context = context,
                    preferredTime = preferredTime,
                    practiceDays = practiceDays.toList(),
                    isEnabled = true,
                    userId = effectiveUid
                )
                return
            }

            // Determine current day of week (e.g. "Monday", "Tuesday", "Wednesday")
            val dayOfWeekFormat = SimpleDateFormat("EEEE", Locale.ENGLISH)
            val currentDayName = dayOfWeekFormat.format(Calendar.getInstance().time)

            Log.d("PracticeReminder", "Checking practice day for $currentDayName against user ($effectiveUid) selected days: $practiceDays")

            // Strictly check if today is in the user's selected practice days
            val isTodaySelected = practiceDays.any {
                it.equals(currentDayName, ignoreCase = true) || it.take(3).equals(currentDayName.take(3), ignoreCase = true)
            }

            if (isTodaySelected) {
                val isActivelyPracticing = com.example.viewmodel.SpeakingPracticeViewModel.isActivelyPracticing
                val hasAttendedPracticeToday = ScheduleNotificationManager.hasUserAttendedPracticeToday(context, effectiveUid)

                if (isActivelyPracticing) {
                    Log.d("PracticeReminder", "User is currently inside an active practice session. Skipping alarm reminder.")
                } else if (hasAttendedPracticeToday) {
                    Log.d("PracticeReminder", "User ($effectiveUid) has already completed practice today ($currentDayName). Skipping reminder alarm.")
                } else {
                    Log.d("PracticeReminder", "Today ($currentDayName) is scheduled AND user ($effectiveUid) has NOT completed practice today. Triggering notification and voice alarm.")
                    // 1. Post separate normal Android notification independently with DONE / STOP action
                    try {
                        ScheduleNotificationManager.showReminderNotification(context, isFollowUp = false)
                    } catch (e: Exception) {
                        Log.e("PracticeReminder", "Error showing reminder notification", e)
                    }

                    // 2. Trigger real Android-native alarm service (audible ring + TTS speech)
                    try {
                        val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
                        val wl = pm?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SpeakUp:PracticeReminderReceiverWL")
                        wl?.acquire(60000L) // 1 minute
                    } catch (e: Exception) {
                        Log.e("PracticeReminder", "WakeLock error in receiver", e)
                    }
                    ScheduleNotificationManager.triggerPersistentAlarm(context)
                }
            } else {
                Log.d("PracticeReminder", "Today ($currentDayName) is NOT in user's selected practice days ($practiceDays). Skipping alarm.")
            }

            // Reschedule for the next day's alarm cycle to repeat automatically
            ScheduleNotificationManager.schedulePracticeReminder(
                context = context,
                preferredTime = preferredTime,
                practiceDays = practiceDays.toList(),
                isEnabled = true,
                userId = effectiveUid
            )
        } catch (e: Exception) {
            Log.e("PracticeReminder", "Error in PracticeReminderReceiver", e)
        } finally {
            pendingResult.finish()
        }
    }
}

