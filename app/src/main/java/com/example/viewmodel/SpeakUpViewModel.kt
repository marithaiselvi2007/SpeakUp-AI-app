package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.DayChallenge
import com.example.data.model.EnglishLevel
import com.example.data.model.PracticeSession
import com.example.data.model.ThemeColorOption
import com.example.data.model.ThemeModeOption
import com.example.data.model.UserProfile
import com.example.data.remote.FirebaseSafeManager
import com.example.data.repository.SpeakUpRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class DashboardScores(
    val overallScore: Int = 0,
    val grammarScore: Int = 0,
    val vocabularyScore: Int = 0,
    val fluencyScore: Int = 0,
    val pronunciationScore: Int = 0,
    val confidenceScore: Int = 0,
    val totalSessions: Int = 0,
    val totalPracticeMinutes: Int = 0
)

data class DayScore(
    val dayName: String,
    val score: Int,
    val hasSession: Boolean
)

data class WeekScore(
    val weekLabel: String,
    val score: Int,
    val hasSession: Boolean
)

data class MonthScore(
    val monthName: String,
    val score: Int,
    val hasSession: Boolean
)

data class ComparisonResult(
    val previousScore: Int,
    val currentScore: Int,
    val delta: Int,
    val insightText: String,
    val hasSufficientData: Boolean
)

data class PracticeDaysProgress(
    val completedCount: Int,
    val totalSelectedDays: Int,
    val selectedDaysList: List<String>,
    val completedDaysList: List<String>
)

enum class DailyChallengeStatus {
    PRACTICE_REQUIRED,
    READY_TO_START,
    COMPLETED_TODAY
}

class SpeakUpViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SpeakUpRepository(application)
    private val auth get() = FirebaseSafeManager.getAuth(getApplication())

    private val _userId = MutableStateFlow(auth?.currentUser?.uid?.trim().orEmpty())
    val currentUserId: StateFlow<String> = _userId.asStateFlow()

    private val _themeColor = MutableStateFlow(ThemeColorOption.BLUE)
    val themeColor: StateFlow<ThemeColorOption> = _themeColor.asStateFlow()

    private val _themeMode = MutableStateFlow(ThemeModeOption.SYSTEM)
    val themeMode: StateFlow<ThemeModeOption> = _themeMode.asStateFlow()

    private val _hasSpokenWelcomeGreeting = MutableStateFlow(false)
    val hasSpokenWelcomeGreeting: StateFlow<Boolean> = _hasSpokenWelcomeGreeting.asStateFlow()

    fun markWelcomeGreetingSpoken() {
        _hasSpokenWelcomeGreeting.value = true
    }

    fun resetWelcomeGreeting() {
        _hasSpokenWelcomeGreeting.value = false
    }

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _completedDayChallenges = MutableStateFlow<Set<Int>>(emptySet())
    val completedDayChallenges: StateFlow<Set<Int>> = _completedDayChallenges.asStateFlow()

    private val _isTodaySpeakingPracticeCompleted = MutableStateFlow(false)
    val isTodaySpeakingPracticeCompleted: StateFlow<Boolean> = _isTodaySpeakingPracticeCompleted.asStateFlow()

    private val _isTodayDailyChallengeCompleted = MutableStateFlow(false)
    val isTodayDailyChallengeCompleted: StateFlow<Boolean> = _isTodayDailyChallengeCompleted.asStateFlow()

    private val _streakMilestoneCelebration = MutableStateFlow<Int?>(null)
    val streakMilestoneCelebration: StateFlow<Int?> = _streakMilestoneCelebration.asStateFlow()

    fun dismissStreakCelebration() {
        _streakMilestoneCelebration.value = null
    }

    private val _sessions = MutableStateFlow<List<PracticeSession>>(emptyList())
    val sessions: StateFlow<List<PracticeSession>> = _sessions.asStateFlow()

    private val _completedPracticeDates = MutableStateFlow<Set<String>>(emptySet())
    val completedPracticeDates: StateFlow<Set<String>> = _completedPracticeDates.asStateFlow()

    private val _practiceDaysProgress = MutableStateFlow(
        PracticeDaysProgress(0, 0, emptyList(), emptyList())
    )
    val practiceDaysProgress: StateFlow<PracticeDaysProgress> = _practiceDaysProgress.asStateFlow()

    private val _todayDailyChallenge = MutableStateFlow<DayChallenge?>(null)
    val todayDailyChallenge: StateFlow<DayChallenge?> = _todayDailyChallenge.asStateFlow()

    private val _nextChallengePreparedEvent = MutableStateFlow<String?>(null)
    val nextChallengePreparedEvent: StateFlow<String?> = _nextChallengePreparedEvent.asStateFlow()

    fun dismissNextChallengePreparedEvent() {
        _nextChallengePreparedEvent.value = null
    }

    private val _activeCelebrationMilestone = MutableStateFlow<com.example.ui.components.CelebrationMilestone?>(null)
    val activeCelebrationMilestone: StateFlow<com.example.ui.components.CelebrationMilestone?> = _activeCelebrationMilestone.asStateFlow()

    fun dismissCelebration() {
        val milestone = _activeCelebrationMilestone.value
        val uid = _userId.value.trim().ifBlank { auth?.currentUser?.uid?.trim().orEmpty() }
        if (milestone != null && uid.isNotBlank()) {
            repository.markAchievementCelebrated(uid, milestone.id)
        }
        _activeCelebrationMilestone.value = null
    }

    fun checkForCelebrationMilestones(userId: String = "") {
        val uid = userId.trim().ifBlank { _userId.value.trim().ifBlank { auth?.currentUser?.uid?.trim().orEmpty() } }
        if (uid.isBlank() || uid == "default_user") return
        viewModelScope.launch {
            try {
                val newMilestone = repository.checkForNewMilestoneCelebration(uid)
                if (newMilestone != null && _activeCelebrationMilestone.value == null) {
                    _activeCelebrationMilestone.value = newMilestone
                }
            } catch (e: Exception) {
                // Ignore milestone check error
            }
        }
    }

    val currentChallengeDayNumber: StateFlow<Int> = combine(
        _completedDayChallenges,
        _isTodayDailyChallengeCompleted
    ) { completed, todayDone ->
        val maxCompleted = if (completed.isEmpty()) 0 else completed.maxOrNull() ?: 0
        if (todayDone) {
            maxCompleted.coerceAtLeast(1)
        } else {
            maxCompleted + 1
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)

    val dailyChallengeStatus: StateFlow<DailyChallengeStatus> = combine(
        _isTodayDailyChallengeCompleted,
        _isTodaySpeakingPracticeCompleted,
        _userProfile
    ) { todayChallengeDone, todayPracticeDone, profile ->
        if (todayChallengeDone) {
            DailyChallengeStatus.COMPLETED_TODAY
        } else if (todayPracticeDone) {
            DailyChallengeStatus.READY_TO_START
        } else {
            DailyChallengeStatus.PRACTICE_REQUIRED
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DailyChallengeStatus.PRACTICE_REQUIRED)

    val dashboardScores: StateFlow<DashboardScores> = _sessions.combine(_userProfile) { sessionList, _ ->
        if (sessionList.isEmpty()) {
            DashboardScores()
        } else {
            val total = sessionList.size
            val avgOverall = sessionList.map { it.overallScore }.average().toInt()
            val avgGrammar = sessionList.map { it.grammarScore }.average().toInt()
            val avgVocab = sessionList.map { it.vocabularyScore }.average().toInt()
            val avgFluency = sessionList.map { it.fluencyScore }.average().toInt()
            val avgPron = sessionList.map { it.pronunciationScore }.average().toInt()
            val avgConf = sessionList.map { it.confidenceScore }.average().toInt()
            val totalMinutes = (sessionList.sumOf { it.durationSeconds } / 60).toInt()

            DashboardScores(
                overallScore = avgOverall,
                grammarScore = avgGrammar,
                vocabularyScore = avgVocab,
                fluencyScore = avgFluency,
                pronunciationScore = avgPron,
                confidenceScore = avgConf,
                totalSessions = total,
                totalPracticeMinutes = totalMinutes
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardScores())

    private var profileJob: kotlinx.coroutines.Job? = null
    private var sessionsJob: kotlinx.coroutines.Job? = null

    init {
        val currentUid = auth?.currentUser?.uid?.trim().orEmpty()
        if (currentUid.isNotBlank()) {
            _userId.value = currentUid
            refreshData()
        }
    }

    fun updateCurrentUserId(uid: String) {
        val cleanUid = uid.trim()
        if (cleanUid.isBlank() || cleanUid == "default_user") {
            clearUserData()
            return
        }
        val changed = _userId.value != cleanUid
        _userId.value = cleanUid
        if (changed) {
            profileJob?.cancel()
            profileJob = null
            sessionsJob?.cancel()
            sessionsJob = null
            // Reset in-memory states immediately to prevent state leakage from previous account
            _completedDayChallenges.value = emptySet()
            _completedPracticeDates.value = emptySet()
            _practiceDaysProgress.value = PracticeDaysProgress(0, 0, emptyList(), emptyList())
            _isTodayDailyChallengeCompleted.value = false
            _isTodaySpeakingPracticeCompleted.value = false
            _todayDailyChallenge.value = null
            _sessions.value = emptyList()
            _userProfile.value = null
            _hasSpokenWelcomeGreeting.value = false
            _streakMilestoneCelebration.value = null
            _nextChallengePreparedEvent.value = null
            _activeCelebrationMilestone.value = null
        }
        refreshData()
    }

    fun clearUserData() {
        profileJob?.cancel()
        profileJob = null
        sessionsJob?.cancel()
        sessionsJob = null
        _userId.value = ""
        _userProfile.value = null
        _completedDayChallenges.value = emptySet()
        _completedPracticeDates.value = emptySet()
        _practiceDaysProgress.value = PracticeDaysProgress(0, 0, emptyList(), emptyList())
        _isTodayDailyChallengeCompleted.value = false
        _isTodaySpeakingPracticeCompleted.value = false
        _todayDailyChallenge.value = null
        _sessions.value = emptyList()
        _hasSpokenWelcomeGreeting.value = false
        _streakMilestoneCelebration.value = null
        _nextChallengePreparedEvent.value = null
        _activeCelebrationMilestone.value = null
    }

    fun refreshData() {
        val uid = _userId.value.trim().ifBlank { auth?.currentUser?.uid?.trim().orEmpty() }
        if (uid.isBlank() || uid == "default_user") {
            clearUserData()
            return
        }
        _userId.value = uid

        // 1. Immediately populate from user-specific local repository cache
        val localCompleted = repository.getCompletedDayChallenges(uid)
        _completedDayChallenges.value = localCompleted
        _isTodayDailyChallengeCompleted.value = repository.hasCompletedDailyChallengeToday(uid)
        val localPracticeDates = repository.getCompletedPracticeDates(uid)
        _completedPracticeDates.value = localPracticeDates
        val localSelectedDays = repository.getSelectedPracticeDays(uid) ?: emptyList()
        _practiceDaysProgress.value = calculatePracticeDaysProgress(localSelectedDays, localPracticeDates)
        loadTodayDailyChallenge(uid)

        // Asynchronously check speaking practice completion immediately for this specific user
        viewModelScope.launch(Dispatchers.IO) {
            val isPracticeDone = repository.hasCompletedSpeakingPracticeToday(uid)
            withContext(Dispatchers.Main) {
                if (_userId.value == uid) {
                    _isTodaySpeakingPracticeCompleted.value = isPracticeDone
                }
            }
        }

        // 2. Proactively synchronize user profile, DB sessions and Firestore daily challenges
        viewModelScope.launch {
            repository.syncUserData(uid)
            if (_userId.value != uid) return@launch

            val dbCompleted = repository.syncCompletedDayChallengesWithDb(uid)
            if (_userId.value == uid) _completedDayChallenges.value = dbCompleted

            val syncedPracticeDates = repository.syncCompletedPracticeDates(uid)
            if (_userId.value == uid) _completedPracticeDates.value = syncedPracticeDates

            updatePracticeDaysProgressState()

            val isPracticeDone = repository.hasCompletedSpeakingPracticeToday(uid)
            if (_userId.value == uid) _isTodaySpeakingPracticeCompleted.value = isPracticeDone

            val isChallengeDone = repository.hasCompletedDailyChallengeToday(uid)
            if (_userId.value == uid) _isTodayDailyChallengeCompleted.value = isChallengeDone

            loadTodayDailyChallenge(uid)
        }
        
        // Check if user has achieved a 7-day milestone (7, 14, 21, 28...)
        if (localCompleted.isNotEmpty() && localCompleted.size % 7 == 0) {
            val milestone = localCompleted.size
            val prefs = getApplication<Application>().getSharedPreferences("speakup_milestone_prefs", android.content.Context.MODE_PRIVATE)
            val lastCelebrated = prefs.getInt("last_celebrated_${uid}_milestone", 0)
            if (lastCelebrated < milestone) {
                _streakMilestoneCelebration.value = milestone
                prefs.edit().putInt("last_celebrated_${uid}_milestone", milestone).apply()
            }
        }
        
        profileJob?.cancel()
        profileJob = viewModelScope.launch {
            try {
                repository.getUserProfileFlow(uid).collect { profile ->
                    if (_userId.value != uid) return@collect
                    _userProfile.value = profile
                    if (profile != null) {
                        _themeColor.value = profile.themeColor
                        _themeMode.value = profile.themeMode
                    }
                    updatePracticeDaysProgressState()
                }
            } catch (e: Throwable) {
                android.util.Log.w("SpeakUpVM", "Profile flow collection error: ${e.message}")
            }
        }

        sessionsJob?.cancel()
        sessionsJob = viewModelScope.launch {
            try {
                repository.getSessionsFlow(uid).collect { sessionList ->
                    if (_userId.value != uid) return@collect
                    _sessions.value = sessionList
                    val updatedCompleted = repository.getCompletedDayChallenges(uid)
                    _completedDayChallenges.value = updatedCompleted
                    _isTodayDailyChallengeCompleted.value = repository.hasCompletedDailyChallengeToday(uid)
                    val isPracticeDone = repository.hasCompletedSpeakingPracticeToday(uid)
                    _isTodaySpeakingPracticeCompleted.value = isPracticeDone
                    val dates = repository.getCompletedPracticeDates(uid)
                    _completedPracticeDates.value = dates
                    updatePracticeDaysProgressState()
                    loadTodayDailyChallenge(uid)
                }
            } catch (e: Throwable) {
                android.util.Log.w("SpeakUpVM", "Sessions flow collection error: ${e.message}")
            }
        }
    }

    private fun loadTodayDailyChallenge(uid: String) {
        viewModelScope.launch {
            try {
                val isTodayDone = repository.hasCompletedDailyChallengeToday(uid)
                val completed = repository.getCompletedDayChallenges(uid)
                val maxCompleted = if (completed.isEmpty()) 0 else completed.maxOrNull() ?: 0
                val dayNum = if (isTodayDone) {
                    maxCompleted.coerceAtLeast(1)
                } else {
                    maxCompleted + 1
                }
                // Adapt level dynamically to user's real progress and performance history
                val adaptiveLevel = repository.evaluateAndAdaptEnglishLevel(uid)
                    ?: _userProfile.value?.englishLevel
                    ?: EnglishLevel.BEGINNER
                val challenge = repository.getOrGenerateDayChallenge(dayNum, adaptiveLevel, uid)
                _todayDailyChallenge.value = challenge
            } catch (e: Exception) {
                android.util.Log.w("SpeakUpVM", "Error loading today challenge: ${e.message}")
            }
        }
    }

    fun prepareNextDayChallenge() {
        val uid = _userId.value.trim().ifBlank { auth?.currentUser?.uid?.trim().orEmpty() }
        if (uid.isBlank()) return
        viewModelScope.launch {
            try {
                val completed = repository.getCompletedDayChallenges(uid)
                val maxCompleted = if (completed.isEmpty()) 0 else completed.maxOrNull() ?: 0
                val nextDayNum = maxCompleted + 1
                val adaptiveLevel = repository.evaluateAndAdaptEnglishLevel(uid)
                    ?: _userProfile.value?.englishLevel
                    ?: EnglishLevel.BEGINNER
                
                // Pre-generate and cache the AI adaptive challenge for the next practice day
                val challenge = repository.getOrGenerateDayChallenge(nextDayNum, adaptiveLevel, uid)
                _nextChallengePreparedEvent.value = "Day $nextDayNum Challenge ('${challenge.title}') is prepared! It will unlock on your next practice day."
            } catch (e: Exception) {
                val completed = repository.getCompletedDayChallenges(uid)
                val maxCompleted = if (completed.isEmpty()) 0 else completed.maxOrNull() ?: 0
                val nextDayNum = maxCompleted + 1
                _nextChallengePreparedEvent.value = "Day $nextDayNum Challenge is prepared! It will unlock on your next practice day."
            }
        }
    }

    fun calculatePracticeDaysProgress(
        selectedDays: List<String>,
        completedDates: Set<String>
    ): PracticeDaysProgress {
        val normalizedSelected = selectedDays.map { com.example.data.repository.SpeakUpRepository.normalizeDayName(it) }.distinct()

        val weekCal = Calendar.getInstance(Locale.ENGLISH).apply {
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            val dayOfWeek = get(Calendar.DAY_OF_WEEK)
            val daysSinceMonday = if (dayOfWeek == Calendar.SUNDAY) 6 else (dayOfWeek - Calendar.MONDAY)
            add(Calendar.DAY_OF_MONTH, -daysSinceMonday)
        }

        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val weekDayMap = mutableMapOf<String, String>()
        val dayNames = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

        for (i in 0 until 7) {
            val dateStr = dateFormat.format(weekCal.time)
            weekDayMap[dayNames[i]] = dateStr
            weekCal.add(Calendar.DAY_OF_MONTH, 1)
        }

        // Only count a day as completed if it was selected AND real practice occurred on that day's date this week
        val completedDaysThisWeek = normalizedSelected.filter { standardDayName ->
            val dateForDay = weekDayMap[standardDayName]
            dateForDay != null && completedDates.contains(dateForDay)
        }

        return PracticeDaysProgress(
            completedCount = completedDaysThisWeek.size,
            totalSelectedDays = normalizedSelected.size,
            selectedDaysList = normalizedSelected,
            completedDaysList = completedDaysThisWeek
        )
    }

    fun updatePracticeDaysProgressState() {
        val uid = _userId.value.trim().ifBlank { auth?.currentUser?.uid?.trim().orEmpty() }
        val cachedDays = if (uid.isNotBlank()) repository.getSelectedPracticeDays(uid) else null
        val profileDays = _userProfile.value?.practiceDays
        val selected = cachedDays ?: profileDays?.map { com.example.data.repository.SpeakUpRepository.normalizeDayName(it) }?.distinct() ?: emptyList()
        val dates = _completedPracticeDates.value
        _practiceDaysProgress.value = calculatePracticeDaysProgress(selected, dates)
    }

    fun recordPracticeSessionCompleted(userId: String = "", timestamp: Long = System.currentTimeMillis()) {
        val uid = userId.trim().ifBlank { _userId.value.trim().ifBlank { auth?.currentUser?.uid?.trim().orEmpty() } }
        if (uid.isBlank()) return
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val dateStr = sdf.format(timestamp)
        repository.recordPracticeDateCompleted(uid, dateStr)

        val currentDates = _completedPracticeDates.value.toMutableSet()
        currentDates.add(dateStr)
        _completedPracticeDates.value = currentDates
        _isTodaySpeakingPracticeCompleted.value = true
        updatePracticeDaysProgressState()
        checkForCelebrationMilestones(uid)
    }

    fun getPracticeDaysProgress(): PracticeDaysProgress {
        return _practiceDaysProgress.value
    }

    fun isDayUnlocked(dayNumber: Int): Boolean {
        val uid = _userId.value.trim().ifBlank { auth?.currentUser?.uid?.trim().orEmpty() }
        val practiceDays = _userProfile.value?.practiceDays ?: emptyList()
        return repository.isDayUnlocked(dayNumber, uid, practiceDays)
    }

    fun isDayCompleted(dayNumber: Int): Boolean {
        val uid = _userId.value.trim().ifBlank { auth?.currentUser?.uid?.trim().orEmpty() }
        return repository.isDayChallengeCompleted(dayNumber, uid)
    }

    fun getDayChallengeBestScore(dayNumber: Int): Int? {
        val uid = _userId.value.trim().ifBlank { auth?.currentUser?.uid?.trim().orEmpty() }
        return repository.getDayChallengeBestScore(uid, dayNumber)
    }

    fun setThemeColor(color: ThemeColorOption) {
        _themeColor.value = color
        val current = _userProfile.value ?: return
        viewModelScope.launch {
            val updated = current.copy(themeColor = color, updatedAt = System.currentTimeMillis())
            repository.saveUserProfile(updated)
            _userProfile.value = updated
        }
    }

    fun setThemeMode(mode: ThemeModeOption) {
        _themeMode.value = mode
        val current = _userProfile.value ?: return
        viewModelScope.launch {
            val updated = current.copy(themeMode = mode, updatedAt = System.currentTimeMillis())
            repository.saveUserProfile(updated)
            _userProfile.value = updated
        }
    }

    fun updateProfileInfo(name: String, level: EnglishLevel? = null, dailyGoal: Int, preferredTopics: List<String>) {
        val current = _userProfile.value ?: return
        viewModelScope.launch {
            val updated = current.copy(
                name = name,
                englishLevel = level ?: current.englishLevel,
                dailyGoalMinutes = dailyGoal,
                preferredTopics = preferredTopics,
                updatedAt = System.currentTimeMillis()
            )
            repository.saveUserProfile(updated)
            _userProfile.value = updated
        }
    }

    fun updateScheduleSettings(
        practiceDays: List<String>,
        preferredPracticeTime: String,
        sessionDurationMinutes: Int,
        dailyGoalMinutes: Int
    ) {
        val current = _userProfile.value ?: return
        viewModelScope.launch {
            val updated = current.copy(
                practiceDays = practiceDays,
                preferredPracticeTime = preferredPracticeTime,
                sessionDurationMinutes = sessionDurationMinutes,
                dailyGoalMinutes = dailyGoalMinutes,
                updatedAt = System.currentTimeMillis()
            )
            repository.saveUserProfile(updated)
            _userProfile.value = updated
        }
    }

    fun updatePracticeDays(days: List<String>) {
        val uid = auth?.currentUser?.uid ?: _userId.value
        val normalized = days.map { com.example.data.repository.SpeakUpRepository.normalizeDayName(it) }.distinct()
        
        // 1. Immediately save synchronously to repository cache
        repository.saveSelectedPracticeDays(uid, normalized)

        // 2. Update user profile state
        val current = _userProfile.value ?: UserProfile(uid = uid, practiceDays = normalized)
        val updated = current.copy(practiceDays = normalized, updatedAt = System.currentTimeMillis())
        _userProfile.value = updated

        // 3. Immediately re-calculate practiceDaysProgress so UI reacts synchronously
        updatePracticeDaysProgressState()

        // 4. Update alarm notification schedule
        val isAlarmOn = updated.isPracticeAlarmEnabled
        val time = updated.preferredPracticeTime
        com.example.notification.ScheduleNotificationManager.schedulePracticeReminder(
            context = getApplication(),
            preferredTime = time,
            practiceDays = normalized,
            isEnabled = isAlarmOn,
            userId = uid
        )

        // 5. Persist to Room and Firestore
        viewModelScope.launch {
            repository.saveUserProfile(updated)
        }
    }

    fun updatePreferredPracticeTime(time: String) {
        val current = _userProfile.value ?: return
        viewModelScope.launch {
            val updated = current.copy(preferredPracticeTime = time, updatedAt = System.currentTimeMillis())
            repository.saveUserProfile(updated)
            _userProfile.value = updated
        }
    }

    fun updatePracticeAlarmEnabled(enabled: Boolean) {
        val current = _userProfile.value ?: return
        viewModelScope.launch {
            val updated = current.copy(isPracticeAlarmEnabled = enabled, updatedAt = System.currentTimeMillis())
            repository.saveUserProfile(updated)
            _userProfile.value = updated
        }
    }

    fun updateSessionDuration(durationMinutes: Int) {
        val current = _userProfile.value ?: return
        viewModelScope.launch {
            val updated = current.copy(sessionDurationMinutes = durationMinutes, updatedAt = System.currentTimeMillis())
            repository.saveUserProfile(updated)
            _userProfile.value = updated
        }
    }

    fun updateDailyGoal(goalMinutes: Int) {
        val current = _userProfile.value ?: return
        viewModelScope.launch {
            val updated = current.copy(dailyGoalMinutes = goalMinutes, updatedAt = System.currentTimeMillis())
            repository.saveUserProfile(updated)
            _userProfile.value = updated
        }
    }

    fun updateProfilePhoto(photoUrl: String) {
        val current = _userProfile.value ?: UserProfile(uid = _userId.value)
        viewModelScope.launch {
            val updated = current.copy(photoUrl = photoUrl, updatedAt = System.currentTimeMillis())
            repository.saveUserProfile(updated)
            _userProfile.value = updated
        }
    }

    companion object {
        val defaultScheduledDays = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    }

    // Helper to parse day of week name (handles "Monday", "Mon", lowercase, etc.)
    fun parseDayOfWeek(dayStr: String): Int? {
        val clean = dayStr.trim().lowercase(Locale.ENGLISH)
        return when {
            clean.startsWith("mon") -> Calendar.MONDAY
            clean.startsWith("tue") -> Calendar.TUESDAY
            clean.startsWith("wed") -> Calendar.WEDNESDAY
            clean.startsWith("thu") -> Calendar.THURSDAY
            clean.startsWith("fri") -> Calendar.FRIDAY
            clean.startsWith("sat") -> Calendar.SATURDAY
            clean.startsWith("sun") -> Calendar.SUNDAY
            else -> null
        }
    }

    fun getCompletedDayOfWeekIntsInCurrentWeek(sessionList: List<PracticeSession> = _sessions.value): Set<Int> {
        if (sessionList.isEmpty()) return emptySet()

        val weekCal = Calendar.getInstance(Locale.ENGLISH).apply {
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            val dayOfWeek = get(Calendar.DAY_OF_WEEK)
            val daysSinceMonday = if (dayOfWeek == Calendar.SUNDAY) 6 else (dayOfWeek - Calendar.MONDAY)
            add(Calendar.DAY_OF_MONTH, -daysSinceMonday)
        }
        val weekStartTime = weekCal.timeInMillis
        val weekEndTime = weekStartTime + (7 * 24 * 60 * 60 * 1000L)

        val sessionCal = Calendar.getInstance(Locale.ENGLISH)
        val completedDays = mutableSetOf<Int>()

        for (session in sessionList) {
            val time = if (session.startTime > 0) session.startTime else session.endTime
            if (time in weekStartTime until weekEndTime) {
                sessionCal.timeInMillis = time
                completedDays.add(sessionCal.get(Calendar.DAY_OF_WEEK))
            }
        }

        return completedDays
    }

    // Calculate unique completed practice days in current weekly cycle based on user's selected days
    fun getCompletedDaysInCurrentWeek(
        scheduledDays: List<String>? = null,
        sessionList: List<PracticeSession> = _sessions.value
    ): Int {
        val targetDays = scheduledDays ?: _practiceDaysProgress.value.selectedDaysList.ifEmpty { _userProfile.value?.practiceDays ?: emptyList() }
        if (targetDays.isEmpty()) return 0

        val completedInWeek = getCompletedDayNamesInCurrentWeek(sessionList)
        return targetDays.count { dayName ->
            completedInWeek.any { it.equals(dayName, ignoreCase = true) }
        }
    }

    fun getCompletedDayNamesInCurrentWeek(sessionList: List<PracticeSession> = _sessions.value): Set<String> {
        val progressList = _practiceDaysProgress.value.completedDaysList
        if (progressList.isNotEmpty()) {
            return progressList.toSet()
        }
        val dayOfWeekInts = getCompletedDayOfWeekIntsInCurrentWeek(sessionList)
        return dayOfWeekInts.mapNotNull { dayInt ->
            when (dayInt) {
                Calendar.MONDAY -> "Monday"
                Calendar.TUESDAY -> "Tuesday"
                Calendar.WEDNESDAY -> "Wednesday"
                Calendar.THURSDAY -> "Thursday"
                Calendar.FRIDAY -> "Friday"
                Calendar.SATURDAY -> "Saturday"
                Calendar.SUNDAY -> "Sunday"
                else -> null
            }
        }.toSet()
    }

    // Daily Progress (Monday to Sunday of current week with full day names)
    fun getDailyProgress(sessionList: List<PracticeSession> = _sessions.value): List<DayScore> {
        val dayNames = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        val weekCal = Calendar.getInstance(Locale.ENGLISH).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            val dayOfWeek = get(Calendar.DAY_OF_WEEK)
            val daysSinceMonday = if (dayOfWeek == Calendar.SUNDAY) 6 else (dayOfWeek - Calendar.MONDAY)
            add(Calendar.DAY_OF_MONTH, -daysSinceMonday)
        }
        val weekStartTime = weekCal.timeInMillis
        val weekEndTime = weekStartTime + (7 * 24 * 60 * 60 * 1000L)

        val sessionsThisWeek = sessionList.filter { it.startTime in weekStartTime until weekEndTime }
        val sessionCal = Calendar.getInstance(Locale.ENGLISH)

        return (0..6).map { dayIndex ->
            val targetDayOfWeek = when (dayIndex) {
                0 -> Calendar.MONDAY
                1 -> Calendar.TUESDAY
                2 -> Calendar.WEDNESDAY
                3 -> Calendar.THURSDAY
                4 -> Calendar.FRIDAY
                5 -> Calendar.SATURDAY
                else -> Calendar.SUNDAY
            }

            val matchingSessions = sessionsThisWeek.filter { session ->
                sessionCal.timeInMillis = session.startTime
                sessionCal.get(Calendar.DAY_OF_WEEK) == targetDayOfWeek
            }

            if (matchingSessions.isNotEmpty()) {
                val avg = matchingSessions.map { it.overallScore }.average().toInt()
                DayScore(dayName = dayNames[dayIndex], score = avg, hasSession = true)
            } else {
                DayScore(dayName = dayNames[dayIndex], score = 0, hasSession = false)
            }
        }
    }

    // Weekly Progress (Week 1 to Week 4)
    fun getWeeklyProgress(): List<WeekScore> {
        val sessionList = _sessions.value
        val labels = listOf("Week 1", "Week 2", "Week 3", "Week 4")
        val now = System.currentTimeMillis()
        val oneWeekMillis = 7 * 24 * 60 * 60 * 1000L

        return (0..3).map { i ->
            val weekEnd = now - (3 - i) * oneWeekMillis
            val weekStart = weekEnd - oneWeekMillis

            val weekSessions = sessionList.filter { it.startTime in weekStart..weekEnd }
            if (weekSessions.isNotEmpty()) {
                val avg = weekSessions.map { it.overallScore }.average().toInt()
                WeekScore(weekLabel = labels[i], score = avg, hasSession = true)
            } else {
                WeekScore(weekLabel = labels[i], score = 0, hasSession = false)
            }
        }
    }

    // Monthly Progress (January to December with full month names)
    fun getMonthlyProgress(): List<MonthScore> {
        val sessionList = _sessions.value
        val months = listOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
        val calendar = Calendar.getInstance(Locale.ENGLISH)

        return (0..11).map { monthIndex ->
            val matching = sessionList.filter {
                calendar.timeInMillis = it.startTime
                calendar.get(Calendar.MONTH) == monthIndex
            }
            if (matching.isNotEmpty()) {
                val avg = matching.map { it.overallScore }.average().toInt()
                MonthScore(monthName = months[monthIndex], score = avg, hasSession = true)
            } else {
                MonthScore(monthName = months[monthIndex], score = 0, hasSession = false)
            }
        }
    }

    // Progress Comparison between latest session and previous session
    fun getProgressComparison(): ComparisonResult {
        val sessionList = _sessions.value
        if (sessionList.size < 2) {
            val currentScore = sessionList.firstOrNull()?.overallScore ?: 0
            return ComparisonResult(
                previousScore = 0,
                currentScore = currentScore,
                delta = 0,
                insightText = if (sessionList.isEmpty()) {
                    "Start your first practice session to track progress."
                } else {
                    "Complete one more practice session to see your progress comparison."
                },
                hasSufficientData = false
            )
        }

        val sorted = sessionList.sortedByDescending { it.startTime }
        val current = sorted[0]
        val previous = sorted[1]
        val delta = current.overallScore - previous.overallScore

        val text = when {
            delta > 0 -> "Great job! Your performance improved by $delta% compared to your previous session."
            delta < 0 -> "Your score changed by $delta%. Consistent daily speaking practice will boost your fluency!"
            else -> "You maintained a solid score of ${current.overallScore}%. Keep challenging yourself!"
        }

        return ComparisonResult(
            previousScore = previous.overallScore,
            currentScore = current.overallScore,
            delta = delta,
            insightText = text,
            hasSufficientData = true
        )
    }

    /**
     * Calculates real consecutive practice days without fake or simulated entries.
     */
    fun getStreakDays(sessionList: List<PracticeSession> = _sessions.value): Int {
        if (sessionList.isEmpty()) return 0
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val distinctDates = sessionList.map { sdf.format(Date(it.startTime)) }.distinct().sortedDescending()
        if (distinctDates.isEmpty()) return 0

        val calendar = Calendar.getInstance(Locale.ENGLISH)
        val todayStr = sdf.format(calendar.time)
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        val yesterdayStr = sdf.format(calendar.time)

        // If no session today or yesterday, streak is broken
        if (!distinctDates.contains(todayStr) && !distinctDates.contains(yesterdayStr)) {
            return 0
        }

        var streak = 0
        var checkCal = Calendar.getInstance(Locale.ENGLISH)
        if (!distinctDates.contains(todayStr)) {
            checkCal.add(Calendar.DAY_OF_MONTH, -1)
        }

        while (true) {
            val dateStr = sdf.format(checkCal.time)
            if (distinctDates.contains(dateStr)) {
                streak++
                checkCal.add(Calendar.DAY_OF_MONTH, -1)
            } else {
                break
            }
        }
        return streak
    }

    override fun onCleared() {
        super.onCleared()
        profileJob?.cancel()
        sessionsJob?.cancel()
    }
}
