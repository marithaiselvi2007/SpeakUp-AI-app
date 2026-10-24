package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey val uid: String = "default_user",
    val name: String = "Learner",
    val email: String = "",
    val photoUrl: String = "",
    val englishLevel: EnglishLevel = EnglishLevel.BEGINNER,
    val practiceDays: List<String> = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday"),
    val preferredPracticeTime: String = "07:30 AM",
    val sessionDurationMinutes: Int = 13,
    val dailyGoalMinutes: Int = 15,
    val preferredTopics: List<String> = listOf("Self Introduction", "Daily Routine", "Free Speaking"),
    val themeColor: ThemeColorOption = ThemeColorOption.BLUE,
    val themeMode: ThemeModeOption = ThemeModeOption.SYSTEM,
    val isProfileCompleted: Boolean = false,
    val isPracticeAlarmEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

