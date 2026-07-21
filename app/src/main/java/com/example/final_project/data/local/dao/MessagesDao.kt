package com.example.final_project.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.final_project.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
@JvmSuppressWildcards
interface MessagesDao {

    @Query(
        """
        SELECT * FROM messages 
        WHERE (senderId = :currentUserId AND receiverId = :otherUserId)
           OR (senderId = :otherUserId AND receiverId = :currentUserId)
        ORDER BY createdAt ASC
        """,
    )
    fun getConversation(currentUserId: String, otherUserId: String): Flow<List<MessageEntity>>

    @Query(
        """
        SELECT * FROM messages 
        WHERE (senderId = :currentUserId AND receiverId = :otherUserId)
           OR (senderId = :otherUserId AND receiverId = :currentUserId)
        ORDER BY createdAt ASC
        """,
    )
    fun getConversationPaged(
        currentUserId: String,
        otherUserId: String,
    ): PagingSource<Int, MessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>): LongArray

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity): Long

    @Query(
        """
        UPDATE messages SET status = 'read'
        WHERE receiverId = :currentUserId AND senderId = :otherUserId AND status != 'read'
        """,
    )
    suspend fun markMessagesAsRead(currentUserId: String, otherUserId: String): Int

    // Latest message per conversation partner. Uses createdAt (MAX(id) was wrong —
    // id is a UUID string, so its max is lexicographic, not the newest message).
    @Query(
        """
        SELECT * FROM messages m
        WHERE (m.senderId = :currentUserId OR m.receiverId = :currentUserId)
          AND NOT EXISTS (
            SELECT 1 FROM messages m2
            WHERE (m2.senderId = :currentUserId OR m2.receiverId = :currentUserId)
              AND (CASE WHEN m2.senderId = :currentUserId THEN m2.receiverId ELSE m2.senderId END)
                = (CASE WHEN m.senderId = :currentUserId THEN m.receiverId ELSE m.senderId END)
              AND m2.createdAt > m.createdAt
          )
        ORDER BY m.createdAt DESC
        """,
    )
    fun getRecentConversations(currentUserId: String): Flow<List<MessageEntity>>

    @Query(
        """
        SELECT COUNT(*) FROM messages
        WHERE receiverId = :currentUserId AND senderId != :currentUserId AND status != 'read'
        """,
    )
    fun getUnreadCount(currentUserId: String): Flow<Int>

    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun deleteById(id: String): Int

    @Query("DELETE FROM messages")
    suspend fun clearAll(): Int
}
