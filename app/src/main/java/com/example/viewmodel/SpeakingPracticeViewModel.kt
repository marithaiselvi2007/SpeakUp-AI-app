package com.example.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.EnglishLevel
import com.example.data.model.GrammarCorrection
import com.example.data.model.PracticeSession
import com.example.data.model.PracticeTopic
import com.example.data.model.VocabularySuggestion
import com.example.data.repository.SpeakUpRepository
import com.example.data.repository.TopicsData
import com.example.speech.SpeechRecognizerHelper
import com.example.speech.SpeechState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import com.example.data.remote.FirebaseSafeManager
import com.example.data.remote.SpeakingPracticeReportResponse
import java.util.UUID

class SpeakingPracticeViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        @Volatile
        var isActivelyPracticing: Boolean = false
    }

    private val repository = SpeakUpRepository(application)
    private val speechRecognizer = SpeechRecognizerHelper(application)

    private val _currentTopic = MutableStateFlow<PracticeTopic>(TopicsData.allTopics.first())
    val currentTopic: StateFlow<PracticeTopic> = _currentTopic.asStateFlow()

    private val _dayNumber = MutableStateFlow<Int?>(null)
    val dayNumber: StateFlow<Int?> = _dayNumber.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()

    private val _userLevel = MutableStateFlow(EnglishLevel.BEGINNER)
    val userLevel: StateFlow<EnglishLevel> = _userLevel.asStateFlow()

    private val _currentUserId = MutableStateFlow("guest_user")
    val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()

    private val _targetDurationMinutes = MutableStateFlow(2)
    val targetDurationMinutes: StateFlow<Int> = _targetDurationMinutes.asStateFlow()

    // Speaking state
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    private val _actualSpeakingSeconds = MutableStateFlow(0L)
    val actualSpeakingSeconds: StateFlow<Long> = _actualSpeakingSeconds.asStateFlow()

    // Verbatim original transcript as recognized
    private val _transcript = MutableStateFlow("")
    val transcript: StateFlow<String> = _transcript.asStateFlow()

    private val _partialTranscript = MutableStateFlow("")
    val partialTranscript: StateFlow<String> = _partialTranscript.asStateFlow()

    val speechState: StateFlow<SpeechState> = speechRecognizer.speechState
    val speechRmsDb: StateFlow<Float> = speechRecognizer.rmsDb

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _completedReport = MutableStateFlow<PracticeSession?>(null)
    val completedReport: StateFlow<PracticeSession?> = _completedReport.asStateFlow()

    private val _showFinishConfirmDialog = MutableStateFlow(false)
    val showFinishConfirmDialog: StateFlow<Boolean> = _showFinishConfirmDialog.asStateFlow()

    private val _showReportDialog = MutableStateFlow(false)
    val showReportDialog: StateFlow<Boolean> = _showReportDialog.asStateFlow()

    private val _showIncompleteDurationNotice = MutableStateFlow(false)
    val showIncompleteDurationNotice: StateFlow<Boolean> = _showIncompleteDurationNotice.asStateFlow()

    private val _showOffTopicNotice = MutableStateFlow(false)
    val showOffTopicNotice: StateFlow<Boolean> = _showOffTopicNotice.asStateFlow()

    private val _offTopicReport = MutableStateFlow<SpeakingPracticeReportResponse?>(null)
    val offTopicReport: StateFlow<SpeakingPracticeReportResponse?> = _offTopicReport.asStateFlow()

    private val _isTimerFinished = MutableStateFlow(false)
    val isTimerFinished: StateFlow<Boolean> = _isTimerFinished.asStateFlow()

    private var timerJob: Job? = null

    init {
        speechRecognizer.onFinalResult = { text ->
            if (text.isNotBlank()) {
                val current = _transcript.value
                val updated = if (current.isBlank()) text.trim() else "$current $text".trim()
                _transcript.value = updated
                _partialTranscript.value = ""
            }
            // Auto restart listening if still in active recording state
            if (_isRecording.value && !_isPaused.value) {
                viewModelScope.launch {
                    delay(250)
                    if (_isRecording.value && !_isPaused.value) {
                        speechRecognizer.startListening()
                    }
                }
            }
        }

        speechRecognizer.onPartialResult = { partial ->
            _partialTranscript.value = partial
        }

        speechRecognizer.onErrorCallback = { errorCode, errorMsg ->
            _partialTranscript.value = ""
            Log.w("PracticeVM", "Speech error received: $errorMsg ($errorCode)")
            // Only retry on temporary silence or non-fatal matching errors
            val isRecoverable = errorCode == android.speech.SpeechRecognizer.ERROR_NO_MATCH ||
                    errorCode == android.speech.SpeechRecognizer.ERROR_SPEECH_TIMEOUT ||
                    errorCode == android.speech.SpeechRecognizer.ERROR_NETWORK_TIMEOUT

            if (isRecoverable && _isRecording.value && !_isPaused.value) {
                viewModelScope.launch {
                    delay(800)
                    if (_isRecording.value && !_isPaused.value) {
                        speechRecognizer.startListening()
                    }
                }
            } else if (!isRecoverable && errorCode == android.speech.SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS) {
                // If microphone permission missing or rejected, stop recording
                _isRecording.value = false
                _isPaused.value = false
                stopTimer()
            }
        }
    }

    fun updateCurrentUserId(uid: String) {
        if (uid.isNotBlank()) {
            _currentUserId.value = uid
        }
    }

    fun startSession(
        topic: PracticeTopic,
        level: EnglishLevel,
        userId: String = "guest_user",
        targetDurationMins: Int = 2,
        dayNumber: Int? = null,
        forceReset: Boolean = false
    ) {
        _currentTopic.value = topic
        _userLevel.value = level
        _currentUserId.value = userId
        _targetDurationMinutes.value = targetDurationMins.coerceIn(2, 60)
        _dayNumber.value = dayNumber

        val restored = if (!forceReset) {
            restoreIncompleteSessionIfExists(topic.id, dayNumber, userId)
        } else false

        if (!restored) {
            _currentQuestionIndex.value = 0
            resetState()
        }
    }

    fun startDayChallengeSession(
        dayNumber: Int,
        level: EnglishLevel,
        userId: String = "guest_user",
        targetDurationMins: Int = 2
    ) {
        viewModelScope.launch {
            val challenge = repository.getOrGenerateDayChallenge(dayNumber, level, userId)
            val challengeTopic = PracticeTopic(
                id = "day_challenge_$dayNumber",
                title = challenge.title,
                subtitle = challenge.subtitle,
                category = challenge.category,
                iconName = challenge.iconName,
                starterPrompt = challenge.starterPrompt,
                sampleQuestions = challenge.questions
            )
            startSession(
                topic = challengeTopic,
                level = level,
                userId = userId,
                targetDurationMins = targetDurationMins,
                dayNumber = dayNumber
            )
        }
    }

    fun setQuestionIndex(index: Int) {
        val maxIdx = (_currentTopic.value.sampleQuestions.size - 1).coerceAtLeast(0)
        _currentQuestionIndex.value = index.coerceIn(0, maxIdx)
    }

    fun nextQuestion() {
        val maxIdx = (_currentTopic.value.sampleQuestions.size - 1).coerceAtLeast(0)
        if (_currentQuestionIndex.value < maxIdx) {
            _currentQuestionIndex.value += 1
        }
    }

    fun previousQuestion() {
        if (_currentQuestionIndex.value > 0) {
            _currentQuestionIndex.value -= 1
        }
    }

    fun setTargetDurationMinutes(minutes: Int) {
        _targetDurationMinutes.value = minutes.coerceIn(2, 60)
    }

    fun startSpeaking() {
        if (_isRecording.value) return
        _isRecording.value = true
        _isPaused.value = false
        _partialTranscript.value = ""
        isActivelyPracticing = true
        com.example.notification.ScheduleNotificationManager.stopActiveAlarm(getApplication())
        speechRecognizer.startListening()
        startTimer()
    }

    fun pauseSpeaking() {
        if (!_isRecording.value || _isPaused.value) return
        _isPaused.value = true
        isActivelyPracticing = false
        stopTimer()
        speechRecognizer.stopListening()
    }

    fun resumeSpeaking() {
        if (!_isRecording.value || !_isPaused.value) return
        _isPaused.value = false
        _partialTranscript.value = ""
        isActivelyPracticing = true
        com.example.notification.ScheduleNotificationManager.stopActiveAlarm(getApplication())
        speechRecognizer.startListening()
        startTimer()
    }

    fun requestFinishConfirmation() {
        val targetSeconds = _targetDurationMinutes.value * 60L
        if (_actualSpeakingSeconds.value < targetSeconds) {
            _showIncompleteDurationNotice.value = true
            return
        }
        _showFinishConfirmDialog.value = true
    }

    fun cancelFinishConfirmation() {
        _showFinishConfirmDialog.value = false
    }

    fun dismissIncompleteDurationNotice() {
        _showIncompleteDurationNotice.value = false
    }

    fun dismissOffTopicNotice() {
        _showOffTopicNotice.value = false
    }

    fun confirmFinishSession() {
        _showFinishConfirmDialog.value = false
        val targetSeconds = _targetDurationMinutes.value * 60L
        if (_actualSpeakingSeconds.value < targetSeconds) {
            _showIncompleteDurationNotice.value = true
            return
        }

        _isRecording.value = false
        _isPaused.value = false
        isActivelyPracticing = false
        stopTimer()
        speechRecognizer.stopListening()

        val fullTranscript = _transcript.value.trim()
        val duration = _actualSpeakingSeconds.value

        viewModelScope.launch {
            _isAnalyzing.value = true
            try {
                val reportResponse = repository.analyzeSpeakingPracticeTranscript(
                    topic = _currentTopic.value,
                    level = _userLevel.value,
                    transcriptText = fullTranscript,
                    durationSeconds = duration
                )

                // Topic-relevance verification
                if (reportResponse.isTopicRelevant == false) {
                    _offTopicReport.value = reportResponse
                    _showOffTopicNotice.value = true
                    return@launch
                }

                val authUid = FirebaseSafeManager.getAuth(getApplication())?.currentUser?.uid
                val effectiveUserId = when {
                    !authUid.isNullOrBlank() -> authUid
                    _currentUserId.value.isNotBlank() && _currentUserId.value != "guest_user" -> _currentUserId.value
                    else -> "guest_user"
                }

                val session = PracticeSession(
                    sessionId = UUID.randomUUID().toString(),
                    userId = effectiveUserId,
                    topicTitle = _currentTopic.value.title,
                    topicCategory = _currentTopic.value.category,
                    startTime = System.currentTimeMillis() - (duration * 1000),
                    endTime = System.currentTimeMillis(),
                    durationSeconds = duration,
                    overallScore = reportResponse.overallScore,
                    grammarScore = reportResponse.grammarScore,
                    vocabularyScore = reportResponse.vocabularyScore,
                    fluencyScore = reportResponse.fluencyScore,
                    pronunciationScore = reportResponse.pronunciationScore,
                    confidenceScore = reportResponse.confidenceScore,
                    messagesCount = 1,
                    userSentencesCount = if (fullTranscript.isNotBlank()) fullTranscript.split(".").size.coerceAtLeast(1) else 0,
                    correctSentencesCount = 1,
                    correctionsCount = reportResponse.grammarCorrections?.size ?: 0,
                    feedbackSummary = reportResponse.feedbackSummary,
                    improvementTip = reportResponse.improvementTip,
                    isTopicRelevant = true,
                    topicRelevanceNotice = null,
                    dayNumber = _dayNumber.value,
                    vocabularySuggestions = reportResponse.vocabularySuggestions?.map { v ->
                        VocabularySuggestion(
                            originalWordOrPhrase = v.word,
                            betterAlternatives = v.betterAlternatives,
                            contextSentence = v.context ?: ""
                        )
                    } ?: emptyList(),
                    grammarCorrections = reportResponse.grammarCorrections?.map { g ->
                        GrammarCorrection(
                            originalSentence = g.originalSentence,
                            correctedSentence = g.correctedSentence,
                            explanation = g.explanation,
                            isTanglish = g.isTanglish
                        )
                    } ?: emptyList()
                )

                repository.saveSession(session)
                com.example.notification.ScheduleNotificationManager.markPracticeSessionAttended(getApplication(), effectiveUserId)
                com.example.notification.ScheduleNotificationManager.stopActiveAlarm(getApplication())

                val dayNum = _dayNumber.value
                if (dayNum != null) {
                    repository.saveDayChallengeCompleted(effectiveUserId, dayNum, session.overallScore)
                }
                clearIncompleteSession(_currentTopic.value.id, dayNum, effectiveUserId)

                _completedReport.value = session
                _showReportDialog.value = true
            } catch (e: Exception) {
                Log.e("SpeakingPracticeVM", "Error analyzing speaking session", e)
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    private val PREFS_INCOMPLETE = "speakup_incomplete_practice_prefs"

    fun saveIncompleteSession() {
        val topic = _currentTopic.value
        val transcript = _transcript.value.trim()
        val seconds = _actualSpeakingSeconds.value
        val dayNum = _dayNumber.value
        val questionIdx = _currentQuestionIndex.value
        val durationMins = _targetDurationMinutes.value
        val level = _userLevel.value.name
        val effectiveUid = _currentUserId.value.trim().ifBlank {
            FirebaseSafeManager.getAuth(getApplication())?.currentUser?.uid?.trim().orEmpty()
        }

        if (effectiveUid.isNotBlank() && (transcript.isNotBlank() || seconds > 0)) {
            val prefs = getApplication<Application>().getSharedPreferences(PREFS_INCOMPLETE, android.content.Context.MODE_PRIVATE)
            val key = if (dayNum != null) "${effectiveUid}_day_$dayNum" else "${effectiveUid}_topic_${topic.id}"
            prefs.edit()
                .putString("${key}_topic_id", topic.id)
                .putString("${key}_transcript", transcript)
                .putLong("${key}_seconds", seconds)
                .putInt("${key}_question_idx", questionIdx)
                .putInt("${key}_duration_mins", durationMins)
                .putString("${key}_level", level)
                .putLong("${key}_timestamp", System.currentTimeMillis())
                .apply()
            Log.d("SpeakingPracticeVM", "Saved incomplete session for $key: seconds=$seconds, transcriptLen=${transcript.length}")
        }
    }

    fun restoreIncompleteSessionIfExists(topicId: String, dayNum: Int?, uid: String): Boolean {
        val effectiveUid = uid.trim().ifBlank {
            _currentUserId.value.trim().ifBlank {
                FirebaseSafeManager.getAuth(getApplication())?.currentUser?.uid?.trim().orEmpty()
            }
        }
        if (effectiveUid.isBlank()) return false
        val prefs = getApplication<Application>().getSharedPreferences(PREFS_INCOMPLETE, android.content.Context.MODE_PRIVATE)
        val key = if (dayNum != null) "${effectiveUid}_day_$dayNum" else "${effectiveUid}_topic_$topicId"

        val savedTranscript = prefs.getString("${key}_transcript", null)
        val savedSeconds = prefs.getLong("${key}_seconds", 0L)
        val savedQuestionIdx = prefs.getInt("${key}_question_idx", 0)

        if (!savedTranscript.isNullOrBlank() || savedSeconds > 0) {
            _transcript.value = savedTranscript ?: ""
            _actualSpeakingSeconds.value = savedSeconds
            _currentQuestionIndex.value = savedQuestionIdx
            _isPaused.value = true
            _isRecording.value = false
            Log.d("SpeakingPracticeVM", "Restored incomplete session for $key: seconds=$savedSeconds, transcript='$savedTranscript'")
            return true
        }
        return false
    }

    fun clearIncompleteSession(topicId: String, dayNum: Int?, uid: String) {
        val effectiveUid = uid.trim().ifBlank {
            _currentUserId.value.trim().ifBlank {
                FirebaseSafeManager.getAuth(getApplication())?.currentUser?.uid?.trim().orEmpty()
            }
        }
        if (effectiveUid.isBlank()) return
        val prefs = getApplication<Application>().getSharedPreferences(PREFS_INCOMPLETE, android.content.Context.MODE_PRIVATE)
        val key = if (dayNum != null) "${effectiveUid}_day_$dayNum" else "${effectiveUid}_topic_$topicId"
        prefs.edit()
            .remove("${key}_topic_id")
            .remove("${key}_transcript")
            .remove("${key}_seconds")
            .remove("${key}_question_idx")
            .remove("${key}_duration_mins")
            .remove("${key}_level")
            .remove("${key}_timestamp")
            .apply()
        Log.d("SpeakingPracticeVM", "Cleared incomplete session for $key")
    }

    fun discardIncompleteSession() {
        val topic = _currentTopic.value
        val dayNum = _dayNumber.value
        val uid = _currentUserId.value
        clearIncompleteSession(topic.id, dayNum, uid)
        resetState()
    }

    fun dismissReportDialog() {
        _showReportDialog.value = false
    }

    fun appendManualText(text: String) {
        if (text.isNotBlank()) {
            val current = _transcript.value
            _transcript.value = if (current.isBlank()) text.trim() else "$current $text".trim()
        }
    }

    fun dismissTimerFinishedDialog() {
        _isTimerFinished.value = false
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive && _isRecording.value && !_isPaused.value) {
                delay(1000)
                _actualSpeakingSeconds.value += 1
                if (_actualSpeakingSeconds.value >= _targetDurationMinutes.value * 60L) {
                    _isRecording.value = false
                    _isPaused.value = false
                    isActivelyPracticing = false
                    speechRecognizer.stopListening()
                    _isTimerFinished.value = true
                    break
                }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    fun resetState() {
        stopTimer()
        speechRecognizer.cancelListening()
        _isRecording.value = false
        _isPaused.value = false
        isActivelyPracticing = false
        _isTimerFinished.value = false
        _actualSpeakingSeconds.value = 0L
        _transcript.value = ""
        _partialTranscript.value = ""
        _completedReport.value = null
        _showFinishConfirmDialog.value = false
        _showReportDialog.value = false
        _showIncompleteDurationNotice.value = false
        _showOffTopicNotice.value = false
        _offTopicReport.value = null
        _isAnalyzing.value = false
    }

    override fun onCleared() {
        super.onCleared()
        saveIncompleteSession()
        isActivelyPracticing = false
        stopTimer()
        speechRecognizer.destroy()
    }
}
