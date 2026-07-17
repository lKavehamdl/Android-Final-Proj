package com.example.final_project.data.repository

import com.example.final_project.core.dispatchers.DispatchersProvider
import com.example.final_project.core.result.Resource
import com.example.final_project.data.local.dao.MessagesDao
import com.example.final_project.data.mapper.toDomain
import com.example.final_project.data.mapper.toEntity
import com.example.final_project.data.remote.dto.MessageDto
import com.example.final_project.domain.model.Message
import com.example.final_project.domain.repository.ChatRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import io.github.jan.supabase.realtime.decodeRecord
import io.github.jan.supabase.realtime.broadcastFlow
import kotlinx.coroutines.flow.filter
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

class ChatRepositoryImpl(
    private val supabaseClient: SupabaseClient,
    private val messagesDao: MessagesDao,
    private val dispatchers: DispatchersProvider,
    private val appScope: CoroutineScope
) : ChatRepository {

    private var realtimeJob: Job? = null
    private val channel = supabaseClient.channel("public:messages")

    override fun getConversation(otherUserId: String): Flow<List<Message>> {
        val currentUserId = supabaseClient.auth.currentUserOrNull()?.id ?: return kotlinx.coroutines.flow.emptyFlow()
        return messagesDao.getConversation(currentUserId, otherUserId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getRecentConversations(): Flow<List<Message>> {
        val currentUserId = supabaseClient.auth.currentUserOrNull()?.id ?: return kotlinx.coroutines.flow.emptyFlow()
        return messagesDao.getRecentConversations(currentUserId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun sendMessage(receiverId: String, content: String?, songId: String?): Resource<Unit> {
        return withContext(dispatchers.io) {
            try {
                val currentUserId = supabaseClient.auth.currentUserOrNull()?.id
                    ?: throw Exception("User not authenticated")

                val type = if (songId != null) "song" else "text"
                val dto = MessageDto(
                    id = java.util.UUID.randomUUID().toString(),
                    senderId = currentUserId,
                    receiverId = receiverId,
                    content = content,
                    messageType = type,
                    songId = songId,
                    status = "sent",
                    createdAt = java.time.Instant.now().toString()
                )

                // Save locally first for offline support
                messagesDao.insertMessage(dto.toEntity())

                // Then send to Supabase
                supabaseClient.postgrest["messages"].insert(dto)
                
                Resource.Success(Unit)
            } catch (e: Exception) {
                Resource.Error(e.message, e)
            }
        }
    }

    override suspend fun markAsRead(otherUserId: String): Resource<Unit> {
        return withContext(dispatchers.io) {
            try {
                val currentUserId = supabaseClient.auth.currentUserOrNull()?.id
                    ?: throw Exception("User not authenticated")

                messagesDao.markMessagesAsRead(currentUserId, otherUserId)
                
                // Update remote
                supabaseClient.postgrest["messages"]
                    .update({
                        set("status", "read")
                    }) {
                        filter {
                            eq("receiver_id", currentUserId)
                            eq("sender_id", otherUserId)
                            neq("status", "read")
                        }
                    }
                Resource.Success(Unit)
            } catch (e: Exception) {
                Resource.Error(e.message, e)
            }
        }
    }

    override suspend fun startListeningForMessages() {
        val currentUserId = supabaseClient.auth.currentUserOrNull()?.id ?: return
        
        if (realtimeJob?.isActive == true) return

        try {
            // First, fetch missed messages
            val missedMessages = supabaseClient.postgrest["messages"]
                .select {
                    filter {
                        or {
                            eq("receiver_id", currentUserId)
                            eq("sender_id", currentUserId)
                        }
                    }
                }
                .decodeList<MessageDto>()
                
            messagesDao.insertMessages(missedMessages.map { it.toEntity() })

            // Subscribe to real-time changes
            val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "messages"
            }
            
            // Also need to listen for our own messages if we use multiple devices, but simplified for now
            val sentChangeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "messages"
            }
            
            channel.subscribe()

            realtimeJob = appScope.launch(dispatchers.io) {
                launch {
                    changeFlow.collect { action ->
                        when (action) {
                            is PostgresAction.Insert -> {
                                val msg = action.decodeRecord<MessageDto>()
                                messagesDao.insertMessage(msg.toEntity())
                            }
                            is PostgresAction.Update -> {
                                val msg = action.decodeRecord<MessageDto>()
                                messagesDao.insertMessage(msg.toEntity())
                            }
                            is PostgresAction.Delete -> {}
                            is PostgresAction.Select -> {}
                        }
                    }
                }
                launch {
                    sentChangeFlow.collect { action ->
                        when (action) {
                            is PostgresAction.Insert -> {
                                val msg = action.decodeRecord<MessageDto>()
                                messagesDao.insertMessage(msg.toEntity())
                            }
                            is PostgresAction.Update -> {
                                val msg = action.decodeRecord<MessageDto>()
                                messagesDao.insertMessage(msg.toEntity())
                            }
                            is PostgresAction.Delete -> {}
                            is PostgresAction.Select -> {}
                        }
                    }
                }
            }
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun stopListeningForMessages() {
        realtimeJob?.cancel()
        realtimeJob = null
        try {
            channel.unsubscribe()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun setTypingStatus(receiverId: String, isTyping: Boolean) {
        try {
            val currentUserId = supabaseClient.auth.currentUserOrNull()?.id ?: return
            channel.broadcast(
                event = "typing",
                message = buildJsonObject {
                    put("senderId", currentUserId)
                    put("receiverId", receiverId)
                    put("isTyping", isTyping)
                }
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun observeTypingStatus(userId: String): Flow<Boolean> {
        val currentUserId = supabaseClient.auth.currentUserOrNull()?.id ?: return kotlinx.coroutines.flow.emptyFlow()
        return channel.broadcastFlow<JsonObject>("typing")
            .filter { payload ->
                val senderId = payload["senderId"]?.jsonPrimitive?.content
                val receiverId = payload["receiverId"]?.jsonPrimitive?.content
                senderId == userId && receiverId == currentUserId
            }
            .map { payload ->
                payload["isTyping"]?.jsonPrimitive?.booleanOrNull ?: false
            }
    }
}
