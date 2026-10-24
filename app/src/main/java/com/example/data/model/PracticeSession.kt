package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "practice_sessions")
data class PracticeSession(
    @PrimaryKey val sessionId: String = UUID.randomUUID().toString(),
    val userId: String,
    val topicTitle: String,
    val topicCategory: String = "General",
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long = 0L,
    val durationSeconds: Long = 0,
    val overallScore: Int = 0,
    val grammarScore: Int = 0,
    val vocabularyScore: Int = 0,
    val fluencyScore: Int = 0,
    val pronunciationScore: Int = 0,
    val confidenceScore: Int = 0,
    val messagesCount: Int = 0,
    val userSentencesCount: Int = 0,
    val correctSentencesCount: Int = 0,
    val correctionsCount: Int = 0,
    val feedbackSummary: String = "",
    val improvementTip: String = "",
    val isTopicRelevant: Boolean = true,
    val topicRelevanceNotice: String? = null,
    val dayNumber: Int? = null,
    val vocabularySuggestions: List<VocabularySuggestion> = emptyList(),
    val grammarCorrections: List<GrammarCorrection> = emptyList()
)

data class GrammarCorrection(
    val originalSentence: String,
    val correctedSentence: String,
    val explanation: String,
    val isTanglish: Boolean = false
)

data class VocabularySuggestion(
    val originalWordOrPhrase: String,
    val betterAlternatives: List<String>,
    val contextSentence: String = ""
)

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val grammarCorrection: GrammarCorrection? = null,
    val vocabularySuggestions: List<VocabularySuggestion> = emptyList(),
    val fluencyNotes: String? = null,
    val pronunciationTip: String? = null,
    val estimatedConfidence: Int? = null,
    val isTanglishDetected: Boolean = false
)

enum class MessageSender {
    USER,
    AI_COACH,
    SYSTEM
}

data class PracticeTopic(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: String,
    val iconName: String,
    val starterPrompt: String,
    val sampleQuestions: List<String>
)
