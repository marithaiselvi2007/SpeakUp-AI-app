package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ChatMessage
import com.example.data.model.EnglishLevel
import com.example.data.model.GrammarCorrection
import com.example.data.model.MessageSender
import com.example.data.model.VocabularySuggestion
import com.example.data.remote.FirebaseSafeManager
import com.example.data.repository.SpeakUpRepository
import com.example.speech.SpeechRecognizerHelper
import com.example.speech.SpeechState
import com.example.speech.TtsHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class CoachViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SpeakUpRepository(application)
    private val auth get() = FirebaseSafeManager.getAuth(getApplication())
    private val speechHelper = SpeechRecognizerHelper(application)
    private val ttsHelper = TtsHelper(application)

    private val defaultInitialMessage = ChatMessage(
        id = "welcome_greeting",
        sender = MessageSender.AI_COACH,
        text = "Hello! I am your AI English Speaking Coach 👋\n\nWe can talk freely about anything: your daily life, college, friends, movies, technology, career, or any questions you have!\n\nHow are you doing today?",
        timestamp = System.currentTimeMillis()
    )

    private val _messages = MutableStateFlow<List<ChatMessage>>(listOf(defaultInitialMessage))
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _userEnglishLevel = MutableStateFlow(EnglishLevel.BEGINNER)
    val userEnglishLevel: StateFlow<EnglishLevel> = _userEnglishLevel.asStateFlow()

    private var currentUserId: String = auth?.currentUser?.uid ?: "guest_user"

    // Speech & TTS states
    val speechState: StateFlow<SpeechState> = speechHelper.speechState
    val speechRmsDb: StateFlow<Float> = speechHelper.rmsDb
    val speechErrorMessage: StateFlow<String?> = speechHelper.errorMessage
    val isTtsSpeaking: StateFlow<Boolean> = ttsHelper.isSpeaking
    val isTtsEnabled: StateFlow<Boolean> = ttsHelper.isTtsEnabled

    init {
        speechHelper.onFinalResult = { recognizedText ->
            if (recognizedText.isNotBlank()) {
                sendMessage(recognizedText)
            }
        }
        loadChatHistory(currentUserId)
    }

    fun updateCurrentUserId(uid: String, level: EnglishLevel? = null) {
        if (level != null) {
            _userEnglishLevel.value = level
        }
        if (currentUserId != uid) {
            currentUserId = uid
            loadChatHistory(uid)
        }
    }

    fun loadChatHistory(userId: String) {
        currentUserId = userId
        viewModelScope.launch {
            val existing = repository.getCoachMessages(userId)
            if (existing.isNotEmpty()) {
                _messages.value = existing
            } else {
                // Initialize default welcoming greeting only if no previous conversation exists
                val initialGreeting = "Hello! I am your AI English Speaking Coach 👋\n\nWe can talk freely about anything: your daily life, college, friends, movies, technology, career, or any questions you have!\n\nHow are you doing today?"
                val initialCoachMessage = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    sender = MessageSender.AI_COACH,
                    text = initialGreeting,
                    timestamp = System.currentTimeMillis()
                )
                _messages.value = listOf(initialCoachMessage)
                repository.saveCoachMessage(userId, initialCoachMessage)
            }
        }
    }

    fun startFreeConversation(level: EnglishLevel = EnglishLevel.BEGINNER) {
        _userEnglishLevel.value = level
        // Ensure chat history is loaded without clearing
        if (_messages.value.isEmpty()) {
            loadChatHistory(currentUserId)
        }
    }

    fun clearMessages() {
        _messages.value = emptyList()
        currentUserId = "guest_user"
        ttsHelper.stop()
        speechHelper.cancelListening()
    }

    fun sendMessage(userText: String) {
        val cleanText = userText.trim()
        if (cleanText.isBlank() || _isAnalyzing.value) return

        val userMessageId = UUID.randomUUID().toString()
        val tempUserMessage = ChatMessage(
            id = userMessageId,
            sender = MessageSender.USER,
            text = cleanText,
            timestamp = System.currentTimeMillis()
        )

        val updatedList = _messages.value + tempUserMessage
        _messages.value = updatedList
        _isAnalyzing.value = true

        viewModelScope.launch {
            // Save preliminary user message
            repository.saveCoachMessage(currentUserId, tempUserMessage)

            try {
                val analysis = repository.analyzeCoachUtterance(
                    userText = cleanText,
                    conversationHistory = _messages.value,
                    level = _userEnglishLevel.value
                )

                var correction: GrammarCorrection? = null
                if (analysis.hasGrammarMistake && analysis.correctedSentence != null) {
                    correction = GrammarCorrection(
                        originalSentence = analysis.originalSentence ?: cleanText,
                        correctedSentence = analysis.correctedSentence,
                        explanation = analysis.grammarExplanation ?: "Use standard English phrasing.",
                        isTanglish = analysis.isTanglishDetected
                    )
                }

                val vocabSuggestions = analysis.vocabularySuggestions?.map {
                    VocabularySuggestion(
                        originalWordOrPhrase = it.word,
                        betterAlternatives = it.betterAlternatives,
                        contextSentence = it.context ?: ""
                    )
                } ?: emptyList()

                val updatedUserMessage = tempUserMessage.copy(
                    grammarCorrection = correction,
                    vocabularySuggestions = vocabSuggestions,
                    fluencyNotes = analysis.fluencyFeedback,
                    pronunciationTip = analysis.pronunciationTip,
                    estimatedConfidence = analysis.confidenceScore,
                    isTanglishDetected = analysis.isTanglishDetected
                )

                val coachMessage = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    sender = MessageSender.AI_COACH,
                    text = analysis.coachReply,
                    timestamp = System.currentTimeMillis()
                )

                _messages.value = _messages.value.map {
                    if (it.id == userMessageId) updatedUserMessage else it
                } + coachMessage

                // Persist updated user message with feedback & coach response
                repository.saveCoachMessage(currentUserId, updatedUserMessage)
                repository.saveCoachMessage(currentUserId, coachMessage)

                // Speak coach response aloud if enabled
                ttsHelper.speak(analysis.coachReply)
            } catch (e: Exception) {
                // Fallback reply
                val fallbackCoachMessage = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    sender = MessageSender.AI_COACH,
                    text = "I understand what you mean! Let's continue practicing. What else would you like to talk about?",
                    timestamp = System.currentTimeMillis()
                )
                _messages.value = _messages.value + fallbackCoachMessage
                repository.saveCoachMessage(currentUserId, fallbackCoachMessage)
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    fun startListening() {
        speechHelper.startListening()
    }

    fun stopListening() {
        speechHelper.stopListening()
    }

    fun toggleTts() {
        ttsHelper.toggleTts()
    }

    fun speakText(text: String) {
        ttsHelper.speak(text)
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearCoachMessages(currentUserId)
            val initialGreeting = "Hello! I am your AI English Speaking Coach 👋\n\nWe can talk freely about anything: your daily life, college, friends, movies, technology, career, or any questions you have!\n\nHow are you doing today?"
            val initialCoachMessage = ChatMessage(
                id = UUID.randomUUID().toString(),
                sender = MessageSender.AI_COACH,
                text = initialGreeting,
                timestamp = System.currentTimeMillis()
            )
            _messages.value = listOf(initialCoachMessage)
            repository.saveCoachMessage(currentUserId, initialCoachMessage)
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechHelper.destroy()
        ttsHelper.shutdown()
    }
}
