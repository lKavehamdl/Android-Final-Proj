package com.example.final_project.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.final_project.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
@JvmSuppressWildcards
interface MessagesDao {

    @Query("""
        SELECT * FROM messages 
        WHERE (senderId = :currentUserId AND receiverId = :otherUserId)
           OR (senderId = :otherUserId AND receiverId = :currentUserId)
        ORDER BY createdAt ASC
    """)
    fun getConversation(currentUserId: String, otherUserId: String): Flow<List<MessageEntity>>

    @Query("""
        SELECT * FROM messages 
        WHERE (senderId = :currentUserId AND receiverId = :otherUserId)
           OR (senderId = :otherUserId AND receiverId = :currentUserId)
        ORDER BY createdAt ASC
    """)
    fun getConversationPaged(
        currentUserId: String,
        otherUserId: String,
    ): androidx.paging.PagingSource<Int, MessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>): LongArray

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity): Long

    @Query("UPDATE messages SET status = 'read' WHERE receiverId = :currentUserId AND senderId = :otherUserId AND status != 'read'")
    suspend fun markMessagesAsRead(currentUserId: String, otherUserId: String): Int
    
    @Query("""
        SELECT * FROM messages 
        WHERE id IN (
            SELECT MAX(id) FROM messages -- Approximation, better to sort by createdAt
            WHERE senderId = :currentUserId OR receiverId = :currentUserId
            GROUP BY CASE WHEN senderId = :currentUserId THEN receiverId ELSE senderId END
        )
        ORDER BY createdAt DESC
    """)
    fun getRecentConversations(currentUserId: String): Flow<List<MessageEntity>>
}
