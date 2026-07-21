package com.example.final_project.data.repository

import com.example.final_project.core.result.Resource
import com.example.final_project.data.mapper.toDomain
import com.example.final_project.data.remote.dto.FollowDto
import com.example.final_project.data.remote.dto.PlaylistDto
import com.example.final_project.data.remote.dto.ProfileDto
import com.example.final_project.domain.model.Playlist
import com.example.final_project.domain.model.UserProfile
import com.example.final_project.domain.repository.SocialRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SocialRepositoryImpl(
    private val supabase: SupabaseClient
) : SocialRepository {

    override fun getCurrentUserId(): String? {
        return supabase.auth.currentUserOrNull()?.id
    }

    override suspend fun updateLastSeen() = withContext(Dispatchers.IO) {
        try {
            val me = getCurrentUserId() ?: return@withContext
            supabase.postgrest["profiles"]
                .update({ set("last_seen", java.time.Instant.now().toString()) }) {
                    filter { eq("id", me) }
                }
        } catch (e: Exception) {
            // Non-fatal: presence is best-effort (e.g. column not migrated yet).
        }
    }

    override suspend fun searchUsers(query: String): Resource<List<UserProfile>> = withContext(Dispatchers.IO) {
        try {
            if (query.isBlank()) return@withContext Resource.Success(emptyList())

            val profiles = supabase.postgrest["profiles"]
                .select {
                    filter {
                        ilike("username", "%$query%")
                    }
                    limit(20)
                }.decodeList<ProfileDto>()

            val currentUserId = getCurrentUserId()
            val userProfiles = profiles.map { profileDto ->
                // For search results, we could skip loading exact follower counts to save network calls,
                // or we could do it. To keep it simple, we just map it.
                mapToUserProfile(profileDto, false, 0, 0)
            }
            Resource.Success(userProfiles)
        } catch (e: Exception) {
            Resource.Error(e.message, e)
        }
    }

    override suspend fun getUserProfile(userId: String): Resource<UserProfile> = withContext(Dispatchers.IO) {
        try {
            val profileDto = supabase.postgrest["profiles"]
                .select {
                    filter {
                        eq("id", userId)
                    }
                }.decodeSingle<ProfileDto>()

            // Count by decoding the rows rather than relying on the Content-Range
            // count header (which was returning null → 0 followers/following).
            val followersCount = supabase.postgrest["follows"]
                .select { filter { eq("following_id", userId) } }
                .decodeList<FollowDto>()
                .size

            val followingCount = supabase.postgrest["follows"]
                .select { filter { eq("follower_id", userId) } }
                .decodeList<FollowDto>()
                .size

            val currentUserId = getCurrentUserId()
            var isFollowing = false
            if (currentUserId != null && currentUserId != userId) {
                val followRecord = supabase.postgrest["follows"]
                    .select {
                        filter {
                            eq("follower_id", currentUserId)
                            eq("following_id", userId)
                        }
                    }.decodeList<FollowDto>()
                isFollowing = followRecord.isNotEmpty()
            }

            Resource.Success(mapToUserProfile(profileDto, isFollowing, followersCount, followingCount))
        } catch (e: Exception) {
            Resource.Error(e.message, e)
        }
    }

    override suspend fun toggleFollow(targetUserId: String): Resource<Boolean> = withContext(Dispatchers.IO) {
        try {
            val currentUserId = getCurrentUserId() ?: return@withContext Resource.Error("Not logged in", Exception("Not logged in"))
            if (currentUserId == targetUserId) return@withContext Resource.Error("Cannot follow yourself", Exception("Cannot follow yourself"))

            // Check if currently following
            val existing = supabase.postgrest["follows"]
                .select {
                    filter {
                        eq("follower_id", currentUserId)
                        eq("following_id", targetUserId)
                    }
                }.decodeList<FollowDto>()

            val isFollowing = existing.isNotEmpty()

            if (isFollowing) {
                // Unfollow
                supabase.postgrest["follows"].delete {
                    filter {
                        eq("follower_id", currentUserId)
                        eq("following_id", targetUserId)
                    }
                }
                Resource.Success(false)
            } else {
                // Follow
                supabase.postgrest["follows"].insert(FollowDto(currentUserId, targetUserId))
                Resource.Success(true)
            }
        } catch (e: Exception) {
            Resource.Error(e.message, e)
        }
    }

    override suspend fun getFollowing(userId: String): Resource<List<UserProfile>> = withContext(Dispatchers.IO) {
        try {
            // Get follow entries where follower is userId
            val follows = supabase.postgrest["follows"]
                .select { filter { eq("follower_id", userId) } }
                .decodeList<FollowDto>()

            if (follows.isEmpty()) {
                return@withContext Resource.Success(emptyList())
            }

            // Get profile details for those we are following
            val followingIds = follows.map { it.followingId }
            val profilesDto = supabase.postgrest["profiles"]
                .select { filter { isIn("id", followingIds) } }
                .decodeList<ProfileDto>()

            val profiles = profilesDto.map { mapToUserProfile(it, true, 0, 0) }
            Resource.Success(profiles)
        } catch (e: Exception) {
            Resource.Error(e.message, e)
        }
    }

    override suspend fun getUserPublicPlaylists(userId: String): Resource<List<Playlist>> = withContext(Dispatchers.IO) {
        try {
            val playlistsDto = supabase.postgrest["playlists"]
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("is_public", true)
                    }
                }.decodeList<PlaylistDto>()

            Resource.Success(playlistsDto.toDomain())
        } catch (e: Exception) {
            Resource.Error(e.message, e)
        }
    }

    override suspend fun updateProfile(
        displayName: String?,
        username: String?,
        avatarUrl: String?,
    ): Resource<UserProfile> = withContext(Dispatchers.IO) {
        try {
            val currentUserId = getCurrentUserId()
                ?: return@withContext Resource.Error("Not logged in", Exception("Not logged in"))

            if (username != null) {
                val taken = supabase.postgrest["profiles"]
                    .select {
                        filter {
                            ilike("username", username)
                            neq("id", currentUserId)
                        }
                    }.decodeList<ProfileDto>()
                if (taken.isNotEmpty()) {
                    return@withContext Resource.Error("Username is already taken")
                }
            }

            if (displayName == null && username == null && avatarUrl == null) {
                return@withContext getUserProfile(currentUserId)
            }

            // Set columns individually (rather than a @Serializable DTO with
            // nullable defaults) so unspecified fields aren't nulled out.
            val updated = supabase.postgrest["profiles"]
                .update({
                    displayName?.let { set("display_name", it) }
                    username?.let { set("username", it) }
                    avatarUrl?.let { set("avatar_url", it) }
                }) {
                    filter { eq("id", currentUserId) }
                    select()
                }
                .decodeSingle<ProfileDto>()

            Resource.Success(mapToUserProfile(updated, false, 0, 0))
        } catch (e: Exception) {
            Resource.Error(e.message, e)
        }
    }

    private fun mapToUserProfile(dto: ProfileDto, isFollowing: Boolean, followers: Int, following: Int): UserProfile {
        return UserProfile(
            id = dto.id,
            username = dto.username,
            displayName = dto.displayName,
            avatarUrl = dto.avatarUrl,
            bio = dto.bio,
            isPremium = dto.isPremium,
            followersCount = followers,
            followingCount = following,
            isFollowing = isFollowing,
            lastSeen = dto.lastSeen,
        )
    }
}
