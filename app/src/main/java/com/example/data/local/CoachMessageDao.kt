package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.CoachChatMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CoachMessageDao {
    @Query("SELECT * FROM coach_chat_messages WHERE userId = :userId ORDER BY timestamp ASC")
    fun getMessagesFlow(userId: String): Flow<List<CoachChatMessageEntity>>

    @Query("SELECT * FROM coach_chat_messages WHERE userId = :userId ORDER BY timestamp ASC")
    suspend fun getMessages(userId: String): List<CoachChatMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: CoachChatMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<CoachChatMessageEntity>)

    @Query("DELETE FROM coach_chat_messages WHERE userId = :userId")
    suspend fun clearMessagesForUser(userId: String)
}
