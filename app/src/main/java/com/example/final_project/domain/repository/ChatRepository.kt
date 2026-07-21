package com.example.final_project.domain.repository

import androidx.paging.PagingData
import com.example.final_project.core.result.Resource
import com.example.final_project.domain.model.Message
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getConversation(otherUserId: String): Flow<List<Message>>
    fun getConversationPaged(otherUserId: String): Flow<PagingData<Message>>
    fun getRecentConversations(): Flow<List<Message>>

    /** Total unread inbound messages for the signed-in user — drives the Messages tab badge. */
    fun observeUnreadCount(): Flow<Int>

    suspend fun sendMessage(receiverId: String, content: String?, songId: String? = null): Resource<Unit>
    suspend fun markAsRead(otherUserId: String): Resource<Unit>

    /** Deletes one of the signed-in user's own messages (server + local). */
    suspend fun deleteMessage(messageId: String): Resource<Unit>

    suspend fun startListeningForMessages()
    suspend fun stopListeningForMessages()

    /** Stops listening and wipes locally-cached messages — call on logout. */
    suspend fun clearLocalData()

    suspend fun setTypingStatus(receiverId: String, isTyping: Boolean)
    fun observeTypingStatus(userId: String): Flow<Boolean>
}
