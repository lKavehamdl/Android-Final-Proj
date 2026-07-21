package com.example.final_project.data.repository

import android.content.Context
import com.example.final_project.core.datastore.SettingsDataStore
import com.example.final_project.core.dispatchers.DispatchersProvider
import com.example.final_project.core.notification.ActiveChatHolder
import com.example.final_project.core.notification.NotificationHelper
import com.example.final_project.core.result.Resource
import com.example.final_project.data.local.dao.MessagesDao
import com.example.final_project.data.mapper.toDomain
import com.example.final_project.data.mapper.toEntity
import com.example.final_project.data.remote.dto.MessageDto
import com.example.final_project.domain.model.Message
import com.example.final_project.domain.repository.ChatRepository
import com.example.final_project.domain.repository.NotificationsRepository
import com.example.final_project.domain.repository.SocialRepository
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
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import io.github.jan.supabase.realtime.decodeRecord
import io.github.jan.supabase.realtime.broadcastFlow
import kotlinx.coroutines.flow.emptyFlow
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
    private val appScope: CoroutineScope,
    private val appContext: Context,
    private val socialRepository: SocialRepository,
    private val settingsDataStore: SettingsDataStore,
    private val notificationsRepository: NotificationsRepository,
) : ChatRepository {

    private var realtimeJob: Job? = null
    private val channel = supabaseClient.channel("public:messages")

    // The user id this repository last set up listening for. This is a Koin
    // singleton that outlives a single login, so on account switch we must wipe
    // the previous account's cached messages — otherwise the messages page would
    // show the previous user's conversations.
    private var lastListenedUserId: String? = null

    /** Posts a local notification for a live incoming message (not from us, not the open chat). */
    private suspend fun maybeNotify(msg: MessageDto) {
        val me = supabaseClient.auth.currentUserOrNull()?.id ?: return
        if (msg.receiverId != me || msg.senderId == me) return
        if (ActiveChatHolder.activeChatUserId.value == msg.senderId) return

        val senderProfile = (socialRepository.getUserProfile(msg.senderId) as? Resource.Success)?.data
        val senderName = senderProfile?.displayName ?: senderProfile?.username ?: "New message"
        val preview = if (msg.messageType == "song") "🎵" else (msg.content ?: "")
        NotificationHelper.showMessageNotification(
            context = appContext,
            senderName = senderName,
            messagePreview = preview,
            notificationId = msg.id.hashCode(),
        )

        // Mirror it into the in-app notifications list / bell badge.
        notificationsRepository.recordMessage(
            messageId = msg.id,
            senderId = msg.senderId,
            senderName = senderName,
            senderAvatarUrl = senderProfile?.avatarUrl,
            preview = preview,
            createdAt = msg.createdAt,
        )
    }

    override fun getConversation(otherUserId: String): Flow<List<Message>> {
        val currentUserId = supabaseClient.auth.currentUserOrNull()?.id ?: return emptyFlow()
        return messagesDao.getConversation(currentUserId, otherUserId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getConversationPaged(otherUserId: String): Flow<PagingData<Message>> {
        val currentUserId = supabaseClient.auth.currentUserOrNull()?.id ?: return emptyFlow()
        return Pager(
            config = PagingConfig(pageSize = 40, enablePlaceholders = false),
            pagingSourceFactory = {
                messagesDao.getConversationPaged(currentUserId, otherUserId)
            },
        ).flow.map { pagingData ->
            pagingData.map { it.toDomain() }
        }
    }

    override fun observeUnreadCount(): Flow<Int> {
        val currentUserId = supabaseClient.auth.currentUserOrNull()?.id ?: return kotlinx.coroutines.flow.flowOf(0)
        return messagesDao.getUnreadCount(currentUserId)
    }

    override suspend fun deleteMessage(messageId: String): Resource<Unit> {
        return withContext(dispatchers.io) {
            try {
                val currentUserId = supabaseClient.auth.currentUserOrNull()?.id
                    ?: throw Exception("User not authenticated")

                // Server first: if the delete is rejected (not our message / offline)
                // we keep the local row rather than resurrecting it on the next sync.
                supabaseClient.postgrest["messages"].delete {
                    filter {
                        eq("id", messageId)
                        eq("sender_id", currentUserId)
                    }
                }
                messagesDao.deleteById(messageId)
                Resource.Success(Unit)
            } catch (e: Exception) {
                Resource.Error(e.message, e)
            }
        }
    }

    override fun getRecentConversations(): Flow<List<Message>> {
        val currentUserId = supabaseClient.auth.currentUserOrNull()?.id ?: return emptyFlow()
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
                    status = "sending",
                    createdAt = java.time.Instant.now().toString()
                )

                // Optimistic local row while upload is in flight
                messagesDao.insertMessage(dto.toEntity())

                supabaseClient.postgrest["messages"].insert(dto)

                // Mark delivered locally once the server accepted the insert
                messagesDao.insertMessage(dto.copy(status = "delivered").toEntity())
                
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

        // Account switched since we last listened: tear down the old subscription
        // and clear the previous user's cached messages before starting fresh.
        if (lastListenedUserId != null && lastListenedUserId != currentUserId) {
            stopListeningForMessages()
            messagesDao.clearAll()
        }
        lastListenedUserId = currentUserId

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

            // Notify for messages that arrived while we were offline (covers the
            // case where the recipient wasn't listening when the message was sent
            // — e.g. testing two accounts on one device). Only unread inbound
            // messages newer than the last one we already notified for, newest 5.
            val lastNotified = settingsDataStore.getLastNotifiedAt(currentUserId)
            val unreadInbound = missedMessages
                .filter { it.receiverId == currentUserId && it.senderId != currentUserId && it.status != "read" }
                .filter { lastNotified == null || it.createdAt > lastNotified }
                .sortedBy { it.createdAt }
                .takeLast(5)
            unreadInbound.forEach { maybeNotify(it) }
            missedMessages.maxOfOrNull { it.createdAt }?.let {
                settingsDataStore.setLastNotifiedAt(currentUserId, it)
            }

            // Subscribe to real-time changes. (A single flow is enough — the
            // previous second, identical flow just double-processed every event.)
            val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "messages"
            }

            channel.subscribe()

            realtimeJob = appScope.launch(dispatchers.io) {
                changeFlow.collect { action ->
                    when (action) {
                        is PostgresAction.Insert -> {
                            val msg = action.decodeRecord<MessageDto>()
                            if (msg.senderId == currentUserId) {
                                // Echo of our OWN just-sent message: the server row
                                // is still "sending", so writing it verbatim would
                                // knock our local "delivered" (✓✓) back to a clock
                                // (🕒). Confirm delivered instead of downgrading.
                                messagesDao.insertMessage(msg.copy(status = "delivered").toEntity())
                            } else {
                                messagesDao.insertMessage(msg.toEntity())
                                maybeNotify(msg)
                            }
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

    override suspend fun clearLocalData() {
        stopListeningForMessages()
        try {
            messagesDao.clearAll()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        lastListenedUserId = null
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
