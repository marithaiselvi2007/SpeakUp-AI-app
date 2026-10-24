package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val systemInstruction: GeminiContent? = null,
    val generationConfig: GeminiGenerationConfig? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    val role: String? = null,
    val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiPart(
    val text: String
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
    val temperature: Float? = 0.7f,
    val topP: Float? = 0.95f,
    val topK: Int? = 40,
    @Json(name = "responseMimeType") val responseMimeType: String? = "application/json"
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val candidates: List<GeminiCandidate>? = null,
    val error: GeminiError? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    val content: GeminiContent? = null,
    val finishReason: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiError(
    val code: Int? = null,
    val message: String? = null,
    val status: String? = null
)

// Structured AI Coach Analysis output from Gemini
@JsonClass(generateAdapter = true)
data class CoachAnalysisResponse(
    @Json(name = "coachReply") val coachReply: String,
    @Json(name = "isTanglishDetected") val isTanglishDetected: Boolean = false,
    @Json(name = "hasGrammarMistake") val hasGrammarMistake: Boolean = false,
    @Json(name = "originalSentence") val originalSentence: String? = null,
    @Json(name = "correctedSentence") val correctedSentence: String? = null,
    @Json(name = "grammarExplanation") val grammarExplanation: String? = null,
    @Json(name = "vocabularySuggestions") val vocabularySuggestions: List<VocabItemJson>? = null,
    @Json(name = "fluencyScore") val fluencyScore: Int = 80,
    @Json(name = "fluencyFeedback") val fluencyFeedback: String? = null,
    @Json(name = "pronunciationTip") val pronunciationTip: String? = null,
    @Json(name = "confidenceScore") val confidenceScore: Int = 80
)

@JsonClass(generateAdapter = true)
data class VocabItemJson(
    @Json(name = "word") val word: String,
    @Json(name = "betterAlternatives") val betterAlternatives: List<String>,
    @Json(name = "context") val context: String? = null
)

@JsonClass(generateAdapter = true)
data class SessionSummaryResponse(
    @Json(name = "overallScore") val overallScore: Int,
    @Json(name = "grammarScore") val grammarScore: Int,
    @Json(name = "vocabularyScore") val vocabularyScore: Int,
    @Json(name = "fluencyScore") val fluencyScore: Int,
    @Json(name = "pronunciationScore") val pronunciationScore: Int,
    @Json(name = "confidenceScore") val confidenceScore: Int,
    @Json(name = "feedbackSummary") val feedbackSummary: String,
    @Json(name = "improvementTip") val improvementTip: String
)

@JsonClass(generateAdapter = true)
data class SpeakingPracticeReportResponse(
    @Json(name = "overallScore") val overallScore: Int,
    @Json(name = "grammarScore") val grammarScore: Int,
    @Json(name = "vocabularyScore") val vocabularyScore: Int,
    @Json(name = "fluencyScore") val fluencyScore: Int,
    @Json(name = "pronunciationScore") val pronunciationScore: Int,
    @Json(name = "confidenceScore") val confidenceScore: Int,
    @Json(name = "feedbackSummary") val feedbackSummary: String,
    @Json(name = "improvementTip") val improvementTip: String,
    @Json(name = "isTopicRelevant") val isTopicRelevant: Boolean = true,
    @Json(name = "topicRelevanceNotice") val topicRelevanceNotice: String? = null,
    @Json(name = "grammarCorrections") val grammarCorrections: List<GrammarCorrectionJson>? = null,
    @Json(name = "vocabularySuggestions") val vocabularySuggestions: List<VocabItemJson>? = null
)

@JsonClass(generateAdapter = true)
data class GrammarCorrectionJson(
    @Json(name = "originalSentence") val originalSentence: String,
    @Json(name = "correctedSentence") val correctedSentence: String,
    @Json(name = "explanation") val explanation: String,
    @Json(name = "isTanglish") val isTanglish: Boolean = false
)

@JsonClass(generateAdapter = true)
data class DayChallengeAiResponse(
    @Json(name = "title") val title: String,
    @Json(name = "subtitle") val subtitle: String,
    @Json(name = "category") val category: String,
    @Json(name = "iconName") val iconName: String = "AutoAwesome",
    @Json(name = "starterPrompt") val starterPrompt: String,
    @Json(name = "questions") val questions: List<String>,
    @Json(name = "keywords") val keywords: List<String>,
    @Json(name = "sampleAnswerHints") val sampleAnswerHints: List<String>
)
