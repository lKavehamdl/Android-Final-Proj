package com.example.final_project.data.remote

import com.example.final_project.core.constants.AppConstants
import com.example.final_project.data.remote.dto.SongDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Thin HTTP client for the Supabase `songs` table via PostgREST.
 *
 * Base URL and auth headers are configured in [com.example.final_project.di.networkModule]
 * via Ktor's `defaultRequest` block — no per-call auth needed here.
 *
 * All PostgREST filter operators:
 *   eq, neq, lt, gt, lte, gte, like, ilike, in, is, not
 *
 * All functions are suspend — callers must run on Dispatchers.IO (enforced in Repository).
 */
class SongApiService(
    private val client: HttpClient,
) {

    // ── Full list ─────────────────────────────────────────────────────────────

    /** Returns all songs ordered alphabetically by title. */
    suspend fun fetchSongs(): List<SongDto> =
        client.get("rest/v1/${AppConstants.SONGS_TABLE}") {
            parameter("select", "*")
            parameter("order", "title.asc")
        }.body()

    // ── Single song ───────────────────────────────────────────────────────────

    /** Fetches exactly one song by primary key. Throws if not found. */
    suspend fun fetchSongById(id: String): SongDto =
        client.get("rest/v1/${AppConstants.SONGS_TABLE}") {
            parameter("id", "eq.$id")
            parameter("select", "*")
        }.body<List<SongDto>>().first()

    // ── Curated lists (mapped from SQL functions) ─────────────────────────────

    /**
     * Trending songs — calls the Supabase RPC `get_trending_songs(limit_count)`.
     * Defined in 03_functions_and_realtime.sql.
     */
    suspend fun fetchTrendingSongs(limit: Int = AppConstants.DEFAULT_PAGE_SIZE): List<SongDto> =
        client.post("rest/v1/rpc/get_trending_songs") {
            contentType(ContentType.Application.Json)
            setBody(buildJsonObject { put("limit_count", limit) })
        }.body()

    /**
     * Newest releases — calls the Supabase RPC `get_new_releases(limit_count)`.
     */
    suspend fun fetchNewReleases(limit: Int = AppConstants.DEFAULT_PAGE_SIZE): List<SongDto> =
        client.post("rest/v1/rpc/get_new_releases") {
            contentType(ContentType.Application.Json)
            setBody(buildJsonObject { put("limit_count", limit) })
        }.body()

    /**
     * Iranian / local music — PostgREST filter on `is_local = true`.
     */
    suspend fun fetchLocalSongs(): List<SongDto> =
        client.get("rest/v1/${AppConstants.SONGS_TABLE}") {
            parameter("is_local", "eq.true")
            parameter("select", "*")
            parameter("order", "play_count.desc")
        }.body()

    /**
     * Full-text search — calls the Supabase RPC `search_songs(search_query)`.
     * Uses ILIKE on title + artist_name (see 03_functions_and_realtime.sql).
     *
     * @param query  Raw user-typed string. Debounce is applied upstream in the ViewModel.
     */
    suspend fun searchSongs(query: String): List<SongDto> =
        client.post("rest/v1/rpc/search_songs") {
            contentType(ContentType.Application.Json)
            setBody(buildJsonObject { put("search_query", query) })
        }.body()

    // ── Mutation ──────────────────────────────────────────────────────────────

    /**
     * Increments play_count server-side via the `increment_play_count` RPC.
     * Fire-and-forget — repository catches any exceptions silently.
     */
    suspend fun incrementPlayCount(songId: String) {
        client.post("rest/v1/rpc/increment_play_count") {
            contentType(ContentType.Application.Json)
            setBody(buildJsonObject { put("p_song_id", songId) })
        }
    }

    suspend fun fetchLikedSongs(): List<SongDto> =
        client.get("rest/v1/songs") {
            parameter("select", "*,liked_songs!inner(user_id)")
        }.body()

    suspend fun toggleLike(songId: String): Boolean =
        client.post("rest/v1/rpc/toggle_like") {
            contentType(ContentType.Application.Json)
            // Wait, toggle_like expects p_user_id and p_song_id. 
            // In PostgREST, authenticated user id is taken from auth token if p_user_id is omitted or we can omit it if RPC is defined without it.
            // Let's pass null for p_user_id if it's optional, or we might need the userId.
            // Actually, we can get current user in Ktor? The backend RPC toggle_like in Supabase probably takes p_user_id and p_song_id.
            // I'll assume the client just sends p_song_id if the RPC uses auth.uid(), or I will send it.
            // Let me look at toggle_like RPC.
        }.body()
}
