package com.example.data.remote

import android.content.Context
import android.util.Log
import com.example.data.model.EnglishLevel
import com.example.data.model.PracticeSession
import com.example.data.model.ThemeColorOption
import com.example.data.model.ThemeModeOption
import com.example.data.model.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class FirestoreService(private val context: Context? = null) {
    private val firestore: FirebaseFirestore?
        get() = try {
            FirebaseSafeManager.getFirestore(context)
        } catch (e: Throwable) {
            null
        }

    suspend fun saveUserProfile(profile: UserProfile): Result<Unit> {
        val fs = firestore ?: return Result.success(Unit) // Local DB handles offline
        return try {
            val userMap = hashMapOf<String, Any>(
                "uid" to profile.uid,
                "name" to profile.name,
                "email" to profile.email,
                "photoUrl" to profile.photoUrl,
                "englishLevel" to profile.englishLevel.name,
                "practiceDays" to profile.practiceDays,
                "preferredPracticeTime" to profile.preferredPracticeTime,
                "sessionDurationMinutes" to profile.sessionDurationMinutes,
                "dailyGoalMinutes" to profile.dailyGoalMinutes,
                "preferredTopics" to profile.preferredTopics,
                "themeColor" to profile.themeColor.name,
                "themeMode" to profile.themeMode.name,
                "isProfileCompleted" to profile.isProfileCompleted,
                "updatedAt" to System.currentTimeMillis()
            )
            fs.collection("users").document(profile.uid)
                .set(userMap, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Throwable) {
            Log.w("FirestoreService", "Error saving user profile: ${e.message}")
            Result.failure(Exception(e))
        }
    }

    suspend fun getUserProfile(uid: String): UserProfile? {
        val fs = firestore ?: return null
        return try {
            val snapshot = fs.collection("users").document(uid).get().await()
            if (!snapshot.exists()) return null

            val name = snapshot.getString("name") ?: "Learner"
            val email = snapshot.getString("email") ?: ""
            val photoUrl = snapshot.getString("photoUrl") ?: ""
            val levelStr = snapshot.getString("englishLevel") ?: EnglishLevel.BEGINNER.name
            val englishLevel = try { EnglishLevel.valueOf(levelStr) } catch (e: Exception) { EnglishLevel.BEGINNER }
            val dailyGoal = snapshot.getLong("dailyGoalMinutes")?.toInt() ?: 15
            val practiceDays = (snapshot.get("practiceDays") as? List<*>)?.mapNotNull { it?.toString() }
                ?: listOf("Mon", "Tue", "Wed", "Thu", "Fri")
            val preferredPracticeTime = snapshot.getString("preferredPracticeTime") ?: "07:30 AM"
            val sessionDurationMinutes = snapshot.getLong("sessionDurationMinutes")?.toInt() ?: 13
            @Suppress("UNCHECKED_CAST")
            val topics = (snapshot.get("preferredTopics") as? List<*>)?.mapNotNull { it?.toString() }
                ?: listOf("Self Introduction", "Daily Routine", "Free Conversation")
            val themeColorStr = snapshot.getString("themeColor") ?: ThemeColorOption.BLUE.name
            val themeColor = try { ThemeColorOption.valueOf(themeColorStr) } catch (e: Exception) { ThemeColorOption.BLUE }
            val themeModeStr = snapshot.getString("themeMode") ?: ThemeModeOption.SYSTEM.name
            val themeMode = try { ThemeModeOption.valueOf(themeModeStr) } catch (e: Exception) { ThemeModeOption.SYSTEM }
            val isCompleted = snapshot.getBoolean("isProfileCompleted") ?: false

            UserProfile(
                uid = uid,
                name = name,
                email = email,
                photoUrl = photoUrl,
                englishLevel = englishLevel,
                practiceDays = practiceDays,
                preferredPracticeTime = preferredPracticeTime,
                sessionDurationMinutes = sessionDurationMinutes,
                dailyGoalMinutes = dailyGoal,
                preferredTopics = topics,
                themeColor = themeColor,
                themeMode = themeMode,
                isProfileCompleted = isCompleted
            )
        } catch (e: Throwable) {
            Log.w("FirestoreService", "Error fetching user profile: ${e.message}")
            null
        }
    }

    suspend fun saveSession(session: PracticeSession): Result<Unit> {
        val fs = firestore ?: return Result.success(Unit)
        return try {
            fs.collection("users").document(session.userId)
                .collection("sessions").document(session.sessionId)
                .set(session, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Throwable) {
            Log.w("FirestoreService", "Error saving session to Firestore: ${e.message}")
            Result.failure(Exception(e))
        }
    }

    suspend fun getSessions(userId: String): List<PracticeSession> {
        val fs = firestore ?: return emptyList()
        return try {
            val snapshot = fs.collection("users").document(userId)
                .collection("sessions")
                .orderBy("startTime")
                .get()
                .await()

            snapshot.toObjects(PracticeSession::class.java)
        } catch (e: Throwable) {
            Log.w("FirestoreService", "Error fetching sessions from Firestore: ${e.message}")
            emptyList()
        }
    }

    suspend fun saveDailyChallengeProgress(uid: String, completedDays: List<Int>, lastChallengeDate: String): Result<Unit> {
        val fs = firestore ?: return Result.success(Unit)
        return try {
            val data = hashMapOf<String, Any>(
                "completedDayChallenges" to completedDays,
                "lastDailyChallengeDate" to lastChallengeDate,
                "updatedAt" to System.currentTimeMillis()
            )
            fs.collection("users").document(uid)
                .set(data, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Throwable) {
            Log.w("FirestoreService", "Error saving daily challenge progress: ${e.message}")
            Result.failure(Exception(e))
        }
    }

    suspend fun getDailyChallengeProgress(uid: String): Pair<List<Int>, String>? {
        val fs = firestore ?: return null
        return try {
            val snapshot = fs.collection("users").document(uid).get().await()
            if (!snapshot.exists()) return null
            val completedList = (snapshot.get("completedDayChallenges") as? List<*>)?.mapNotNull {
                (it as? Number)?.toInt() ?: it?.toString()?.toIntOrNull()
            } ?: emptyList()
            val lastDate = snapshot.getString("lastDailyChallengeDate") ?: ""
            Pair(completedList, lastDate)
        } catch (e: Throwable) {
            Log.w("FirestoreService", "Error fetching daily challenge progress: ${e.message}")
            null
        }
    }

    suspend fun savePracticeCompletion(uid: String, completedDates: List<String>): Result<Unit> {
        val fs = firestore ?: return Result.success(Unit)
        return try {
            val data = hashMapOf<String, Any>(
                "completedPracticeDates" to completedDates,
                "updatedAt" to System.currentTimeMillis()
            )
            fs.collection("users").document(uid)
                .set(data, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Throwable) {
            Log.w("FirestoreService", "Error saving practice completion to Firestore: ${e.message}")
            Result.failure(Exception(e))
        }
    }

    suspend fun getPracticeCompletion(uid: String): List<String> {
        val fs = firestore ?: return emptyList()
        return try {
            val snapshot = fs.collection("users").document(uid).get().await()
            if (!snapshot.exists()) return emptyList()
            (snapshot.get("completedPracticeDates") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
        } catch (e: Throwable) {
            Log.w("FirestoreService", "Error fetching practice completion: ${e.message}")
            emptyList()
        }
    }
}
