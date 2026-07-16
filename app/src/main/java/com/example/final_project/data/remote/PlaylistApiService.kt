package com.example.final_project.data.remote

import com.example.final_project.core.constants.AppConstants
import com.example.final_project.data.remote.dto.PlaylistDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

/**
 * Thin HTTP client for the Supabase `playlists` table via PostgREST.
 * Base URL + auth headers configured in networkModule's defaultRequest.
 * All functions suspend — callers run on Dispatchers.IO (enforced in Repository).
 */
class PlaylistApiService(
    private val client: HttpClient,
) {

    /** All public playlists ordered by newest first. */
    suspend fun fetchPublicPlaylists(): List<PlaylistDto> =
        client.get("rest/v1/${AppConstants.PLAYLISTS_TABLE}") {
            parameter("is_public", "eq.true")
            parameter("select", "*")
            parameter("order", "created_at.desc")
        }.body()

    /** Playlists owned by a given user. */
    suspend fun fetchUserPlaylists(userId: String): List<PlaylistDto> =
        client.get("rest/v1/${AppConstants.PLAYLISTS_TABLE}") {
            parameter("user_id", "eq.$userId")
            parameter("select", "*")
            parameter("order", "created_at.desc")
        }.body()
}