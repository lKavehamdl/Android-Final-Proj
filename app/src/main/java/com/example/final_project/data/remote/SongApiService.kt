package com.example.final_project.data.remote

import com.example.final_project.core.constants.AppConstants
import com.example.final_project.data.remote.dto.SongDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

/**
 * Remote API for the Supabase `songs` table.
 *
 * Task A endpoint (PostgREST):
 *   GET {SUPABASE_URL}/rest/v1/songs
 *   Headers: apikey, Authorization: Bearer {anon_key}
 *   (headers are applied in NetworkModule defaultRequest)
 */
class SongApiService(
    private val client: HttpClient,
) {
    suspend fun fetchSongs(): List<SongDto> {
        return client.get("rest/v1/${AppConstants.SONGS_TABLE}") {
            parameter("select", "*")
            parameter("order", "title.asc")
        }.body()
    }

    suspend fun fetchSongById(id: String): SongDto {
        return client.get("rest/v1/${AppConstants.SONGS_TABLE}") {
            parameter("id", "eq.$id")
            parameter("select", "*")
        }.body<List<SongDto>>().first()
    }
}
