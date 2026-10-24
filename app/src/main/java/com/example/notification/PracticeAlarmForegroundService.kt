package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import java.util.Locale

/**
 * Persistent Practice Alarm Foreground Service.
 * Speaks the English reminder as the FIRST audible output (no prior ringtones/dings).
 * Keeps repeating the reminder until the user starts practice or dismisses the alarm.
 */
class PracticeAlarmForegroundService : Service() {

    companion object {
        const val ACTION_START_ALARM = "com.example.notification.ACTION_START_ALARM"
        const val ACTION_DISMISS_ALARM = "com.example.notification.ACTION_DISMISS_ALARM"
        const val ACTION_START_PRACTICE = "com.example.notification.ACTION_START_PRACTICE"

        const val NOTIFICATION_ID = 2005
        const val CHANNEL_ID = "practice_alarm_persistent_channel"
        const val CHANNEL_NAME = "Speaking Practice Alarm"

        const val REMINDER_SPEECH_TEXT = "It’s time for your English speaking class. Please come and join your class."
    }

    private var textToSpeech: TextToSpeech? = null
    private var isTtsReady = false
    private var isAlarmActive = false
    private val handler = Handler(Looper.getMainLooper())
    private var wakeLock: PowerManager.WakeLock? = null
    private var ringtone: android.media.Ringtone? = null
    private var vibrator: android.os.Vibrator? = null

    private val repeatRunnable = object : Runnable {
        override fun run() {
            if (isAlarmActive) {
                // Check if user has started practicing while alarm was running or already attended
                if (com.example.viewmodel.SpeakingPracticeViewModel.isActivelyPracticing ||
                    ScheduleNotificationManager.hasUserAttendedPracticeToday(applicationContext)) {
                    Log.d("PracticeAlarmService", "User started practicing or attended. Stopping alarm service.")
                    stopAlarm()
                    stopSelf()
                    return
                }
                playAlarmSoundAndVoice()
                // Repeat speaking reminder every 7 seconds until user dismisses or joins class
                handler.postDelayed(this, 7000L)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        acquireWakeLock()
        createNotificationChannel()
        initTextToSpeech()
        initAlarmMedia()
    }

    private fun initAlarmMedia() {
        try {
            vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? android.os.Vibrator
            val alarmUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_ALARM)
                ?: android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
            ringtone = android.media.RingtoneManager.getRingtone(applicationContext, alarmUri)?.apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    audioAttributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                }
            }
        } catch (e: Exception) {
            Log.e("PracticeAlarmService", "Error initializing ringtone", e)
        }
    }

    private fun acquireWakeLock() {
        try {
            val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
            wakeLock = powerManager?.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "SpeakUp:PracticeAlarmWakeLock"
            )?.apply {
                setReferenceCounted(false)
                acquire(15 * 60 * 1000L) // 15 minutes max safety limit
            }
        } catch (e: Exception) {
            Log.e("PracticeAlarmService", "Error acquiring WakeLock", e)
        }
    }

    private fun releaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
            wakeLock = null
        } catch (e: Exception) {
            Log.e("PracticeAlarmService", "Error releasing WakeLock", e)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START_ALARM

        if (action == ACTION_DISMISS_ALARM) {
            Log.d("PracticeAlarmService", "Alarm dismissed by user.")
            stopAlarm()
            ScheduleNotificationManager.handleAlarmOrNotificationDismissed(applicationContext)
            stopSelf()
            return START_NOT_STICKY
        }

        // Safety check: If user has already attended or is actively practicing, do not trigger
        if (com.example.viewmodel.SpeakingPracticeViewModel.isActivelyPracticing ||
            ScheduleNotificationManager.hasUserAttendedPracticeToday(applicationContext)) {
            Log.d("PracticeAlarmService", "User already attended practice today. Aborting alarm.")
            stopAlarm()
            stopSelf()
            return START_NOT_STICKY
        }

        isAlarmActive = true

        // Build high-priority persistent notification with Start Practice and Dismiss actions
        val notification = buildPersistentNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        // Trigger immediate speech once TTS is ready
        if (isTtsReady) {
            playAlarmSoundAndVoice()
        }

        // Schedule continuous repetition until dismissed
        handler.removeCallbacks(repeatRunnable)
        handler.postDelayed(repeatRunnable, 7000L)

        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Continuous English Speaking Practice Reminders"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                setSound(null, null) // Audio is managed directly via USAGE_ALARM stream
                setShowBadge(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildPersistentNotification(): android.app.Notification {
        val startPracticeIntent = Intent(this, MainActivity::class.java).apply {
            action = ACTION_START_PRACTICE
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val startPracticePendingIntent = PendingIntent.getActivity(
            this,
            101,
            startPracticeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dismissIntent = Intent(this, PracticeAlarmForegroundService::class.java).apply {
            action = ACTION_DISMISS_ALARM
        }
        val dismissPendingIntent = PendingIntent.getService(
            this,
            102,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("English Speaking Class Alarm ⏰")
            .setContentText(REMINDER_SPEECH_TEXT)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(REMINDER_SPEECH_TEXT)
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(startPracticePendingIntent, true)
            .setOngoing(true)
            .setAutoCancel(false)
            .setContentIntent(startPracticePendingIntent)
            .addAction(
                android.R.drawable.ic_btn_speak_now,
                "🎙️ Join Class",
                startPracticePendingIntent
            )
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "DONE / STOP",
                dismissPendingIntent
            )
            .build()
    }

    private fun initTextToSpeech() {
        textToSpeech = TextToSpeech(applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                try {
                    textToSpeech?.language = Locale.ENGLISH
                    textToSpeech?.setSpeechRate(0.95f)
                    textToSpeech?.setPitch(1.0f)

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        val audioAttributes = AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build()
                        textToSpeech?.setAudioAttributes(audioAttributes)
                    }

                    textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            Log.d("PracticeAlarmService", "Speaking reminder voice started")
                        }
                        override fun onDone(utteranceId: String?) {
                            Log.d("PracticeAlarmService", "Speaking reminder voice finished sentence")
                        }
                        override fun onError(utteranceId: String?) {
                            Log.e("PracticeAlarmService", "Speaking reminder voice error")
                        }
                    })

                    isTtsReady = true
                    if (isAlarmActive) {
                        playAlarmSoundAndVoice()
                    }
                } catch (e: Exception) {
                    Log.e("PracticeAlarmService", "Error configuring TextToSpeech", e)
                }
            } else {
                Log.e("PracticeAlarmService", "TextToSpeech init failed with status: $status")
            }
        }
    }

    private fun playAlarmSoundAndVoice() {
        if (!isAlarmActive) return
        try {
            // Pulse vibration
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(
                    android.os.VibrationEffect.createWaveform(longArrayOf(0, 400, 200, 400), -1)
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 400, 200, 400), -1)
            }

            // Play ringtone chime
            try {
                if (ringtone?.isPlaying == false) {
                    ringtone?.play()
                }
            } catch (e: Exception) {
                Log.e("PracticeAlarmService", "Error playing ringtone", e)
            }

            // Speak reminder voice
            if (isTtsReady) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    textToSpeech?.speak(
                        REMINDER_SPEECH_TEXT,
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        "PracticeAlarmUtterance_${System.currentTimeMillis()}"
                    )
                } else {
                    @Suppress("DEPRECATION")
                    textToSpeech?.speak(
                        REMINDER_SPEECH_TEXT,
                        TextToSpeech.QUEUE_FLUSH,
                        null
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("PracticeAlarmService", "Error in playAlarmSoundAndVoice()", e)
        }
    }

    private fun stopAlarm() {
        isAlarmActive = false
        handler.removeCallbacks(repeatRunnable)
        try {
            ringtone?.stop()
        } catch (e: Exception) {
            Log.e("PracticeAlarmService", "Error stopping ringtone", e)
        }
        try {
            vibrator?.cancel()
        } catch (e: Exception) {
            Log.e("PracticeAlarmService", "Error cancelling vibrator", e)
        }
        try {
            textToSpeech?.stop()
        } catch (e: Exception) {
            Log.e("PracticeAlarmService", "Error stopping TTS", e)
        }
        releaseWakeLock()
    }

    override fun onDestroy() {
        stopAlarm()
        try {
            textToSpeech?.shutdown()
            textToSpeech = null
        } catch (e: Exception) {
            Log.e("PracticeAlarmService", "Error shutting down TTS", e)
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
