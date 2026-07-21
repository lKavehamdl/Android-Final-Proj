package com.example.final_project.domain.repository

import com.example.final_project.domain.model.AppNotification
import kotlinx.coroutines.flow.Flow

/**
 * In-app notifications: new messages, new followers and unfollows.
 *
 * Message notifications are recorded by [ChatRepository]'s realtime handler via
 * [recordMessage]; follow/unfollow notifications are produced by [startListening],
 * which subscribes to the follows table and reconciles the follower set on start.
 */
interface NotificationsRepository {

    /** All notifications for the signed-in user, newest first. */
    fun observeNotifications(): Flow<List<AppNotification>>

    /** Number of unread notifications — drives the bell badge dot. */
    fun observeUnreadCount(): Flow<Int>

    /** Marks every notification for the signed-in user as read (clears the dot). */
    suspend fun markAllRead()

    /** Removes a single notification. */
    suspend fun deleteNotification(id: String)

    /** Removes every notification for the signed-in user. */
    suspend fun deleteAll()

    /** Records an incoming-message notification. Called from the chat realtime handler. */
    suspend fun recordMessage(
        messageId: String,
        senderId: String,
        senderName: String,
        senderAvatarUrl: String?,
        preview: String,
        createdAt: String,
    )

    /** Starts listening for follow/unfollow changes and reconciles missed ones. */
    suspend fun startListening()

    /** Stops the follows subscription. */
    suspend fun stopListening()

    /** Clears cached notifications (e.g. on logout / account switch). */
    suspend fun clearLocalData()
}
