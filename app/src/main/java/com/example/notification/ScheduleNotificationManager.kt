package com.example.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object ScheduleNotificationManager {
    const val CHANNEL_ID = "practice_reminder_channel"
    const val CHANNEL_NAME = "Practice Reminders"
    const val NOTIFICATION_ID = 1001
    const val ALARM_REQUEST_CODE = 2001
    const val FOLLOW_UP_ALARM_REQUEST_CODE = 2002
    const val PREFS_NAME = "speakup_schedule_prefs"
    const val KEY_ALARM_ENABLED = "alarm_enabled"
    const val KEY_PREFERRED_TIME = "preferred_time"
    const val KEY_PRACTICE_DAYS = "practice_days"
    const val KEY_CURRENT_UID = "current_uid"
    const val KEY_LAST_PRACTICE_DATE = "last_practice_date"
    const val KEY_LAST_PRACTICE_TIMESTAMP = "last_practice_timestamp"
    const val KEY_FOLLOW_UP_SCHEDULED_DATE = "follow_up_scheduled_date"

    const val ACTION_DISMISS_NOTIFICATION = "com.example.notification.ACTION_DISMISS_NOTIFICATION"
    const val ACTION_FOLLOW_UP_REMINDER = "com.example.action.FOLLOW_UP_REMINDER"

    val ALL_DAYS = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

    const val PRACTICE_REMINDER_MESSAGE = "It’s time for your English speaking class. Please come and join your class."

    private var textToSpeechInstance: TextToSpeech? = null

    /**
     * Records that the user has completed a Speaking Practice session today.
     * Also cancels any active alarm reminder immediately and any pending follow-up reminder for today.
     */
    fun markPracticeSessionAttended(context: Context, userId: String? = null) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Calendar.getInstance().time)
            val now = System.currentTimeMillis()
            val effectiveUid = userId?.trim()?.ifBlank { null }
                ?: com.example.data.remote.FirebaseSafeManager.getAuth(context)?.currentUser?.uid
                ?: prefs.getString(KEY_CURRENT_UID, "guest_user")
                ?: "guest_user"

            prefs.edit()
                .putString(KEY_LAST_PRACTICE_DATE, todayStr)
                .putLong(KEY_LAST_PRACTICE_TIMESTAMP, now)
                .putString("${KEY_LAST_PRACTICE_DATE}_$effectiveUid", todayStr)
                .putLong("${KEY_LAST_PRACTICE_TIMESTAMP}_$effectiveUid", now)
                .apply()
            Log.d("ScheduleNotif", "Practice completed recorded for user $effectiveUid on date: $todayStr")
            stopActiveAlarm(context)
            cancelFollowUpReminder(context)
        } catch (e: Exception) {
            Log.e("ScheduleNotif", "Error marking practice attended", e)
        }
    }

    /**
     * Checks whether the user has already completed a scheduled practice session today.
     */
    fun hasUserAttendedPracticeToday(context: Context, userId: String? = null): Boolean {
        // 1. Check if user is currently actively practicing
        if (com.example.viewmodel.SpeakingPracticeViewModel.isActivelyPracticing) {
            return true
        }

        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Calendar.getInstance().time)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val effectiveUid = userId?.trim()?.ifBlank { null }
            ?: com.example.data.remote.FirebaseSafeManager.getAuth(context)?.currentUser?.uid
            ?: prefs.getString(KEY_CURRENT_UID, "guest_user")
            ?: "guest_user"

        // 2. Check SharedPreferences recorded practice date for this user
        val userLastDate = prefs.getString("${KEY_LAST_PRACTICE_DATE}_$effectiveUid", "")
        if (userLastDate == todayStr) {
            return true
        }

        val globalLastDate = prefs.getString(KEY_LAST_PRACTICE_DATE, "")
        val lastSavedUid = prefs.getString(KEY_CURRENT_UID, "")
        if (lastSavedUid == effectiveUid && globalLastDate == todayStr) {
            return true
        }

        val startOfToday = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val lastTimestamp = prefs.getLong("${KEY_LAST_PRACTICE_TIMESTAMP}_$effectiveUid", 0L)
        if (lastTimestamp >= startOfToday) {
            return true
        }

        // 3. Check Day Challenges completion prefs for this user
        try {
            val dayPrefs = context.getSharedPreferences("speakup_day_challenges_prefs", Context.MODE_PRIVATE)
            val challengeDate = dayPrefs.getString("last_challenge_date_$effectiveUid", "")
            if (challengeDate == todayStr) {
                return true
            }
        } catch (e: Exception) {
            Log.e("ScheduleNotif", "Error checking day challenges completion", e)
        }

        // 4. Query Room DB for any sessions recorded today for this user
        return try {
            val db = com.example.data.local.AppDatabase.getDatabase(context)
            val count = kotlinx.coroutines.runBlocking(kotlinx.coroutines.Dispatchers.IO) {
                db.sessionDao().getSessionsCountSinceForUser(effectiveUid, startOfToday)
            }
            count > 0
        } catch (e: Exception) {
            Log.e("ScheduleNotif", "Error checking db for attended practice today", e)
            false
        }
    }

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Daily English Speaking Practice Reminders"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 400, 200, 400)
                setShowBadge(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(channel)
        }
    }

    /**
     * Parses time string such as "07:00 PM", "7:00 PM", "12:00 AM", "12:30 PM", "19:00"
     * into Pair(hourOfDay in 24h format 0..23, minute 0..59)
     */
    fun parseTimeToHourMinute(timeStr: String): Pair<Int, Int> {
        return try {
            val trimmed = timeStr.trim()
            val isPm = trimmed.endsWith("PM", ignoreCase = true)
            val isAm = trimmed.endsWith("AM", ignoreCase = true)
            val cleanTime = trimmed
                .replace("AM", "", ignoreCase = true)
                .replace("PM", "", ignoreCase = true)
                .trim()
            val parts = cleanTime.split(":")
            var hour = parts[0].trim().toInt()
            val minute = if (parts.size > 1) parts[1].trim().toInt() else 0

            if (isPm) {
                if (hour < 12) hour += 12
            } else if (isAm) {
                if (hour == 12) hour = 0
            }
            Pair(hour.coerceIn(0, 23), minute.coerceIn(0, 59))
        } catch (e: Exception) {
            Log.e("ScheduleNotif", "Error parsing time '$timeStr', using default 19:00", e)
            Pair(19, 0) // Default 07:00 PM
        }
    }

    fun isAlarmEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_ALARM_ENABLED, true)
    }

    /**
     * Schedules the exact daily practice alarm using Android AlarmManager.
     * Uses AlarmManager.setAlarmClock for absolute guaranteed on-time wakeup.
     */
    fun schedulePracticeReminder(
        context: Context,
        preferredTime: String,
        practiceDays: List<String>? = null,
        isEnabled: Boolean = true,
        userId: String? = null
    ) {
        val effectiveUid = userId?.trim()?.ifBlank { null }
            ?: com.example.data.remote.FirebaseSafeManager.getAuth(context)?.currentUser?.uid
            ?: "guest_user"

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedDaysForUser = prefs.getStringSet("${KEY_PRACTICE_DAYS}_$effectiveUid", null)
            ?: prefs.getStringSet(KEY_PRACTICE_DAYS, null)

        val resolvedDays: List<String> = when {
            practiceDays != null -> practiceDays.map { com.example.data.repository.SpeakUpRepository.normalizeDayName(it) }.distinct()
            savedDaysForUser != null && savedDaysForUser.isNotEmpty() -> savedDaysForUser.map { com.example.data.repository.SpeakUpRepository.normalizeDayName(it) }.distinct()
            else -> emptyList()
        }

        val editor = prefs.edit()
            .putString(KEY_CURRENT_UID, effectiveUid)
            .putString(KEY_PREFERRED_TIME, preferredTime)
            .putBoolean(KEY_ALARM_ENABLED, isEnabled)
            .putString("${KEY_PREFERRED_TIME}_$effectiveUid", preferredTime)
            .putBoolean("${KEY_ALARM_ENABLED}_$effectiveUid", isEnabled)

        if (resolvedDays.isNotEmpty()) {
            editor.putStringSet(KEY_PRACTICE_DAYS, resolvedDays.toSet())
            editor.putStringSet("${KEY_PRACTICE_DAYS}_$effectiveUid", resolvedDays.toSet())
        }
        editor.apply()

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val intent = Intent(context, PracticeReminderReceiver::class.java).apply {
            action = PracticeReminderReceiver.ACTION_PRACTICE_REMINDER
            flags = Intent.FLAG_RECEIVER_FOREGROUND
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (!isEnabled) {
            try {
                alarmManager.cancel(pendingIntent)
                Log.d("ScheduleNotif", "Practice alarm turned OFF and cancelled.")
            } catch (e: Exception) {
                Log.e("ScheduleNotif", "Error cancelling alarm", e)
            }
            return
        }

        createNotificationChannel(context)
        val (targetHour, targetMinute) = parseTimeToHourMinute(preferredTime)

        // Calculate next trigger time
        val now = Calendar.getInstance()
        val nextTrigger = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, targetHour)
            set(Calendar.MINUTE, targetMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= now.timeInMillis) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val showIntent = Intent(context, MainActivity::class.java).apply {
            action = PracticeAlarmForegroundService.ACTION_START_PRACTICE
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val showPendingIntent = PendingIntent.getActivity(
            context,
            ALARM_REQUEST_CODE + 10,
            showIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val clockInfo = AlarmManager.AlarmClockInfo(nextTrigger.timeInMillis, showPendingIntent)
                alarmManager.setAlarmClock(clockInfo, pendingIntent)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    nextTrigger.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    nextTrigger.timeInMillis,
                    pendingIntent
                )
            }
            Log.d("ScheduleNotif", "Practice reminder alarm scheduled for ${nextTrigger.time} (Alarm ON)")
        } catch (e: SecurityException) {
            Log.w("ScheduleNotif", "Exact alarm permission restricted, falling back: ${e.message}")
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        nextTrigger.timeInMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        nextTrigger.timeInMillis,
                        pendingIntent
                    )
                }
            } catch (e2: Exception) {
                Log.e("ScheduleNotif", "Error in fallback alarm scheduling", e2)
            }
        } catch (e: Exception) {
            Log.e("ScheduleNotif", "Error scheduling reminder alarm", e)
        }
    }

    fun cancelPracticeReminder(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_ALARM_ENABLED, false).apply()

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, PracticeReminderReceiver::class.java).apply {
            action = PracticeReminderReceiver.ACTION_PRACTICE_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        try {
            alarmManager.cancel(pendingIntent)
            cancelFollowUpReminder(context)
            stopActiveAlarm(context)
            Log.d("ScheduleNotif", "Practice reminder alarm cancelled.")
        } catch (e: Exception) {
            Log.e("ScheduleNotif", "Error cancelling alarm", e)
        }
    }

    /**
     * Speaks aloud immediately via Android Text-to-Speech:
     * "Your class time has arrived. Please come and join your class."
     */
    fun triggerAlarmAudioAndSpeech(context: Context) {
        try {
            val appContext = context.applicationContext
            textToSpeechInstance?.stop()
            textToSpeechInstance?.shutdown()

            textToSpeechInstance = TextToSpeech(appContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    try {
                        textToSpeechInstance?.language = Locale.ENGLISH
                        textToSpeechInstance?.setSpeechRate(0.95f)
                        textToSpeechInstance?.setPitch(1.0f)
                        textToSpeechInstance?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                            override fun onStart(utteranceId: String?) {
                                Log.d("ScheduleNotif", "TTS practice alarm voice started")
                            }
                            override fun onDone(utteranceId: String?) {
                                Log.d("ScheduleNotif", "TTS practice alarm voice completed")
                            }
                            override fun onError(utteranceId: String?) {
                                Log.e("ScheduleNotif", "TTS practice alarm error")
                            }
                        })

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            val audioAttributes = AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_ALARM)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                                .build()
                            textToSpeechInstance?.setAudioAttributes(audioAttributes)

                            textToSpeechInstance?.speak(
                                PRACTICE_REMINDER_MESSAGE,
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                "PracticeAlarmReminderUtterance_${System.currentTimeMillis()}"
                            )
                        } else {
                            @Suppress("DEPRECATION")
                            textToSpeechInstance?.speak(
                                PRACTICE_REMINDER_MESSAGE,
                                TextToSpeech.QUEUE_FLUSH,
                                null
                            )
                        }
                    } catch (e: Exception) {
                        Log.e("ScheduleNotif", "Error during TTS speak execution", e)
                    }
                } else {
                    Log.e("ScheduleNotif", "TTS initialization failed with status $status")
                }
            }
        } catch (e: Exception) {
            Log.e("ScheduleNotif", "Error setting up TextToSpeech", e)
        }
    }

    /**
     * Starts the persistent foreground alarm service that keeps reminding with spoken TTS until dismissed.
     */
    fun triggerPersistentAlarm(context: Context) {
        try {
            val serviceIntent = Intent(context, PracticeAlarmForegroundService::class.java).apply {
                action = PracticeAlarmForegroundService.ACTION_START_ALARM
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ContextCompat.startForegroundService(context, serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
            Log.d("ScheduleNotif", "Started PracticeAlarmForegroundService successfully")
        } catch (e: Exception) {
            Log.e("ScheduleNotif", "Error starting PracticeAlarmForegroundService: ${e.message}", e)
        }
    }

    /**
     * Stops the persistent alarm service.
     */
    fun stopActiveAlarm(context: Context) {
        try {
            val serviceIntent = Intent(context, PracticeAlarmForegroundService::class.java).apply {
                action = PracticeAlarmForegroundService.ACTION_DISMISS_ALARM
            }
            context.stopService(serviceIntent)
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.cancel(PracticeAlarmForegroundService.NOTIFICATION_ID)
            notificationManager?.cancel(NOTIFICATION_ID)
            textToSpeechInstance?.stop()
        } catch (e: Exception) {
            Log.e("ScheduleNotif", "Error stopping active alarm", e)
        }
    }

    /**
     * Cancels any pending follow-up reminder.
     * Called when the user completes practice or turns off alarms.
     */
    fun cancelFollowUpReminder(context: Context) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, PracticeReminderReceiver::class.java).apply {
                action = ACTION_FOLLOW_UP_REMINDER
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                FOLLOW_UP_ALARM_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
            Log.d("ScheduleNotif", "Cancelled follow-up reminder alarm")
        } catch (e: Exception) {
            Log.e("ScheduleNotif", "Error cancelling follow-up alarm", e)
        }
    }

    /**
     * Schedules exactly ONE follow-up reminder later on the SAME day if the user has NOT completed
     * today's practice/challenge.
     */
    fun scheduleOneFollowUpReminderIfIncomplete(context: Context) {
        try {
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Calendar.getInstance().time)
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

            // Check if user has already attended or is actively practicing
            if (hasUserAttendedPracticeToday(context) || com.example.viewmodel.SpeakingPracticeViewModel.isActivelyPracticing) {
                Log.d("ScheduleNotif", "User already completed practice today. No follow-up needed.")
                return
            }

            // Check if a follow-up reminder was already scheduled for today (Strictly ONE follow-up per day)
            val alreadyScheduledDate = prefs.getString(KEY_FOLLOW_UP_SCHEDULED_DATE, "")
            if (alreadyScheduledDate == todayStr) {
                Log.d("ScheduleNotif", "Follow-up reminder was already scheduled today. Skipping duplicate follow-up.")
                return
            }

            // Calculate follow-up time: 2 hours later, strictly clamped before 10:30 PM (22:30) of the SAME day
            val now = Calendar.getInstance()
            val followUpTime = Calendar.getInstance().apply {
                add(Calendar.HOUR_OF_DAY, 2)
            }

            val endOfDayLimit = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 22)
                set(Calendar.MINUTE, 30)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            // If 2 hours later exceeds 10:30 PM today, check if there is still time before 10:30 PM
            if (followUpTime.after(endOfDayLimit)) {
                // If now is already past 9:30 PM, no suitable follow-up time remaining today
                if (now.get(Calendar.HOUR_OF_DAY) >= 21 && now.get(Calendar.MINUTE) >= 30) {
                    Log.d("ScheduleNotif", "Too late in the day for a follow-up reminder. Skipping.")
                    return
                }
                // Clamp to 10:00 PM today
                followUpTime.set(Calendar.HOUR_OF_DAY, 22)
                followUpTime.set(Calendar.MINUTE, 0)
                followUpTime.set(Calendar.SECOND, 0)
            }

            // Ensure followUpTime is strictly in the future and strictly today
            if (followUpTime.timeInMillis <= now.timeInMillis) {
                followUpTime.timeInMillis = now.timeInMillis + (30 * 60 * 1000L) // 30 mins later
                if (followUpTime.after(endOfDayLimit)) {
                    Log.d("ScheduleNotif", "Follow-up time exceeds today limit. Skipping.")
                    return
                }
            }

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, PracticeReminderReceiver::class.java).apply {
                action = ACTION_FOLLOW_UP_REMINDER
                flags = Intent.FLAG_RECEIVER_FOREGROUND
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                FOLLOW_UP_ALARM_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val showIntent = Intent(context, MainActivity::class.java).apply {
                action = PracticeAlarmForegroundService.ACTION_START_PRACTICE
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val showPendingIntent = PendingIntent.getActivity(
                context,
                FOLLOW_UP_ALARM_REQUEST_CODE + 10,
                showIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val clockInfo = AlarmManager.AlarmClockInfo(followUpTime.timeInMillis, showPendingIntent)
                alarmManager.setAlarmClock(clockInfo, pendingIntent)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    followUpTime.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    followUpTime.timeInMillis,
                    pendingIntent
                )
            }

            // Record that follow-up has been scheduled for today
            prefs.edit().putString(KEY_FOLLOW_UP_SCHEDULED_DATE, todayStr).apply()
            Log.d("ScheduleNotif", "One follow-up reminder successfully scheduled for ${followUpTime.time} today")
        } catch (e: Exception) {
            Log.e("ScheduleNotif", "Error scheduling follow-up reminder", e)
        }
    }

    /**
     * Handles dismiss action triggered from notification or alarm.
     * Stops active alarm and evaluates if a follow-up reminder should be scheduled.
     */
    fun handleAlarmOrNotificationDismissed(context: Context) {
        stopActiveAlarm(context)
        scheduleOneFollowUpReminderIfIncomplete(context)
    }

    /**
     * Displays an independent standard Android notification for scheduled practice reminder.
     * Includes clear "DONE / STOP" and "Join Class" action buttons.
     */
    fun showReminderNotification(context: Context, isFollowUp: Boolean = false) {
        try {
            createNotificationChannel(context)

            // Permission check for Android 13+ (API 33)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.POST_NOTIFICATIONS
                    ) != android.content.pm.PackageManager.PERMISSION_GRANTED
                ) {
                    Log.w("ScheduleNotif", "POST_NOTIFICATIONS permission not granted. Cannot display reminder notification.")
                    return
                }
            }

            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                action = PracticeAlarmForegroundService.ACTION_START_PRACTICE
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                NOTIFICATION_ID,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Action: DONE / STOP (dismisses notification and stops alarm immediately)
            val dismissIntent = Intent(context, PracticeReminderReceiver::class.java).apply {
                action = ACTION_DISMISS_NOTIFICATION
            }
            val dismissPendingIntent = PendingIntent.getBroadcast(
                context,
                NOTIFICATION_ID + 2,
                dismissIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val title = if (isFollowUp) "SpeakUp AI Practice (Follow-up) ⏰" else "SpeakUp AI Practice ⏰"
            val body = if (isFollowUp) {
                "Quick reminder: You haven't completed today's speaking challenge yet. Join now to keep your progress!"
            } else {
                PRACTICE_REMINDER_MESSAGE
            }

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(body)
                )
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setContentIntent(pendingIntent)
                .setDeleteIntent(dismissPendingIntent)
                .setAutoCancel(true)
                .addAction(
                    android.R.drawable.ic_btn_speak_now,
                    "🎙️ Start Practice",
                    pendingIntent
                )
                .addAction(
                    android.R.drawable.ic_menu_close_clear_cancel,
                    "DONE / STOP",
                    dismissPendingIntent
                )
                .build()

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.notify(NOTIFICATION_ID, notification)
            Log.d("ScheduleNotif", "Posted practice reminder notification with DONE/STOP action successfully")
        } catch (e: Exception) {
            Log.e("ScheduleNotif", "Error showing reminder notification", e)
        }
    }
}

