package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "coach_chat_messages")
data class CoachChatMessageEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val sender: String, // "USER", "AI_COACH", "SYSTEM"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val originalSentence: String? = null,
    val correctedSentence: String? = null,
    val explanation: String? = null,
    val isTanglish: Boolean = false,
    val vocabularySuggestions: List<VocabularySuggestion> = emptyList(),
    val fluencyNotes: String? = null,
    val pronunciationTip: String? = null,
    val estimatedConfidence: Int? = null,
    val isTanglishDetected: Boolean = false
) {
    fun toChatMessage(): ChatMessage {
        val correction = if (correctedSentence != null) {
            GrammarCorrection(
                originalSentence = originalSentence ?: "",
                correctedSentence = correctedSentence,
                explanation = explanation ?: "",
                isTanglish = isTanglish
            )
        } else null

        val messageSender = try {
            MessageSender.valueOf(sender)
        } catch (e: Exception) {
            MessageSender.AI_COACH
        }

        return ChatMessage(
            id = id,
            sender = messageSender,
            text = text,
            timestamp = timestamp,
            grammarCorrection = correction,
            vocabularySuggestions = vocabularySuggestions,
            fluencyNotes = fluencyNotes,
            pronunciationTip = pronunciationTip,
            estimatedConfidence = estimatedConfidence,
            isTanglishDetected = isTanglishDetected
        )
    }

    companion object {
        fun fromChatMessage(msg: ChatMessage, userId: String): CoachChatMessageEntity {
            return CoachChatMessageEntity(
                id = msg.id,
                userId = userId,
                sender = msg.sender.name,
                text = msg.text,
                timestamp = msg.timestamp,
                originalSentence = msg.grammarCorrection?.originalSentence,
                correctedSentence = msg.grammarCorrection?.correctedSentence,
                explanation = msg.grammarCorrection?.explanation,
                isTanglish = msg.grammarCorrection?.isTanglish ?: false,
                vocabularySuggestions = msg.vocabularySuggestions,
                fluencyNotes = msg.fluencyNotes,
                pronunciationTip = msg.pronunciationTip,
                estimatedConfidence = msg.estimatedConfidence,
                isTanglishDetected = msg.isTanglishDetected
            )
        }
    }
}
