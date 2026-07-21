package com.example.final_project.domain.model

enum class NotificationType { MESSAGE, FOLLOW, UNFOLLOW }

/**
 * A single in-app notification shown on the Notifications screen.
 */
data class AppNotification(
    val id: String,
    val type: NotificationType,
    val actorId: String,
    val actorName: String,
    val actorAvatarUrl: String?,
    val preview: String?,
    val createdAt: String,
    val isRead: Boolean,
)
