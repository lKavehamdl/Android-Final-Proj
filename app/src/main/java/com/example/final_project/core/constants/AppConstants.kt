package com.example.final_project.core.constants

import com.example.final_project.BuildConfig

object AppConstants {

    /**
     * Supabase project URL from Task A (BuildConfig — update in app/build.gradle.kts).
     * Expected REST path: `{BASE_URL}rest/v1/songs`
     */
    val BASE_URL: String = BuildConfig.SUPABASE_URL

    val SUPABASE_ANON_KEY: String = BuildConfig.SUPABASE_ANON_KEY

    /** When true, Koin binds [FakeSongRepository]; set USE_FAKE_SONGS=false to use live API. */
    val USE_FAKE_SONGS: Boolean = BuildConfig.USE_FAKE_SONGS

    const val SONGS_TABLE = "songs"
    const val WEB_SOCKET_URL = "wss://YOUR_PROJECT.supabase.co/realtime/v1"

    // Search
    const val SEARCH_DEBOUNCE_MS = 300L

    // Pagination
    const val DEFAULT_PAGE_SIZE = 20

    // Downloads
    const val DOWNLOADS_DIRECTORY = "melodify_downloads"

    // DataStore keys — wired in Phase 2
    const val PREF_THEME_MODE = "theme_mode"
    const val PREF_LANGUAGE = "language"
    const val PREF_FONT_SCALE = "font_scale"
    const val PREF_IS_PREMIUM = "is_premium"
}
