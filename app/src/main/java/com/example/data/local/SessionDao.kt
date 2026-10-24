package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.PracticeSession
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Query("SELECT * FROM practice_sessions WHERE userId = :userId ORDER BY startTime DESC")
    fun getAllSessionsFlow(userId: String): Flow<List<PracticeSession>>

    @Query("SELECT * FROM practice_sessions WHERE userId = :userId ORDER BY startTime DESC")
    suspend fun getAllSessions(userId: String): List<PracticeSession>

    @Query("SELECT * FROM practice_sessions WHERE sessionId = :sessionId LIMIT 1")
    suspend fun getSessionById(sessionId: String): PracticeSession?

    @Query("SELECT * FROM practice_sessions WHERE userId = :userId ORDER BY startTime DESC LIMIT 1")
    suspend fun getLatestSession(userId: String): PracticeSession?

    @Query("SELECT * FROM practice_sessions WHERE userId = :userId ORDER BY startTime DESC LIMIT 1 OFFSET 1")
    suspend fun getPreviousSession(userId: String): PracticeSession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: PracticeSession)

    @Query("UPDATE practice_sessions SET userId = :newUserId WHERE userId = 'guest_user'")
    suspend fun associateGuestSessions(newUserId: String)

    @Query("SELECT COUNT(*) FROM practice_sessions WHERE startTime >= :startOfDayTimestamp")
    suspend fun getSessionsCountSince(startOfDayTimestamp: Long): Int

    @Query("SELECT COUNT(*) FROM practice_sessions WHERE userId = :userId AND startTime >= :startOfDayTimestamp")
    suspend fun getSessionsCountSinceForUser(userId: String, startOfDayTimestamp: Long): Int

    @Query("SELECT * FROM practice_sessions WHERE dayNumber IS NOT NULL AND userId = :userId ORDER BY startTime DESC")
    suspend fun getDayChallengeSessions(userId: String): List<PracticeSession>

    @Query("DELETE FROM practice_sessions WHERE sessionId = :sessionId")
    suspend fun deleteSession(sessionId: String)

    @Query("DELETE FROM practice_sessions WHERE userId = :userId")
    suspend fun deleteAllUserSessions(userId: String)
}
