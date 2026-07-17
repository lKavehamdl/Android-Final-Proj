package com.example.final_project.domain.repository

import com.example.final_project.core.result.Resource
import com.example.final_project.domain.model.Message
import com.example.final_project.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getConversation(otherUserId: String): Flow<List<Message>>
    fun getRecentConversations(): Flow<List<Message>>
    suspend fun sendMessage(receiverId: String, content: String?, songId: String? = null): Resource<Unit>
    suspend fun markAsRead(otherUserId: String): Resource<Unit>
    
    // Realtime connection lifecycle
    suspend fun startListeningForMessages()
    suspend fun stopListeningForMessages()
    
    suspend fun setTypingStatus(receiverId: String, isTyping: Boolean)
    fun observeTypingStatus(userId: String): Flow<Boolean>
}
