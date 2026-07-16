package com.example.final_project.domain.model

data class UserProfile(
    val id: String,
    val username: String,
    val displayName: String?,
    val avatarUrl: String?,
    val bio: String?,
    val isPremium: Boolean,
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val isFollowing: Boolean = false // Specific to the currently logged-in user viewing this profile
)
