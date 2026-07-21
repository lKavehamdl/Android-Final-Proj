package com.example.final_project.domain.repository

import com.example.final_project.core.result.Resource
import com.example.final_project.domain.model.Playlist
import com.example.final_project.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface SocialRepository {
    /**
     * Search profiles by username (case-insensitive).
     */
    suspend fun searchUsers(query: String): Resource<List<UserProfile>>

    /**
     * Get a user's profile details.
     */
    suspend fun getUserProfile(userId: String): Resource<UserProfile>

    /**
     * Toggle following status for a user.
     * @return true if currently following, false if unfollowed
     */
    suspend fun toggleFollow(targetUserId: String): Resource<Boolean>

    /**
     * Get a list of users that the target user follows.
     */
    suspend fun getFollowing(userId: String): Resource<List<UserProfile>>

    /**
     * Get a list of public playlists for a specific user.
     */
    suspend fun getUserPublicPlaylists(userId: String): Resource<List<Playlist>>
    
    /**
     * Get the logged-in user's ID
     */
    fun getCurrentUserId(): String?

    /**
     * Updates the signed-in user's `last_seen` timestamp to now (presence heartbeat).
     */
    suspend fun updateLastSeen()

    /**
     * Update the current user's own profile. Only non-null params are changed.
     * @return Error if [username] is already taken by someone else.
     */
    suspend fun updateProfile(
        displayName: String? = null,
        username: String? = null,
        avatarUrl: String? = null,
    ): Resource<UserProfile>
}
