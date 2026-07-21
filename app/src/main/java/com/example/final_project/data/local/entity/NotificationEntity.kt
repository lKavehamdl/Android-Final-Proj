package com.example.final_project.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * An in-app notification shown on the Notifications screen and reflected as the
 * unread dot on the top-bar bell. Populated locally from the chat realtime
 * handler (new messages) and the follows realtime/reconcile logic (follow /
 * unfollow). [ownerId] is the recipient so the list stays correct across account
 * switches.
 */
@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val ownerId: String,
    val type: String,          // 'message' | 'follow' | 'unfollow'
    val actorId: String,
    val actorName: String,
    val actorAvatarUrl: String?,
    val preview: String?,      // message text; null for follow/unfollow
    val createdAt: String,     // ISO-8601
    val isRead: Boolean = false,
)
