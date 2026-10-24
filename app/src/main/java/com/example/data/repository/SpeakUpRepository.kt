package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.model.ChatMessage
import com.example.data.model.CoachChatMessageEntity
import com.example.data.model.DayChallenge
import com.example.data.model.EnglishLevel
import com.example.data.model.GrammarCorrection
import com.example.data.model.MessageSender
import com.example.data.model.PracticeSession
import com.example.data.model.PracticeTopic
import com.example.data.model.UserProfile
import com.example.data.model.VocabularySuggestion
import com.example.data.remote.CoachAnalysisResponse
import com.example.data.remote.DayChallengeAiResponse
import com.example.data.remote.FirestoreService
import com.example.data.remote.GeminiClient
import com.example.data.remote.GeminiContent
import com.example.data.remote.GeminiGenerationConfig
import com.example.data.remote.GeminiPart
import com.example.data.remote.GeminiRequest
import com.example.data.remote.GrammarCorrectionJson
import com.example.data.remote.SessionSummaryResponse
import com.example.data.remote.SpeakingPracticeReportResponse
import com.example.data.remote.VocabItemJson
import com.squareup.moshi.JsonAdapter
import java.util.Calendar
import java.util.Locale
import kotlin.math.max
import kotlin.math.min
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SpeakUpRepository(
    private val context: Context,
    private val database: AppDatabase = AppDatabase.getDatabase(context),
    private val firestoreService: FirestoreService = FirestoreService()
) {
    private val userDao = database.userDao()
    private val sessionDao = database.sessionDao()
    private val coachMessageDao = database.coachMessageDao()
    private val moshi = GeminiClient.moshiInstance

    private val coachAnalysisAdapter: JsonAdapter<CoachAnalysisResponse> =
        moshi.adapter(CoachAnalysisResponse::class.java)

    private val sessionSummaryAdapter: JsonAdapter<SessionSummaryResponse> =
        moshi.adapter(SessionSummaryResponse::class.java)

    private val practiceReportAdapter: JsonAdapter<SpeakingPracticeReportResponse> =
        moshi.adapter(SpeakingPracticeReportResponse::class.java)

    private val dayChallengeAiAdapter: JsonAdapter<DayChallengeAiResponse> =
        moshi.adapter(DayChallengeAiResponse::class.java)

    fun getUserProfileFlow(uid: String): Flow<UserProfile?> {
        return userDao.getUserProfileFlow(uid)
    }

    suspend fun getUserProfile(uid: String): UserProfile? = withContext(Dispatchers.IO) {
        val local = userDao.getUserProfile(uid)
        if (local != null) return@withContext local

        try {
            val remote = firestoreService.getUserProfile(uid)
            if (remote != null) {
                userDao.insertOrUpdateProfile(remote)
                return@withContext remote
            }
        } catch (e: Exception) {
            Log.w("SpeakUpRepo", "Could not fetch remote profile: ${e.message}")
        }
        null
    }

    suspend fun saveUserProfile(profile: UserProfile) = withContext(Dispatchers.IO) {
        userDao.insertOrUpdateProfile(profile)
        saveSelectedPracticeDays(profile.uid, profile.practiceDays)
        try {
            firestoreService.saveUserProfile(profile)
        } catch (e: Exception) {
            Log.w("SpeakUpRepo", "Could not sync profile to Firestore: ${e.message}")
        }
    }

    fun getSessionsFlow(userId: String): Flow<List<PracticeSession>> {
        return sessionDao.getAllSessionsFlow(userId)
    }

    suspend fun getAllSessions(userId: String): List<PracticeSession> = withContext(Dispatchers.IO) {
        val local = sessionDao.getAllSessions(userId)
        if (local.isNotEmpty()) return@withContext local

        try {
            val remote = firestoreService.getSessions(userId)
            remote.forEach { sessionDao.insertSession(it) }
            return@withContext remote
        } catch (e: Exception) {
            Log.w("SpeakUpRepo", "Could not fetch remote sessions: ${e.message}")
        }
        emptyList()
    }

    suspend fun saveSession(session: PracticeSession) = withContext(Dispatchers.IO) {
        sessionDao.insertSession(session)
        val sessionTime = if (session.startTime > 0) session.startTime else session.endTime
        val sessionDate = if (sessionTime > 0) {
            java.text.SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(sessionTime)
        } else {
            getTodayDateString()
        }
        recordPracticeDateCompleted(session.userId, sessionDate)

        // Mark practice session attended for today so smart alarm knows practice is completed
        com.example.notification.ScheduleNotificationManager.markPracticeSessionAttended(context, session.userId)

        // If this was a day challenge session and was topic relevant with passing score, record day completion
        if (session.dayNumber != null && session.isTopicRelevant && session.overallScore >= 35) {
            saveDayChallengeCompleted(session.userId, session.dayNumber, session.overallScore)
        }

        try {
            firestoreService.saveSession(session)
        } catch (e: Exception) {
            Log.w("SpeakUpRepo", "Could not sync session to Firestore: ${e.message}")
        }

        // Evaluate real practice performance and adapt English difficulty level
        try {
            evaluateAndAdaptEnglishLevel(session.userId)
        } catch (e: Exception) {
            Log.w("SpeakUpRepo", "Adaptive level evaluation failed: ${e.message}")
        }
    }

    /**
     * Automatically adapts the user's English level based on REAL completed practice performance across:
     * - Grammar
     * - Vocabulary
     * - Fluency
     * - Pronunciation
     * - Speaking ability (Overall score)
     *
     * Rules:
     * 1. Initially uses the user's selected level.
     * 2. Evaluates the last 3-5 qualifying completed sessions (topic relevant, actual speaking time >= 15s, non-empty transcript).
     * 3. Requires at least 3 qualifying sessions so it does not change randomly or based on one session.
     * 4. If user consistently improves:
     *    - BEGINNER -> INTERMEDIATE (requires avg overall >= 78, and grammar/vocab/fluency/pronunciation >= 70 across 3 consecutive sessions)
     *    - INTERMEDIATE -> ADVANCED (requires avg overall >= 85, and grammar/vocab/fluency/pronunciation >= 80 across 3 consecutive sessions)
     * 5. If the user struggles, keeps the current level (no abrupt demotion) and provides supportive scaffolding.
     * 6. Saves the current adaptive level for the user's Firebase UID in Room and Firestore so it persists across app restart and login.
     */
    suspend fun evaluateAndAdaptEnglishLevel(userId: String): EnglishLevel? = withContext(Dispatchers.IO) {
        if (userId.isBlank() || userId == "default_user") return@withContext null

        val currentProfile = getUserProfile(userId) ?: return@withContext null
        val currentLevel = currentProfile.englishLevel

        // If already Advanced, no higher level to promote to
        if (currentLevel == EnglishLevel.ADVANCED) {
            return@withContext EnglishLevel.ADVANCED
        }

        // Get all completed sessions for this user
        val allSessions = sessionDao.getAllSessions(userId)

        // Filter for real, qualifying completed practice sessions
        val qualifyingSessions = allSessions.filter { session ->
            session.isTopicRelevant &&
            session.durationSeconds >= 15 &&
            session.overallScore > 0
        }.sortedByDescending { it.endTime }

        // Must have at least 3 qualifying sessions to evaluate consistency (never change based on a single session)
        if (qualifyingSessions.size < 3) {
            return@withContext currentLevel
        }

        val recentSessions = qualifyingSessions.take(3)
        val avgOverall = recentSessions.map { it.overallScore }.average()
        val avgGrammar = recentSessions.map { it.grammarScore }.average()
        val avgVocab = recentSessions.map { it.vocabularyScore }.average()
        val avgFluency = recentSessions.map { it.fluencyScore }.average()
        val avgPronunciation = recentSessions.map { it.pronunciationScore }.average()

        var newLevel = currentLevel

        when (currentLevel) {
            EnglishLevel.BEGINNER -> {
                val hasConsistentlyPassingScores = recentSessions.all { it.overallScore >= 70 }
                val meetsOverall = avgOverall >= 78.0
                val meetsGrammar = avgGrammar >= 70.0
                val meetsVocab = avgVocab >= 70.0
                val meetsFluency = avgFluency >= 70.0
                val meetsPronunciation = avgPronunciation >= 70.0

                if (hasConsistentlyPassingScores && meetsOverall && meetsGrammar && meetsVocab && meetsFluency && meetsPronunciation) {
                    newLevel = EnglishLevel.INTERMEDIATE
                    Log.i("SpeakUpRepo", "Gradually upgraded English Level: BEGINNER -> INTERMEDIATE for user $userId (avgOverall: $avgOverall, grammar: $avgGrammar, vocab: $avgVocab, fluency: $avgFluency, pron: $avgPronunciation)")
                }
            }
            EnglishLevel.INTERMEDIATE -> {
                val hasConsistentlyHighScores = recentSessions.all { it.overallScore >= 80 }
                val meetsOverall = avgOverall >= 85.0
                val meetsGrammar = avgGrammar >= 80.0
                val meetsVocab = avgVocab >= 80.0
                val meetsFluency = avgFluency >= 80.0
                val meetsPronunciation = avgPronunciation >= 80.0

                if (hasConsistentlyHighScores && meetsOverall && meetsGrammar && meetsVocab && meetsFluency && meetsPronunciation) {
                    newLevel = EnglishLevel.ADVANCED
                    Log.i("SpeakUpRepo", "Gradually upgraded English Level: INTERMEDIATE -> ADVANCED for user $userId (avgOverall: $avgOverall, grammar: $avgGrammar, vocab: $avgVocab, fluency: $avgFluency, pron: $avgPronunciation)")
                }
            }
            EnglishLevel.ADVANCED -> {
                // Keep Advanced
            }
        }

        // If the level has adapted, persist it to both Room Local DB and Firestore for this user's UID
        if (newLevel != currentLevel) {
            val updatedProfile = currentProfile.copy(
                englishLevel = newLevel,
                updatedAt = System.currentTimeMillis()
            )
            userDao.insertOrUpdateProfile(updatedProfile)
            try {
                firestoreService.saveUserProfile(updatedProfile)
            } catch (e: Exception) {
                Log.w("SpeakUpRepo", "Error syncing adapted English level to Firestore: ${e.message}")
            }
        }

        newLevel
    }

    companion object {
        fun normalizeDayName(day: String): String {
            return when (day.trim().lowercase(Locale.ENGLISH)) {
                "mon", "monday" -> "Monday"
                "tue", "tues", "tuesday" -> "Tuesday"
                "wed", "wednesday" -> "Wednesday"
                "thu", "thur", "thurs", "thursday" -> "Thursday"
                "fri", "friday" -> "Friday"
                "sat", "saturday" -> "Saturday"
                "sun", "sunday" -> "Sunday"
                else -> day.trim()
            }
        }

        fun shortDayName(day: String): String {
            return when (normalizeDayName(day)) {
                "Monday" -> "Mon"
                "Tuesday" -> "Tue"
                "Wednesday" -> "Wed"
                "Thursday" -> "Thu"
                "Friday" -> "Fri"
                "Saturday" -> "Sat"
                "Sunday" -> "Sun"
                else -> day.take(3)
            }
        }
    }

    private val PREFS_DAY_CHALLENGES = "speakup_day_challenges_prefs"
    private val KEY_COMPLETED_DAYS = "completed_days_"
    private val KEY_LAST_CHALLENGE_DATE = "last_challenge_date_"

    private val PREFS_PRACTICE_COMPLETION = "speakup_practice_completion_prefs"
    private val KEY_COMPLETED_PRACTICE_DATES = "completed_practice_dates_"

    private val PREFS_SELECTED_PRACTICE_DAYS = "speakup_selected_practice_days_prefs"
    private val KEY_SELECTED_PRACTICE_DAYS = "selected_practice_days_"

    fun getSelectedPracticeDays(userId: String = ""): List<String>? {
        val effectiveUid = userId.trim()
        if (effectiveUid.isBlank()) return null
        val prefs = context.getSharedPreferences(PREFS_SELECTED_PRACTICE_DAYS, Context.MODE_PRIVATE)
        val set = prefs.getStringSet("$KEY_SELECTED_PRACTICE_DAYS$effectiveUid", null)
        if (set != null && set.isNotEmpty()) {
            return set.map { normalizeDayName(it) }.distinct()
        }
        return null
    }

    fun saveSelectedPracticeDays(userId: String, days: List<String>) {
        val effectiveUid = userId.trim()
        if (effectiveUid.isBlank()) return
        val normalized = days.map { normalizeDayName(it) }.distinct()
        val prefs = context.getSharedPreferences(PREFS_SELECTED_PRACTICE_DAYS, Context.MODE_PRIVATE)
        prefs.edit()
            .putStringSet("$KEY_SELECTED_PRACTICE_DAYS$effectiveUid", normalized.toSet())
            .apply()

        // Keep notification prefs in sync with the exact same selected days
        try {
            val notifPrefs = context.getSharedPreferences(
                com.example.notification.ScheduleNotificationManager.PREFS_NAME,
                Context.MODE_PRIVATE
            )
            notifPrefs.edit()
                .putStringSet("${com.example.notification.ScheduleNotificationManager.KEY_PRACTICE_DAYS}_$effectiveUid", normalized.toSet())
                .putStringSet(com.example.notification.ScheduleNotificationManager.KEY_PRACTICE_DAYS, normalized.toSet())
                .apply()
        } catch (e: Exception) {
            Log.w("SpeakUpRepo", "Error syncing practice days to notification prefs: ${e.message}")
        }
    }

    fun getTodayDateString(): String {
        return java.text.SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Calendar.getInstance().time)
    }

    fun getCompletedPracticeDates(userId: String = ""): Set<String> {
        val effectiveUid = userId.trim()
        if (effectiveUid.isBlank()) return emptySet()
        val prefs = context.getSharedPreferences(PREFS_PRACTICE_COMPLETION, Context.MODE_PRIVATE)
        val rawSet = prefs.getStringSet("$KEY_COMPLETED_PRACTICE_DATES$effectiveUid", emptySet()) ?: emptySet()
        return rawSet.toSet()
    }

    fun recordPracticeDateCompleted(userId: String, dateString: String = getTodayDateString()) {
        val effectiveUid = userId.trim()
        if (effectiveUid.isBlank()) return
        val prefs = context.getSharedPreferences(PREFS_PRACTICE_COMPLETION, Context.MODE_PRIVATE)
        val currentSet = prefs.getStringSet("$KEY_COMPLETED_PRACTICE_DATES$effectiveUid", emptySet())?.toMutableSet()
            ?: mutableSetOf()
        currentSet.add(dateString)
        prefs.edit()
            .putStringSet("$KEY_COMPLETED_PRACTICE_DATES$effectiveUid", currentSet)
            .apply()
        Log.d("SpeakUpRepo", "Recorded practice completed date $dateString for user $effectiveUid. Total dates: $currentSet")

        com.example.notification.ScheduleNotificationManager.markPracticeSessionAttended(context, effectiveUid)

        if (effectiveUid != "guest_user" && effectiveUid != "default_user") {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    firestoreService.savePracticeCompletion(effectiveUid, currentSet.toList())
                } catch (e: Exception) {
                    Log.w("SpeakUpRepo", "Error persisting practice completion dates to Firestore: ${e.message}")
                }
            }
        }
    }

    suspend fun syncCompletedPracticeDates(userId: String = ""): Set<String> = withContext(Dispatchers.IO) {
        val effectiveUid = userId.trim()
        if (effectiveUid.isBlank()) return@withContext emptySet()
        val prefs = context.getSharedPreferences(PREFS_PRACTICE_COMPLETION, Context.MODE_PRIVATE)
        val mergedSet = getCompletedPracticeDates(effectiveUid).toMutableSet()

        try {
            val sessions = sessionDao.getAllSessions(effectiveUid)
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            sessions.forEach { session ->
                val time = if (session.startTime > 0) session.startTime else session.endTime
                if (time > 0) {
                    mergedSet.add(sdf.format(time))
                }
            }
        } catch (e: Exception) {
            Log.w("SpeakUpRepo", "Error syncing practice dates with Room DB: ${e.message}")
        }

        if (effectiveUid != "guest_user" && effectiveUid != "default_user") {
            try {
                val remoteDates = firestoreService.getPracticeCompletion(effectiveUid)
                mergedSet.addAll(remoteDates)
            } catch (e: Exception) {
                Log.w("SpeakUpRepo", "Error syncing practice dates from Firestore: ${e.message}")
            }
        }

        prefs.edit()
            .putStringSet("$KEY_COMPLETED_PRACTICE_DATES$effectiveUid", mergedSet)
            .apply()

        mergedSet
    }

    fun getLastDailyChallengeDate(userId: String = ""): String {
        val effectiveUid = userId.trim()
        if (effectiveUid.isBlank()) return ""
        val prefs = context.getSharedPreferences(PREFS_DAY_CHALLENGES, Context.MODE_PRIVATE)
        return prefs.getString("$KEY_LAST_CHALLENGE_DATE$effectiveUid", "") ?: ""
    }

    fun hasCompletedDailyChallengeToday(userId: String = ""): Boolean {
        val effectiveUid = userId.trim()
        if (effectiveUid.isBlank()) return false
        val lastDate = getLastDailyChallengeDate(effectiveUid)
        val todayStr = getTodayDateString()
        return lastDate.isNotBlank() && lastDate == todayStr
    }

    /**
     * Checks if the user has completed at least one regular Speaking Practice session today (non-challenge or any practice).
     */
    suspend fun hasCompletedSpeakingPracticeToday(userId: String = ""): Boolean = withContext(Dispatchers.IO) {
        val effectiveUid = userId.trim()
        if (effectiveUid.isBlank()) return@withContext false
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = calendar.timeInMillis
        val sessionsToday = sessionDao.getAllSessions(effectiveUid).filter {
            it.startTime >= startOfDay && it.dayNumber == null
        }
        sessionsToday.isNotEmpty()
    }

    fun getCompletedDayChallenges(userId: String = ""): Set<Int> {
        val effectiveUid = userId.trim()
        if (effectiveUid.isBlank()) return emptySet()
        val prefs = context.getSharedPreferences(PREFS_DAY_CHALLENGES, Context.MODE_PRIVATE)
        val rawSet = prefs.getStringSet("$KEY_COMPLETED_DAYS$effectiveUid", emptySet()) ?: emptySet()
        return rawSet.mapNotNull { it.toIntOrNull() }.toSet()
    }

    suspend fun syncCompletedDayChallengesWithDb(userId: String = ""): Set<Int> = withContext(Dispatchers.IO) {
        val effectiveUid = userId.trim()
        if (effectiveUid.isBlank()) return@withContext emptySet()
        val prefs = context.getSharedPreferences(PREFS_DAY_CHALLENGES, Context.MODE_PRIVATE)
        val mergedSet = getCompletedDayChallenges(effectiveUid).toMutableSet()
        try {
            val dbSessions = sessionDao.getDayChallengeSessions(effectiveUid)
            val dbDays = dbSessions.mapNotNull { it.dayNumber }
            mergedSet.addAll(dbDays)

            val latestChallengeSession = dbSessions.maxByOrNull { it.startTime }
            if (latestChallengeSession != null) {
                val dbDateStr = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(latestChallengeSession.startTime)
                val currentLastDate = prefs.getString("$KEY_LAST_CHALLENGE_DATE$effectiveUid", "") ?: ""
                if (currentLastDate.isBlank() || dbDateStr > currentLastDate) {
                    prefs.edit()
                        .putString("$KEY_LAST_CHALLENGE_DATE$effectiveUid", dbDateStr)
                        .apply()
                }
            }

            val stringSet = mergedSet.map { it.toString() }.toSet()
            prefs.edit()
                .putStringSet("$KEY_COMPLETED_DAYS$effectiveUid", stringSet)
                .apply()
        } catch (e: Exception) {
            Log.w("SpeakUpRepo", "Error syncing day challenges with Room DB: ${e.message}")
        }
        mergedSet
    }

    fun isTodayScheduledPracticeDay(practiceDays: List<String>): Boolean {
        if (practiceDays.isEmpty()) return true
        val currentDayName = java.text.SimpleDateFormat("EEEE", Locale.ENGLISH).format(Calendar.getInstance().time)
        val currentDayShort = java.text.SimpleDateFormat("EEE", Locale.ENGLISH).format(Calendar.getInstance().time)
        return practiceDays.any {
            it.equals(currentDayName, ignoreCase = true) || it.equals(currentDayShort, ignoreCase = true)
        }
    }

    fun isDayUnlocked(
        dayNumber: Int,
        userId: String = "",
        practiceDays: List<String> = emptyList()
    ): Boolean {
        val effectiveUid = userId.trim()
        if (effectiveUid.isBlank()) return dayNumber == 1
        val completed = getCompletedDayChallenges(effectiveUid)
        val maxCompleted = if (completed.isEmpty()) 0 else completed.maxOrNull() ?: 0
        val targetDay = maxCompleted + 1

        // If this day was already completed in the past, it remains accessible
        if (completed.contains(dayNumber)) return true

        // Only the immediate next day in sequence can be unlocked
        if (dayNumber != targetDay) return false

        // Exactly the current day in sequence is unlocked on scheduled practice days
        return isTodayScheduledPracticeDay(practiceDays)
    }

    fun isDayChallengeCompleted(dayNumber: Int, userId: String = ""): Boolean {
        val effectiveUid = userId.trim()
        if (effectiveUid.isBlank()) return false
        return getCompletedDayChallenges(effectiveUid).contains(dayNumber)
    }

    fun saveDayChallengeCompleted(userId: String, dayNumber: Int, score: Int) {
        val effectiveUid = userId.trim()
        if (effectiveUid.isBlank()) return
        val prefs = context.getSharedPreferences(PREFS_DAY_CHALLENGES, Context.MODE_PRIVATE)
        val todayStr = getTodayDateString()

        val currentSet = prefs.getStringSet("$KEY_COMPLETED_DAYS$effectiveUid", emptySet())?.toMutableSet()
            ?: mutableSetOf()
        currentSet.add(dayNumber.toString())

        prefs.edit()
            .putStringSet("$KEY_COMPLETED_DAYS$effectiveUid", currentSet)
            .putString("$KEY_LAST_CHALLENGE_DATE$effectiveUid", todayStr)
            .putInt("best_score_${effectiveUid}_day_$dayNumber", score)
            .apply()
        Log.d("SpeakUpRepo", "Day Challenge $dayNumber marked permanently completed for user $effectiveUid on $todayStr. Total completed days: $currentSet")

        // Asynchronously persist challenge progress to Firestore under users/{effectiveUid}
        if (effectiveUid != "guest_user" && effectiveUid != "default_user") {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val completedList = currentSet.mapNotNull { it.toIntOrNull() }.sorted()
                    firestoreService.saveDailyChallengeProgress(effectiveUid, completedList, todayStr)
                } catch (e: Exception) {
                    Log.w("SpeakUpRepo", "Error syncing day challenge completion to Firestore: ${e.message}")
                }
            }
        }
    }

    fun getDayChallengeBestScore(userId: String, dayNumber: Int): Int? {
        val effectiveUid = userId.trim()
        if (effectiveUid.isBlank()) return null
        val prefs = context.getSharedPreferences(PREFS_DAY_CHALLENGES, Context.MODE_PRIVATE)
        val score = prefs.getInt("best_score_${effectiveUid}_day_$dayNumber", -1)
        return if (score >= 0) score else null
    }

    suspend fun syncUserData(userId: String) = withContext(Dispatchers.IO) {
        val effectiveUid = userId.trim()
        if (effectiveUid.isBlank() || effectiveUid == "guest_user" || effectiveUid == "default_user") return@withContext
        try {
            val remoteProfile = firestoreService.getUserProfile(effectiveUid)
            if (remoteProfile != null) {
                userDao.insertOrUpdateProfile(remoteProfile)
                saveSelectedPracticeDays(effectiveUid, remoteProfile.practiceDays)
            }
            val remoteSessions = firestoreService.getSessions(effectiveUid)
            if (remoteSessions.isNotEmpty()) {
                remoteSessions.forEach { sessionDao.insertSession(it) }
                evaluateAndAdaptEnglishLevel(effectiveUid)
            }

            // Sync daily challenge state strictly for this UID
            val remoteChallenges = firestoreService.getDailyChallengeProgress(effectiveUid)
            val prefs = context.getSharedPreferences(PREFS_DAY_CHALLENGES, Context.MODE_PRIVATE)
            if (remoteChallenges != null) {
                val (completedList, lastDate) = remoteChallenges
                val localSet = prefs.getStringSet("$KEY_COMPLETED_DAYS$effectiveUid", emptySet())?.toMutableSet()
                    ?: mutableSetOf()
                completedList.forEach { localSet.add(it.toString()) }
                val localLastDate = prefs.getString("$KEY_LAST_CHALLENGE_DATE$effectiveUid", "") ?: ""
                val effectiveLastDate = if (lastDate.isNotBlank()) lastDate else localLastDate
                prefs.edit()
                    .putStringSet("$KEY_COMPLETED_DAYS$effectiveUid", localSet)
                    .putString("$KEY_LAST_CHALLENGE_DATE$effectiveUid", effectiveLastDate)
                    .apply()
            }

            // Sync practice completion dates strictly for this UID
            syncCompletedPracticeDates(effectiveUid)
        } catch (e: Exception) {
            Log.w("SpeakUpRepo", "Could not sync user data from Firestore: ${e.message}")
        }
    }

    fun getCoachMessagesFlow(userId: String): Flow<List<ChatMessage>> {
        return coachMessageDao.getMessagesFlow(userId).map { entities ->
            entities.map { it.toChatMessage() }
        }
    }

    suspend fun getCoachMessages(userId: String): List<ChatMessage> = withContext(Dispatchers.IO) {
        coachMessageDao.getMessages(userId).map { it.toChatMessage() }
    }

    suspend fun saveCoachMessage(userId: String, message: ChatMessage) = withContext(Dispatchers.IO) {
        coachMessageDao.insertMessage(CoachChatMessageEntity.fromChatMessage(message, userId))
    }

    suspend fun saveCoachMessages(userId: String, messages: List<ChatMessage>) = withContext(Dispatchers.IO) {
        coachMessageDao.insertMessages(messages.map { CoachChatMessageEntity.fromChatMessage(it, userId) })
    }

    suspend fun clearCoachMessages(userId: String) = withContext(Dispatchers.IO) {
        coachMessageDao.clearMessagesForUser(userId)
    }

    /**
     * AI Coach Conversational Utterance Evaluation.
     * Evaluates free conversation / user questions concisely with natural answers.
     */
    suspend fun analyzeCoachUtterance(
        userText: String,
        conversationHistory: List<ChatMessage>,
        level: EnglishLevel
    ): CoachAnalysisResponse = withContext(Dispatchers.IO) {
        val apiKey = GeminiClient.getApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w("SpeakUpRepo", "Using local AI Coach analyzer (API key placeholder)")
            return@withContext fallbackAnalyzeCoachUtterance(userText, level)
        }

        try {
            val historyContext = conversationHistory.takeLast(6).joinToString("\n") {
                val role = if (it.sender == MessageSender.USER) "User" else "AI Coach"
                "$role: ${it.text}"
            }

            val prompt = """
                You are SpeakUp AI, an encouraging, natural English Speaking Coach and conversation partner.
                User English Level: ${level.displayName}

                Recent Conversation Context:
                $historyContext

                Latest User Input: "$userText"

                Instructions:
                1. Understand the user's intent first. The user may speak English, Tanglish (Tamil + English words like 'naan', 'college ponaen', 'romba nalla'), or ask any general question (e.g. 'How can I use this app?', 'What is AI?').
                2. If the user asks a normal question, answer it directly and concisely (4-5 lines max).
                3. If the user makes a grammar mistake or uses Tanglish:
                   - Do not ignore it!
                   - Identify the user's original sentence (originalSentence).
                   - Provide the natural, better English version (correctedSentence).
                   - Provide a simple 1-2 sentence explanation of why (grammarExplanation).
                4. coachReply: A friendly, supportive conversational reply continuing the conversation naturally. Do not interrupt with a huge lecture.
                5. Estimate fluencyScore (0-100), pronunciationTip, confidenceScore (0-100).

                Return ONLY valid JSON matching this schema:
                {
                  "coachReply": "string",
                  "isTanglishDetected": boolean,
                  "hasGrammarMistake": boolean,
                  "originalSentence": "string or null",
                  "correctedSentence": "string or null",
                  "grammarExplanation": "string or null",
                  "vocabularySuggestions": [
                    {
                      "word": "string",
                      "betterAlternatives": ["string", "string"],
                      "context": "string"
                    }
                  ],
                  "fluencyScore": number,
                  "fluencyFeedback": "string",
                  "pronunciationTip": "string",
                  "confidenceScore": number
                }
            """.trimIndent()

            val request = GeminiRequest(
                contents = listOf(
                    GeminiContent(
                        role = "user",
                        parts = listOf(GeminiPart(text = prompt))
                    )
                ),
                generationConfig = GeminiGenerationConfig(
                    temperature = 0.6f,
                    topP = 0.9f,
                    responseMimeType = "application/json"
                )
            )

            val response = GeminiClient.apiService.generateContent(apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text

            if (!jsonText.isNullOrBlank()) {
                val cleanJson = extractJson(jsonText)
                val parsed = coachAnalysisAdapter.fromJson(cleanJson)
                if (parsed != null) {
                    return@withContext parsed
                }
            }
            fallbackAnalyzeCoachUtterance(userText, level)
        } catch (e: Exception) {
            Log.e("SpeakUpRepo", "Gemini API call failed for Coach Utterance", e)
            fallbackAnalyzeCoachUtterance(userText, level)
        }
    }

    /**
     * Speaking Practice Monologue Analysis.
     * Evaluates the completed monologue transcript for one of the 15 predefined topics or Day Challenges.
     * Calculates realistic scores strictly from actual speech/transcript performance without artificial inflation.
     */
    suspend fun analyzeSpeakingPracticeTranscript(
        topic: PracticeTopic,
        level: EnglishLevel,
        transcriptText: String,
        durationSeconds: Long
    ): SpeakingPracticeReportResponse = withContext(Dispatchers.IO) {
        val trimmedTranscript = transcriptText.trim()
        val words = trimmedTranscript.split(Regex("\\s+")).filter { it.isNotBlank() }
        val wordCount = words.size

        if (wordCount == 0 || trimmedTranscript.isBlank()) {
            return@withContext computeLocalSpeakingPracticeReport(topic, trimmedTranscript, durationSeconds)
        }

        val sentences = trimmedTranscript.split(Regex("[.!?]+|\\n+")).map { it.trim() }.filter { it.isNotBlank() }
        val sentenceCount = sentences.size.coerceAtLeast(1)
        val effectiveDuration = durationSeconds.coerceAtLeast(1)
        val wpm = ((wordCount * 60f) / effectiveDuration).toInt()

        val apiKey = GeminiClient.getApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext computeLocalSpeakingPracticeReport(topic, trimmedTranscript, durationSeconds)
        }

        try {
            val prompt = """
                You are evaluating a completed English Speaking Practice session on a specific topic.
                Topic: ${topic.title}
                User English Level: ${level.displayName}
                Actual Speaking Duration: $durationSeconds seconds
                Actual Word Count: $wordCount words
                Actual Sentence Count: $sentenceCount sentences
                Calculated Speaking Rate: $wpm words per minute
                Topic Subtitle: ${topic.subtitle}
                Starter Prompt: ${topic.starterPrompt}

                Verbatim Spoken Transcript:
                "$trimmedTranscript"

                CRITICAL SCORING MANDATE - STRICT REAL-PERFORMANCE EVALUATION:
                - DO NOT give high, fixed, or default scores (such as 75%, 80%, or 85%).
                - DO NOT artificially boost scores simply because the session was completed.
                - Scores MUST be calculated strictly from the user's actual speech/transcript and real spoken performance.
                - If the user speaks only 2–3 sentences (or 10-35 words), evaluate ONLY that actual content. A short 2–3 sentence response has limited vocabulary, brief fluency, and basic structure, so its realistic overall score should typically be in the 35–55% range (or lower if there are errors).
                - If the user speaks only 1 sentence or a few words (< 10 words), score realistically in the 15–35% range.
                - Higher scores (75–90%+) can ONLY be awarded when the user delivers sustained, multi-sentence speech with varied vocabulary, correct grammar, and steady fluency.

                Evaluation Dimensions:
                1. Grammar (0-100):
                   - Evaluate the actual grammatical correctness of spoken sentences.
                   - For 1-3 simple sentences with basic structure and no errors: award realistic basic score (50-62%).
                   - For errors (tense mistakes, subject-verb disagreement, missing prepositions, Tanglish): penalize heavily (25-45%).
                   - Higher scores (75-95%) require compound/complex sentence structures with zero to few errors across extended speech.
                2. Vocabulary (0-100):
                   - Evaluate the lexical diversity and topic-specific vocabulary in the transcript.
                   - Basic repetitive words (e.g. "good, like, nice, big, happy"): score 30-48%.
                   - Descriptive and topic-appropriate terms: score 52-70%.
                   - Rich, nuanced, idiomatic vocabulary: score 75-95%.
                3. Fluency (0-100):
                   - Evaluated from content volume, sentence continuity, transitions, and words per minute (WPM).
                   - Only 2-3 brief sentences or low speaking rate (< 60 WPM): score 30-50%.
                   - Continuous flow with connectors (because, although, however, therefore, while) and natural pace (90-130 WPM): score 75-95%.
                4. Pronunciation / Clarity (0-100):
                   - Evaluated from transcription clarity, recognizable word patterns, and enunciation.
                   - For short 2-3 sentence speech: score 40-55% proportionate to the small sample.
                   - Clear phonological enunciation across extensive vocabulary: score 70-90%.
                5. Confidence (0-100):
                   - Evaluated from fullness of response, completeness of thoughts, and lack of trailing fragments.
                   - Short 2-3 sentence response: score 35-50%.
                   - Comprehensive, confident elaboration: score 75-95%.
                6. Overall Score (0-100):
                   - Must strictly equal the weighted composite:
                     overallScore = (grammarScore * 0.25) + (vocabularyScore * 0.25) + (fluencyScore * 0.25) + (pronunciationScore * 0.10) + (confidenceScore * 0.15)

                Topic Relevance Evaluation (STRICT):
                - Evaluate whether the user's speech is actually related to the selected topic "${topic.title}".
                - For example, if the topic is "My Best Friend", the user's answer MUST be about their best friend.
                - DO NOT give a score just because the English grammar is correct.
                - If the speech is NOT about "${topic.title}":
                  * set "isTopicRelevant": false
                  * set "topicRelevanceNotice": "Please speak only about the selected topic."
                  * set "overallScore": 0
                  * set "grammarScore": 0
                  * set "vocabularyScore": 0
                  * set "fluencyScore": 0
                  * set "pronunciationScore": 0
                  * set "confidenceScore": 0
                  * set "feedbackSummary": "Please speak only about the selected topic."
                  * set "improvementTip": "Your speech must be directly about ${topic.title}. Please speak about ${topic.title} to receive your score."
                - If the speech IS genuinely related to "${topic.title}":
                  * set "isTopicRelevant": true
                  * set "topicRelevanceNotice": null

                Grammar & Vocabulary Feedback:
                - Identify any grammar errors or Tanglish phrases.
                - Provide 2-3 upgraded vocabulary suggestions with context.
                - Provide a concise feedbackSummary and improvementTip reflecting actual performance.

                Return ONLY valid JSON matching this schema:
                {
                  "overallScore": number,
                  "grammarScore": number,
                  "vocabularyScore": number,
                  "fluencyScore": number,
                  "pronunciationScore": number,
                  "confidenceScore": number,
                  "feedbackSummary": "string",
                  "improvementTip": "string",
                  "isTopicRelevant": boolean,
                  "topicRelevanceNotice": "string or null",
                  "grammarCorrections": [
                    {
                      "originalSentence": "string",
                      "correctedSentence": "string",
                      "explanation": "string",
                      "isTanglish": boolean
                    }
                  ],
                  "vocabularySuggestions": [
                    {
                      "word": "string",
                      "betterAlternatives": ["string", "string"],
                      "context": "string"
                    }
                  ]
                }
            """.trimIndent()

            val request = GeminiRequest(
                contents = listOf(
                    GeminiContent(
                        role = "user",
                        parts = listOf(GeminiPart(text = prompt))
                    )
                ),
                generationConfig = GeminiGenerationConfig(
                    temperature = 0.2f,
                    responseMimeType = "application/json"
                )
            )

            val response = GeminiClient.apiService.generateContent(apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text

            if (!jsonText.isNullOrBlank()) {
                val cleanJson = extractJson(jsonText)
                val parsed = practiceReportAdapter.fromJson(cleanJson)
                if (parsed != null) {
                    // Calibrate parsed scores to enforce real performance bounds based on actual word and sentence count
                    return@withContext calibrateReportScores(parsed, wordCount, sentenceCount, durationSeconds, topic)
                }
            }
            computeLocalSpeakingPracticeReport(topic, trimmedTranscript, durationSeconds)
        } catch (e: Exception) {
            Log.e("SpeakUpRepo", "Speaking Practice analysis failed", e)
            computeLocalSpeakingPracticeReport(topic, trimmedTranscript, durationSeconds)
        }
    }

    private fun getTopicKeywords(topic: PracticeTopic): Set<String> {
        val baseSet = mutableSetOf<String>()
        val textToTokenize = "${topic.title} ${topic.subtitle} ${topic.starterPrompt} ${topic.sampleQuestions.joinToString(" ")}"
        val words = textToTokenize.lowercase()
            .split(Regex("[^a-zA-Z0-9]+"))
            .filter { it.length > 2 && it !in listOf("the", "and", "for", "with", "this", "that", "you", "your", "what", "how", "why", "are", "about", "can", "have") }
        baseSet.addAll(words)

        when (topic.id) {
            "self_intro" -> baseSet.addAll(listOf("name", "born", "from", "live", "family", "education", "study", "student", "college", "school", "degree", "work", "job", "myself", "am", "years", "old", "hobby", "goal", "aim", "passionate", "ambition", "intro", "personal", "background", "brother", "sister", "parents"))
            "my_village_city" -> baseSet.addAll(listOf("village", "city", "town", "hometown", "place", "street", "temple", "lake", "river", "market", "people", "culture", "climate", "weather", "beautiful", "located", "district", "state", "native", "green", "farms", "landmark", "monument", "shops"))
            "my_best_friend" -> baseSet.addAll(listOf("friend", "best", "friendship", "together", "met", "school", "college", "help", "kind", "honest", "share", "memories", "talk", "support", "laugh", "fun", "trust", "loyal", "buddy", "close", "companion", "character"))
            "daily_routine" -> baseSet.addAll(listOf("wake", "morning", "night", "routine", "day", "sleep", "brush", "breakfast", "lunch", "dinner", "exercise", "study", "work", "schedule", "time", "clock", "evening", "afternoon", "habit", "daily", "tasks", "commute", "walk"))
            "hobbies_free_time" -> baseSet.addAll(listOf("hobby", "hobbies", "free", "time", "spare", "play", "cricket", "football", "music", "sing", "dance", "read", "book", "game", "draw", "paint", "swim", "chess", "art", "relax", "enjoy", "interest", "sports", "gardening"))
            "college_school_days" -> baseSet.addAll(listOf("college", "school", "teacher", "professor", "class", "classroom", "student", "friends", "exam", "grade", "campus", "fest", "event", "study", "subject", "memories", "days", "lecture", "uniform", "homework", "hostel", "canteen"))
            "dream_job" -> baseSet.addAll(listOf("job", "dream", "career", "work", "company", "engineer", "doctor", "developer", "software", "manager", "business", "salary", "future", "ambition", "profession", "skill", "interview", "hire", "goal", "office", "lead", "role"))
            "weekend_activities" -> baseSet.addAll(listOf("weekend", "saturday", "sunday", "relax", "outing", "trip", "family", "friends", "movie", "shopping", "park", "rest", "sleep", "fun", "holiday", "week", "unwind", "play", "stay", "dinner", "cinema"))
            "favourite_food" -> baseSet.addAll(listOf("food", "dish", "favourite", "favorite", "eat", "meal", "taste", "delicious", "biryani", "dosa", "rice", "curry", "chicken", "paneer", "sweet", "spicy", "cook", "restaurant", "recipe", "snack", "breakfast", "dinner", "lunch", "dessert", "fruits"))
            "memorable_journey" -> baseSet.addAll(listOf("journey", "trip", "travel", "vacation", "tour", "visit", "train", "bus", "car", "flight", "place", "mountains", "beach", "scenery", "hotel", "explore", "memorable", "destination", "road", "ticket", "station", "sightseeing"))
            "technology_daily_life" -> baseSet.addAll(listOf("technology", "tech", "smartphone", "phone", "mobile", "computer", "laptop", "internet", "ai", "artificial", "intelligence", "app", "online", "digital", "device", "gadget", "software", "machine", "modern", "automation"))
            "festival_you_celebrate" -> baseSet.addAll(listOf("festival", "celebrate", "celebration", "diwali", "pongal", "deepavali", "eid", "christmas", "puja", "sweets", "clothes", "family", "prayer", "temple", "lights", "crackers", "traditional", "culture", "holiday", "special", "rituals"))
            "importance_learning_english" -> baseSet.addAll(listOf("english", "language", "learn", "speak", "fluent", "fluency", "grammar", "vocabulary", "communication", "talk", "confidence", "global", "career", "job", "international", "improve", "practice", "skills", "words", "opportunities"))
            "social_media" -> baseSet.addAll(listOf("social", "media", "instagram", "facebook", "whatsapp", "youtube", "tiktok", "twitter", "x", "post", "reel", "video", "chat", "message", "online", "internet", "followers", "share", "content", "platform", "scroll", "screen", "digital", "phone", "connect", "influencer", "app"))
        }

        val dayChallenge = DayChallengeData.dailyChallenges.firstOrNull {
            it.title.equals(topic.title, ignoreCase = true) || topic.title.startsWith("Day ${it.dayNumber}")
        }
        if (dayChallenge != null) {
            baseSet.addAll(dayChallenge.keywords)
        }

        return baseSet
    }

    private fun calibrateReportScores(
        parsed: SpeakingPracticeReportResponse,
        wordCount: Int,
        sentenceCount: Int,
        durationSeconds: Long,
        topic: PracticeTopic
    ): SpeakingPracticeReportResponse {
        if (wordCount == 0) {
            return parsed.copy(
                overallScore = 0,
                grammarScore = 0,
                vocabularyScore = 0,
                fluencyScore = 0,
                pronunciationScore = 0,
                confidenceScore = 0
            )
        }

        val isTopicRelevant = parsed.isTopicRelevant
        if (!isTopicRelevant) {
            return parsed.copy(
                overallScore = 0,
                grammarScore = 0,
                vocabularyScore = 0,
                fluencyScore = 0,
                pronunciationScore = 0,
                confidenceScore = 0,
                topicRelevanceNotice = "Please speak only about the selected topic.",
                feedbackSummary = "Please speak only about the selected topic.",
                improvementTip = "Your speech must be directly about ${topic.title}. Please speak about ${topic.title} to receive your score."
            )
        }

        // Calibrate ceiling limits based on actual speech output volume to prevent inflated scores for few sentences
        val (maxOverall, maxGrammar, maxVocab, maxFluency, maxPron, maxConf) = when {
            wordCount < 10 -> SixScores(35, 48, 35, 30, 45, 35)
            wordCount in 10..22 -> SixScores(52, 65, 52, 50, 58, 52)
            wordCount in 23..40 -> SixScores(62, 75, 62, 62, 68, 62)
            wordCount in 41..70 -> SixScores(76, 85, 78, 76, 80, 78)
            else -> SixScores(100, 100, 100, 100, 100, 100)
        }

        val gScore = parsed.grammarScore.coerceIn(15, maxGrammar)
        val vScore = parsed.vocabularyScore.coerceIn(15, maxVocab)
        val fScore = parsed.fluencyScore.coerceIn(15, maxFluency)
        val pScore = parsed.pronunciationScore.coerceIn(20, maxPron)
        val cScore = parsed.confidenceScore.coerceIn(15, maxConf)

        val computedOverall = ((gScore * 0.25f) + (vScore * 0.25f) + (fScore * 0.25f) + (pScore * 0.10f) + (cScore * 0.15f)).toInt()
        val finalOverall = computedOverall.coerceIn(10, maxOverall)

        return parsed.copy(
            overallScore = finalOverall,
            grammarScore = gScore,
            vocabularyScore = vScore,
            fluencyScore = fScore,
            pronunciationScore = pScore,
            confidenceScore = cScore
        )
    }

    private data class SixScores(
        val overall: Int,
        val grammar: Int,
        val vocab: Int,
        val fluency: Int,
        val pron: Int,
        val conf: Int
    )

    private fun computeLocalSpeakingPracticeReport(
        topic: PracticeTopic,
        transcript: String,
        durationSeconds: Long
    ): SpeakingPracticeReportResponse {
        val trimmed = transcript.trim()
        val words = trimmed.split(Regex("\\s+")).filter { it.isNotBlank() }
        val wordCount = words.size

        if (wordCount == 0) {
            return SpeakingPracticeReportResponse(
                overallScore = 0,
                grammarScore = 0,
                vocabularyScore = 0,
                fluencyScore = 0,
                pronunciationScore = 0,
                confidenceScore = 0,
                feedbackSummary = "No speech detected. Please speak into the microphone during your practice session.",
                improvementTip = "Try speaking at least a few sentences about ${topic.title}.",
                isTopicRelevant = true,
                topicRelevanceNotice = null
            )
        }

        val lower = trimmed.lowercase()
        val sentences = trimmed.split(Regex("[.!?]+|\\n+")).map { it.trim() }.filter { it.isNotBlank() }
        val sentenceCount = sentences.size.coerceAtLeast(1)

        val cleanedWords = words.map { it.lowercase().filter { c -> c.isLetter() } }.filter { it.isNotBlank() }
        val uniqueWords = cleanedWords.toSet()
        val lexicalDiversity = if (cleanedWords.isNotEmpty()) uniqueWords.size.toFloat() / cleanedWords.size else 0f
        val complexWordsCount = cleanedWords.count { it.length >= 7 }

        val corrections = mutableListOf<GrammarCorrectionJson>()
        val vocabSuggestions = mutableListOf<VocabItemJson>()

        // Check topic relevance
        val topicKeywords = getTopicKeywords(topic)
        val transcriptTokens = lower.split(Regex("[^a-zA-Z0-9]+")).filter { it.isNotBlank() }.toSet()
        val matchedKeywordCount = topicKeywords.count { it in transcriptTokens || lower.contains(it) }

        val isOpenOrCustom = topic.id == "open_topic" || topic.category.equals("Custom", ignoreCase = true)
        val isTopicRelevant = if (isOpenOrCustom || wordCount < 5) {
            true
        } else {
            matchedKeywordCount > 0
        }

        val topicRelevanceNotice = if (!isTopicRelevant) {
            "Please speak only about the selected topic."
        } else null

        // Grammar checks
        if (lower.contains("i go to college yesterday") || lower.contains("i go yesterday") || lower.contains("i go college")) {
            corrections.add(
                GrammarCorrectionJson(
                    originalSentence = "I go to college yesterday.",
                    correctedSentence = "I went to college yesterday.",
                    explanation = "Use the simple past tense 'went' because the action took place yesterday."
                )
            )
        } else if (lower.contains("i am go")) {
            corrections.add(
                GrammarCorrectionJson(
                    originalSentence = "I am go.",
                    correctedSentence = "I am going.",
                    explanation = "With the auxiliary 'am', use the present participle '-ing' form (e.g. 'I am going')."
                )
            )
        } else if (lower.contains("she go") || lower.contains("he go")) {
            corrections.add(
                GrammarCorrectionJson(
                    originalSentence = "He / She go.",
                    correctedSentence = "He / She goes.",
                    explanation = "Use third-person singular 'goes' with he or she in the simple present tense."
                )
            )
        } else if (lower.contains("didn't went")) {
            corrections.add(
                GrammarCorrectionJson(
                    originalSentence = "I didn't went.",
                    correctedSentence = "I didn't go.",
                    explanation = "After 'didn't', always use the base form of the verb ('go')."
                )
            )
        }

        // Tanglish checks
        val tanglishKeywords = listOf("naan", "ponaen", "vandhaen", "pannaen", "romba nalla", "enga", "irukku")
        if (tanglishKeywords.any { lower.contains(it) }) {
            corrections.add(
                GrammarCorrectionJson(
                    originalSentence = transcript.take(60),
                    correctedSentence = "I really enjoyed my day and spent quality time.",
                    explanation = "Translate Tamil expressions into full English phrases to build spoken confidence.",
                    isTanglish = true
                )
            )
        }

        // Vocab suggestions
        if (lower.contains("good") || lower.contains("very good")) {
            vocabSuggestions.add(
                VocabItemJson("good", listOf("fantastic", "exceptional", "rewarding"), "To describe high quality experiences")
            )
        }
        if (lower.contains("happy") || lower.contains("like")) {
            vocabSuggestions.add(
                VocabItemJson("happy", listOf("delighted", "thrilled", "enthusiastic"), "Expressing genuine joy")
            )
        }
        if (lower.contains("important") || lower.contains("big")) {
            vocabSuggestions.add(
                VocabItemJson("important", listOf("crucial", "pivotal", "significant"), "To highlight major priority")
            )
        }

        // Connectors detection
        val connectors = listOf("because", "although", "however", "therefore", "moreover", "since", "while", "as a result", "and", "but", "so")
        val connectorBonus = connectors.count { lower.contains(it) }.coerceAtMost(3) * 3

        // Realistic score calculation based on actual speech metrics
        val effectiveDuration = durationSeconds.coerceIn(5, 300)
        val wpm = (wordCount * 60f / effectiveDuration).toInt()

        val rawGrammarScore: Int
        val rawVocabScore: Int
        val rawFluencyScore: Int
        val rawPronScore: Int
        val rawConfScore: Int

        when {
            wordCount < 10 -> {
                // Short fragment / 1 brief sentence
                val errorPenalty = corrections.size * 12
                rawGrammarScore = (42 - errorPenalty).coerceIn(15, 48)
                rawVocabScore = (18 + uniqueWords.size * 2).coerceIn(15, 35)
                rawFluencyScore = (16 + wordCount * 2).coerceIn(15, 30)
                rawPronScore = 35
                rawConfScore = (20 + wordCount * 2).coerceIn(18, 35)
            }
            wordCount in 10..35 -> {
                // 2-3 brief sentences
                val errorPenalty = corrections.size * 10
                rawGrammarScore = (55 - errorPenalty + connectorBonus).coerceIn(25, 62)
                rawVocabScore = (30 + (uniqueWords.size * 1.1f).toInt() + (matchedKeywordCount * 3) + (complexWordsCount * 2)).coerceIn(25, 52)
                val wpmFactor = when { wpm < 40 -> 32; wpm < 70 -> 42; else -> 48 }
                rawFluencyScore = ((wpmFactor * 0.6f) + (wordCount * 0.4f) + connectorBonus).toInt().coerceIn(25, 52)
                rawPronScore = (42 + (lexicalDiversity * 10).toInt()).coerceIn(35, 55)
                rawConfScore = (32 + (sentenceCount * 4) + (wordCount / 4)).coerceIn(28, 52)
            }
            wordCount in 36..70 -> {
                // Moderate paragraph
                val errorPenalty = corrections.size * 8
                rawGrammarScore = (68 - errorPenalty + connectorBonus).coerceIn(40, 78)
                rawVocabScore = (45 + (uniqueWords.size * 0.8f).toInt() + (matchedKeywordCount * 3) + (complexWordsCount * 2)).coerceIn(40, 75)
                val wpmFactor = when { wpm < 50 -> 45; wpm < 80 -> 60; else -> 72 }
                rawFluencyScore = ((wpmFactor * 0.6f) + (wordCount * 0.3f) + connectorBonus).toInt().coerceIn(40, 75)
                rawPronScore = (55 + (lexicalDiversity * 15).toInt()).coerceIn(45, 75)
                rawConfScore = (50 + (sentenceCount * 3) + (wordCount / 5)).coerceIn(45, 75)
            }
            else -> {
                // Sustained speech (> 70 words)
                val errorPenalty = corrections.size * 6
                rawGrammarScore = (82 - errorPenalty + connectorBonus).coerceIn(55, 95)
                rawVocabScore = (60 + (uniqueWords.size * 0.5f).toInt() + (matchedKeywordCount * 2) + (complexWordsCount * 2)).coerceIn(55, 94)
                val wpmFactor = when { wpm < 60 -> 55; wpm in 80..130 -> 85; else -> 75 }
                rawFluencyScore = ((wpmFactor * 0.6f) + (wordCount * 0.2f) + connectorBonus).toInt().coerceIn(55, 95)
                rawPronScore = (70 + (lexicalDiversity * 15).toInt()).coerceIn(60, 92)
                rawConfScore = (65 + (sentenceCount * 2) + (wordCount / 8)).coerceIn(60, 95)
            }
        }

        val grammarScore = if (!isTopicRelevant) 0 else rawGrammarScore
        val vocabularyScore = if (!isTopicRelevant) 0 else rawVocabScore
        val fluencyScore = if (!isTopicRelevant) 0 else rawFluencyScore
        val pronunciationScore = if (!isTopicRelevant) 0 else rawPronScore
        val confidenceScore = if (!isTopicRelevant) 0 else rawConfScore

        val overallScore = if (!isTopicRelevant) {
            0
        } else {
            ((grammarScore * 0.25f) + (vocabularyScore * 0.25f) + (fluencyScore * 0.25f) + (pronunciationScore * 0.10f) + (confidenceScore * 0.15f)).toInt().coerceIn(10, 98)
        }

        val feedbackSummary = if (!isTopicRelevant) {
            "Please speak only about the selected topic."
        } else if (wordCount < 15) {
            "You spoke briefly ($wordCount words) about ${topic.title}. Try elaborating with more sentences to build fluency."
        } else if (wordCount <= 35) {
            "Good start! You spoke 2–3 sentences on ${topic.title}. Continue practicing by adding more details, reasons, and examples."
        } else {
            "Well done! You spoke about ${topic.title} for ${durationSeconds} seconds with clear expression and good topic focus."
        }

        val improvementTip = if (!isTopicRelevant) {
            "Your speech must be directly about ${topic.title}. Please speak about ${topic.title} to receive your score."
        } else if (wordCount < 20) {
            "Try expanding your answers by giving reasons using 'because' and sharing personal examples."
        } else if (corrections.isNotEmpty()) {
            "Practice maintaining correct past tense verbs and subject-verb agreement throughout your speaking."
        } else {
            "Continue expanding your descriptive vocabulary with advanced adjectives and varied connectors."
        }

        return SpeakingPracticeReportResponse(
            overallScore = overallScore,
            grammarScore = grammarScore,
            vocabularyScore = vocabularyScore,
            fluencyScore = fluencyScore,
            pronunciationScore = pronunciationScore,
            confidenceScore = confidenceScore,
            feedbackSummary = feedbackSummary,
            improvementTip = improvementTip,
            isTopicRelevant = isTopicRelevant,
            topicRelevanceNotice = topicRelevanceNotice,
            grammarCorrections = corrections.ifEmpty { null },
            vocabularySuggestions = vocabSuggestions.ifEmpty { null }
        )
    }

    private fun fallbackAnalyzeCoachUtterance(
        userText: String,
        level: EnglishLevel
    ): CoachAnalysisResponse {
        val lower = userText.lowercase().trim()

        // 1. App guidance questions
        if (lower.contains("how can i use") || lower.contains("how to use this app") || lower.contains("what is this app")) {
            return CoachAnalysisResponse(
                coachReply = "You can use SpeakUp AI to practice English speaking.\nChoose Speaking Practice to practice a topic.\nYou can also use AI Coach for natural conversations.\nSpeak naturally and the app will help you improve your English.",
                isTanglishDetected = false,
                hasGrammarMistake = false,
                fluencyScore = 90,
                confidenceScore = 90
            )
        }

        // 2. Knowledge questions (e.g. AI)
        if (lower.contains("what is artificial intelligence") || lower.contains("what is ai")) {
            return CoachAnalysisResponse(
                coachReply = "Artificial Intelligence, or AI, is technology that allows computers to perform tasks that normally require human intelligence.\nFor example, AI can understand language, recognize images and help answer questions.",
                isTanglishDetected = false,
                hasGrammarMistake = false,
                fluencyScore = 92,
                confidenceScore = 92
            )
        }

        // 3. Tanglish detection
        val tanglishKeywords = listOf("naan", "enga", "ponaen", "vandhaen", "pannaen", "irukku", "romba", "nalla", "aprom", "enna", "theriyum")
        val isTanglish = tanglishKeywords.any { lower.contains(it) }

        var hasGrammarMistake = false
        var originalSentence: String? = null
        var correctedSentence: String? = null
        var grammarExplanation: String? = null

        if (isTanglish) {
            hasGrammarMistake = true
            originalSentence = userText
            if (lower.contains("naan") && lower.contains("ponaen")) {
                correctedSentence = "I went to college yesterday."
                grammarExplanation = "Convert Tamil phrase 'naan ponaen' into English 'I went' using the simple past tense."
            } else if (lower.contains("romba nalla")) {
                correctedSentence = "It is very good and enjoyable."
                grammarExplanation = "Use 'very good' or 'fantastic' instead of 'romba nalla' in English."
            } else {
                correctedSentence = "I really enjoyed that and met my friends."
                grammarExplanation = "Great effort! Expressing your thoughts fully in English builds natural fluency."
            }
        } else if (lower.contains("i go college") || lower.contains("i go to college yesterday") || lower.contains("i go yesterday")) {
            hasGrammarMistake = true
            originalSentence = userText
            correctedSentence = "I went to college yesterday."
            grammarExplanation = "Because the action happened yesterday, use the past tense 'went'."
        } else if (lower.contains("i am go")) {
            hasGrammarMistake = true
            originalSentence = userText
            correctedSentence = "I am going to college today."
            grammarExplanation = "With 'am', use the '-ing' present participle form 'going'."
        } else if (lower.contains("she go") || lower.contains("he go")) {
            hasGrammarMistake = true
            originalSentence = userText
            correctedSentence = userText.replace(Regex("(?i)\\b(she|he) go\\b")) { "${it.groupValues[1]} goes" }
            grammarExplanation = "With third-person singular subjects (he, she, it), use 'goes' in the simple present tense."
        }

        val vocabList = mutableListOf<VocabItemJson>()
        if (lower.contains("fine") || lower.contains("good")) {
            vocabList.add(VocabItemJson("fine", listOf("doing great", "wonderful", "splendid"), "Expressing positive state"))
        }

        val coachReply = when {
            isTanglish -> "I understand what you mean! That sounds wonderful. What happened next?"
            hasGrammarMistake -> "I understand what you mean! Let's keep practicing. What are you planning to do next?"
            lower.contains("hello") || lower.contains("hi") -> "Hi! How are you doing today? What's on your mind?"
            lower.contains("fine") || lower.contains("i am fine") -> "That's great! What are you doing today?"
            else -> "That is wonderful! Could you tell me more about that?"
        }

        return CoachAnalysisResponse(
            coachReply = coachReply,
            isTanglishDetected = isTanglish,
            hasGrammarMistake = hasGrammarMistake,
            originalSentence = originalSentence,
            correctedSentence = correctedSentence,
            grammarExplanation = grammarExplanation,
            vocabularySuggestions = vocabList.ifEmpty { null },
            fluencyScore = if (isTanglish) 75 else if (hasGrammarMistake) 78 else 88,
            fluencyFeedback = if (isTanglish) "Nice try! Keep practicing converting Tamil words into English." else "Good flow!",
            pronunciationTip = "Focus on clear pronunciation and steady rhythm.",
            confidenceScore = 85
        )
    }

    /**
     * Gets or generates an AI-tailored Daily Challenge for Day [dayNumber].
     * Analyzes previous practice performance to detect focus areas,
     * calls Gemini API to construct a customized challenge, or falls back to
     * procedural generation, caching the result locally per user.
     */
    suspend fun getOrGenerateDayChallenge(
        dayNumber: Int,
        level: EnglishLevel,
        userId: String
    ): DayChallenge = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences("day_challenges_cache", Context.MODE_PRIVATE)
        val cacheKey = "challenge_${userId}_day_$dayNumber"
        val cachedJson = prefs.getString(cacheKey, null)
        if (!cachedJson.isNullOrBlank()) {
            try {
                val parsed = dayChallengeAiAdapter.fromJson(cachedJson)
                if (parsed != null) {
                    return@withContext DayChallenge(
                        dayNumber = dayNumber,
                        title = parsed.title,
                        subtitle = parsed.subtitle,
                        category = parsed.category,
                        iconName = parsed.iconName.ifBlank { "AutoAwesome" },
                        starterPrompt = parsed.starterPrompt,
                        questions = parsed.questions,
                        keywords = parsed.keywords,
                        sampleAnswerHints = parsed.sampleAnswerHints
                    )
                }
            } catch (e: Exception) {
                Log.w("SpeakUpRepo", "Failed to deserialize cached day challenge: ${e.message}")
            }
        }

        // Analyze user's previous sessions to determine focus areas
        val recentSessions = try {
            sessionDao.getAllSessions(userId).take(5)
        } catch (e: Exception) {
            emptyList()
        }

        var focusArea: String? = null
        if (recentSessions.isNotEmpty()) {
            val avgGrammar = recentSessions.map { it.grammarScore }.average()
            val avgVocab = recentSessions.map { it.vocabularyScore }.average()
            val avgFluency = recentSessions.map { it.fluencyScore }.average()
            focusArea = when {
                avgGrammar < 70 -> "Refining sentence structure, verb tenses and grammar clarity"
                avgVocab < 70 -> "Expanding descriptive vocabulary and word choices"
                avgFluency < 70 -> "Spontaneous storytelling, transitions and smooth speech flow"
                else -> "Expressive communication, storytelling and confidence"
            }
        }

        val apiKey = GeminiClient.getApiKey()
        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val levelDirective = when (level) {
                    EnglishLevel.BEGINNER -> """
                        CRITICAL INSTRUCTION FOR BEGINNER LEVEL:
                        - Questions MUST be very simple, short, and easy to understand.
                        - Absolutely NO difficult, confusing, or abstract words.
                        - Starter prompt must be simple (e.g. 'I like...', 'My name is...').
                        - Hints must be very simple and encouraging.
                    """.trimIndent()
                    EnglishLevel.INTERMEDIATE -> """
                        INSTRUCTION FOR INTERMEDIATE LEVEL:
                        - Questions should be natural conversational questions asking for reasons and examples.
                        - Encourage connecting words like 'because', 'however', and 'also'.
                    """.trimIndent()
                    EnglishLevel.ADVANCED -> """
                        INSTRUCTION FOR ADVANCED LEVEL:
                        - Questions should be thoughtful, reflective, and professional.
                        - Encourage structured monologues with rich vocabulary and rhetorical flow.
                    """.trimIndent()
                }

                val prompt = """
                    You are SpeakUp AI's curriculum creator. Generate a personalized English speaking challenge for Day $dayNumber.
                    User English Level: ${level.displayName}
                    $levelDirective
                    ${if (focusArea != null) "Focus Area based on recent practice performance: $focusArea" else ""}

                    Return a JSON object with this exact schema:
                    {
                      "title": "Day $dayNumber: [Engaging 2-4 word Title]",
                      "subtitle": "Brief 1-2 sentence description motivating the user to speak about this topic tailored for ${level.displayName} level.",
                      "category": "Category name (e.g. Lifestyle, Career, Creative, Travel, Society)",
                      "iconName": "Icon name (e.g. AutoAwesome, Mic, Star, School, Lightbulb, Work, Public)",
                      "starterPrompt": "A starter sentence for the user to begin speaking (e.g., 'When I think about...')",
                      "questions": [
                        "First progressive question to answer",
                        "Second deeper question",
                        "Third reflective/future question"
                      ],
                      "keywords": ["10-15 relevant keywords or phrases to use"],
                      "sampleAnswerHints": [
                        "Tip 1 for speaking well on this topic",
                        "Tip 2 for organizing thoughts",
                        "Tip 3 for grammar/vocabulary"
                      ]
                    }
                """.trimIndent()

                val request = GeminiRequest(
                    contents = listOf(
                        GeminiContent(
                            role = "user",
                            parts = listOf(GeminiPart(text = prompt))
                        )
                    ),
                    generationConfig = GeminiGenerationConfig(
                        temperature = 0.7f,
                        responseMimeType = "application/json"
                    )
                )

                val response = GeminiClient.apiService.generateContent(apiKey, request)
                val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!jsonText.isNullOrBlank()) {
                    val cleanJson = extractJson(jsonText)
                    val parsed = dayChallengeAiAdapter.fromJson(cleanJson)
                    if (parsed != null) {
                        prefs.edit().putString(cacheKey, cleanJson).apply()
                        return@withContext DayChallenge(
                            dayNumber = dayNumber,
                            title = parsed.title,
                            subtitle = parsed.subtitle,
                            category = parsed.category,
                            iconName = parsed.iconName.ifBlank { "AutoAwesome" },
                            starterPrompt = parsed.starterPrompt,
                            questions = parsed.questions,
                            keywords = parsed.keywords,
                            sampleAnswerHints = parsed.sampleAnswerHints
                        )
                    }
                }
            } catch (e: Exception) {
                Log.w("SpeakUpRepo", "Gemini generation for Day $dayNumber failed: ${e.message}")
            }
        }

        // Fallback to procedural generator tailored to level and focus
        val procedural = DayChallengeData.getChallengeForDay(dayNumber, level, focusArea)
        try {
            val jsonToCache = dayChallengeAiAdapter.toJson(
                DayChallengeAiResponse(
                    title = procedural.title,
                    subtitle = procedural.subtitle,
                    category = procedural.category,
                    iconName = procedural.iconName,
                    starterPrompt = procedural.starterPrompt,
                    questions = procedural.questions,
                    keywords = procedural.keywords,
                    sampleAnswerHints = procedural.sampleAnswerHints
                )
            )
            prefs.edit().putString(cacheKey, jsonToCache).apply()
        } catch (e: Exception) {
            // Ignore cache write error
        }
        return@withContext procedural
    }

    private fun extractJson(raw: String): String {
        val trimmed = raw.trim()
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) return trimmed
        val start = trimmed.indexOf('{')
        val end = trimmed.lastIndexOf('}')
        if (start != -1 && end != -1 && end > start) {
            return trimmed.substring(start, end + 1)
        }
        return trimmed
    }

    // ==========================================
    // REAL ACHIEVEMENT PERSISTENCE (Strictly UID-Scoped)
    // ==========================================

    fun getUnlockedAchievements(userId: String): Set<String> {
        val effectiveUid = userId.trim()
        if (effectiveUid.isBlank() || effectiveUid == "default_user") return emptySet()
        val prefs = context.getSharedPreferences("user_achievements_$effectiveUid", Context.MODE_PRIVATE)
        return prefs.getStringSet("unlocked_ids", emptySet()) ?: emptySet()
    }

    fun saveUnlockedAchievement(userId: String, achievementId: String) {
        val effectiveUid = userId.trim()
        if (effectiveUid.isBlank() || effectiveUid == "default_user") return
        val prefs = context.getSharedPreferences("user_achievements_$effectiveUid", Context.MODE_PRIVATE)
        val current = prefs.getStringSet("unlocked_ids", emptySet())?.toMutableSet() ?: mutableSetOf()
        current.add(achievementId)
        prefs.edit().putStringSet("unlocked_ids", current).apply()
    }

    fun getCelebratedAchievements(userId: String): Set<String> {
        val effectiveUid = userId.trim()
        if (effectiveUid.isBlank() || effectiveUid == "default_user") return emptySet()
        val prefs = context.getSharedPreferences("user_achievements_$effectiveUid", Context.MODE_PRIVATE)
        return prefs.getStringSet("celebrated_ids", emptySet()) ?: emptySet()
    }

    fun markAchievementCelebrated(userId: String, achievementId: String) {
        val effectiveUid = userId.trim()
        if (effectiveUid.isBlank() || effectiveUid == "default_user") return
        val prefs = context.getSharedPreferences("user_achievements_$effectiveUid", Context.MODE_PRIVATE)
        val current = prefs.getStringSet("celebrated_ids", emptySet())?.toMutableSet() ?: mutableSetOf()
        current.add(achievementId)
        prefs.edit().putStringSet("celebrated_ids", current).apply()
    }

    /**
     * Evaluates real completed practice sessions and challenges for the CURRENT UID only.
     * Returns any newly unlocked milestone that has not yet been celebrated.
     */
    suspend fun checkForNewMilestoneCelebration(userId: String): com.example.ui.components.CelebrationMilestone? = withContext(Dispatchers.IO) {
        val effectiveUid = userId.trim()
        if (effectiveUid.isBlank() || effectiveUid == "default_user") return@withContext null

        val celebrated = getCelebratedAchievements(effectiveUid)
        val sessions = try { sessionDao.getAllSessions(effectiveUid) } catch (e: Exception) { emptyList() }
        val completedDays = getCompletedDayChallenges(effectiveUid)
        val completedDates = getCompletedPracticeDates(effectiveUid)

        val qualifyingSessions = sessions.filter { it.isTopicRelevant && it.durationSeconds >= 15 && it.overallScore > 0 }
        val totalCompletedCount = (completedDays.size + completedDates.size + qualifyingSessions.size).coerceAtLeast(qualifyingSessions.size)
        val maxFluency = qualifyingSessions.maxOfOrNull { it.fluencyScore } ?: 0
        val maxOverall = qualifyingSessions.maxOfOrNull { it.overallScore } ?: 0

        // 1. One Week Completed (7 days)
        if (totalCompletedCount >= 7 && !celebrated.contains("one_week_completed")) {
            saveUnlockedAchievement(effectiveUid, "one_week_completed")
            return@withContext com.example.ui.components.CelebrationMilestone(
                id = "one_week_completed",
                headline = "WELL DONE! 🎉",
                title = "1 WEEK COMPLETED!",
                description = "Incredible dedication! You have successfully completed 7 real practice sessions. Your speaking habit is firmly taking root!",
                emojiBadge = "🏆",
                subLabel = "7 Days Practice"
            )
        }

        // 2. High Fluency Progress
        if (maxFluency >= 80 && !celebrated.contains("high_fluency_progress")) {
            saveUnlockedAchievement(effectiveUid, "high_fluency_progress")
            return@withContext com.example.ui.components.CelebrationMilestone(
                id = "high_fluency_progress",
                headline = "WELL DONE! 🎉",
                title = "GREAT SPEAKING PROGRESS!",
                description = "You achieved an 80%+ Fluency score in your real practice session! You are expressing ideas with smooth, confident phrasing.",
                emojiBadge = "🗣️",
                subLabel = "Fluency Milestone"
            )
        }

        // 3. High Overall Score Excellence
        if (maxOverall >= 85 && !celebrated.contains("high_score_excellence")) {
            saveUnlockedAchievement(effectiveUid, "high_score_excellence")
            return@withContext com.example.ui.components.CelebrationMilestone(
                id = "high_score_excellence",
                headline = "WELL DONE! 🎉",
                title = "EXCELLENT PERFORMANCE!",
                description = "Outstanding achievement! You earned an 85%+ overall score across grammar, vocabulary, pronunciation, and fluency.",
                emojiBadge = "🌟",
                subLabel = "Speaking Excellence"
            )
        }

        // 4. First Practice Completed
        if (qualifyingSessions.isNotEmpty() && !celebrated.contains("first_session_completed")) {
            saveUnlockedAchievement(effectiveUid, "first_session_completed")
            return@withContext com.example.ui.components.CelebrationMilestone(
                id = "first_session_completed",
                headline = "WELL DONE! 🎉",
                title = "FIRST SESSION COMPLETED!",
                description = "You took the most courageous step and completed your first real speaking session! Keep going strong every practice day.",
                emojiBadge = "🎙️",
                subLabel = "Journey Begun"
            )
        }

        // 5. 3 Days Streak
        if (totalCompletedCount >= 3 && !celebrated.contains("three_days_streak")) {
            saveUnlockedAchievement(effectiveUid, "three_days_streak")
            return@withContext com.example.ui.components.CelebrationMilestone(
                id = "three_days_streak",
                headline = "WELL DONE! 🎉",
                title = "3 DAYS CONSISTENT PRACTICE!",
                description = "You completed 3 real practice sessions! Consistency is transforming your confidence into natural fluency.",
                emojiBadge = "⭐",
                subLabel = "Consistency Star"
            )
        }

        // 6. Two Weeks Completed (14 days)
        if (totalCompletedCount >= 14 && !celebrated.contains("two_weeks_completed")) {
            saveUnlockedAchievement(effectiveUid, "two_weeks_completed")
            return@withContext com.example.ui.components.CelebrationMilestone(
                id = "two_weeks_completed",
                headline = "WELL DONE! 🎉",
                title = "2 WEEKS COMPLETED!",
                description = "14 days of dedicated English speaking practice! Notice how much faster you find words and articulate ideas.",
                emojiBadge = "🔥",
                subLabel = "14 Days Streak"
            )
        }

        null
    }
}
