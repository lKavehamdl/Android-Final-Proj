package com.example.final_project.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProfileDto(
    @SerialName("id") val id: String,
    @SerialName("username") val username: String,
    @SerialName("display_name") val displayName: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("bio") val bio: String? = null,
    @SerialName("is_premium") val isPremium: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("last_seen") val lastSeen: String? = null
)

@Serializable
data class FollowDto(
    @SerialName("follower_id") val followerId: String,
    @SerialName("following_id") val followingId: String,
    @SerialName("created_at") val createdAt: String? = null
)
