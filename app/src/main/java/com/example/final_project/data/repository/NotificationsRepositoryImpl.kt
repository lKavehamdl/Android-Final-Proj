package com.example.final_project.data.repository

import com.example.final_project.core.datastore.SettingsDataStore
import com.example.final_project.core.dispatchers.DispatchersProvider
import com.example.final_project.core.result.Resource
import com.example.final_project.data.local.dao.NotificationsDao
import com.example.final_project.data.local.entity.NotificationEntity
import com.example.final_project.data.remote.dto.FollowDto
import com.example.final_project.domain.model.AppNotification
import com.example.final_project.domain.model.NotificationType
import com.example.final_project.domain.repository.NotificationsRepository
import com.example.final_project.domain.repository.SocialRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant

class NotificationsRepositoryImpl(
    private val supabaseClient: SupabaseClient,
    private val notificationsDao: NotificationsDao,
    private val socialRepository: SocialRepository,
    private val settingsDataStore: SettingsDataStore,
    private val dispatchers: DispatchersProvider,
    private val appScope: CoroutineScope,
) : NotificationsRepository {

    private var realtimeJob: Job? = null
    private val channel = supabaseClient.channel("public:follows")

    // Same lifecycle concern as ChatRepositoryImpl: this is a singleton that
    // outlives one login, so on account switch we wipe the previous user's
    // cached notifications before listening as the new user.
    private var lastListenedUserId: String? = null

    override fun observeNotifications(): Flow<List<AppNotification>> {
        val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return flowOf(emptyList())
        return notificationsDao.getNotifications(userId).map { list -> list.map { it.toDomain() } }
    }

    override fun observeUnreadCount(): Flow<Int> {
        val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return flowOf(0)
        return notificationsDao.getUnreadCount(userId)
    }

    override suspend fun markAllRead() {
        val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return
        withContext(dispatchers.io) { notificationsDao.markAllRead(userId) }
    }

    override suspend fun deleteNotification(id: String) {
        withContext(dispatchers.io) { notificationsDao.deleteById(id) }
    }

    override suspend fun deleteAll() {
        val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return
        withContext(dispatchers.io) { notificationsDao.deleteAllForOwner(userId) }
    }

    override suspend fun recordMessage(
        messageId: String,
        senderId: String,
        senderName: String,
        senderAvatarUrl: String?,
        preview: String,
        createdAt: String,
    ) {
        val me = supabaseClient.auth.currentUserOrNull()?.id ?: return
        withContext(dispatchers.io) {
            notificationsDao.insert(
                NotificationEntity(
                    id = "message:$messageId",
                    ownerId = me,
                    type = "message",
                    actorId = senderId,
                    actorName = senderName,
                    actorAvatarUrl = senderAvatarUrl,
                    preview = preview,
                    createdAt = createdAt,
                    isRead = false,
                )
            )
        }
    }

    override suspend fun startListening() {
        val currentUserId = supabaseClient.auth.currentUserOrNull()?.id ?: return

        if (lastListenedUserId != null && lastListenedUserId != currentUserId) {
            stopListening()
            notificationsDao.clearAll()
        }
        lastListenedUserId = currentUserId

        // Reconcile followers that changed while we were away (works even if the
        // follows table isn't in the realtime publication).
        reconcileFollowers(currentUserId)

        if (realtimeJob?.isActive == true) return

        try {
            val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "follows"
            }
            channel.subscribe()

            realtimeJob = appScope.launch(dispatchers.io) {
                changeFlow.collect {
                    // Any follows change: re-diff our own follower set. Cheap for
                    // this app's scale and avoids version-specific delete decoding.
                    reconcileFollowers(currentUserId)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Compares the current follower set against the last-seen snapshot and emits a
     * follow / unfollow notification per change. On the very first run (no snapshot)
     * it just seeds the snapshot so pre-existing followers aren't announced as new.
     */
    private suspend fun reconcileFollowers(currentUserId: String) {
        try {
            val currentFollowers = supabaseClient.postgrest["follows"]
                .select { filter { eq("following_id", currentUserId) } }
                .decodeList<FollowDto>()
                .map { it.followerId }
                .toSet()

            val previous = settingsDataStore.getFollowersSnapshot(currentUserId)
            if (previous == null) {
                settingsDataStore.setFollowersSnapshot(currentUserId, currentFollowers)
                return
            }
            if (currentFollowers == previous) return

            val newFollowers = currentFollowers - previous
            val lostFollowers = previous - currentFollowers
            val now = Instant.now().toString()

            newFollowers.forEach { actorId ->
                insertSocialNotification(currentUserId, actorId, NotificationType.FOLLOW, now)
            }
            lostFollowers.forEach { actorId ->
                insertSocialNotification(currentUserId, actorId, NotificationType.UNFOLLOW, now)
            }

            settingsDataStore.setFollowersSnapshot(currentUserId, currentFollowers)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun insertSocialNotification(
        ownerId: String,
        actorId: String,
        type: NotificationType,
        createdAt: String,
    ) {
        val profile = (socialRepository.getUserProfile(actorId) as? Resource.Success)?.data
        val name = profile?.displayName ?: profile?.username ?: "Someone"
        val typeStr = if (type == NotificationType.FOLLOW) "follow" else "unfollow"
        notificationsDao.insert(
            NotificationEntity(
                id = "$typeStr:$actorId:$createdAt",
                ownerId = ownerId,
                type = typeStr,
                actorId = actorId,
                actorName = name,
                actorAvatarUrl = profile?.avatarUrl,
                preview = null,
                createdAt = createdAt,
                isRead = false,
            )
        )
    }

    override suspend fun stopListening() {
        realtimeJob?.cancel()
        realtimeJob = null
        try {
            channel.unsubscribe()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun clearLocalData() {
        stopListening()
        try {
            notificationsDao.clearAll()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        lastListenedUserId = null
    }

    private fun NotificationEntity.toDomain() = AppNotification(
        id = id,
        type = when (type) {
            "follow" -> NotificationType.FOLLOW
            "unfollow" -> NotificationType.UNFOLLOW
            else -> NotificationType.MESSAGE
        },
        actorId = actorId,
        actorName = actorName,
        actorAvatarUrl = actorAvatarUrl,
        preview = preview,
        createdAt = createdAt,
        isRead = isRead,
    )
}
