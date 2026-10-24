package com.example.data.model

data class DayChallenge(
    val dayNumber: Int,
    val title: String,
    val subtitle: String,
    val category: String,
    val iconName: String,
    val starterPrompt: String,
    val questions: List<String>,
    val keywords: List<String>,
    val sampleAnswerHints: List<String>,
    val isCompleted: Boolean = false,
    val isUnlocked: Boolean = false,
    val bestScore: Int? = null
)

enum class DayStatus {
    COMPLETED,
    AVAILABLE,
    LOCKED
}
