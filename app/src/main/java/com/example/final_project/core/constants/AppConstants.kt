package com.example.final_project.core.constants

object AppConstants {

    // Network — update in Phase 1 when backend is chosen
    const val BASE_URL = "https://api.melodify.example/"
    const val WEB_SOCKET_URL = "wss://api.melodify.example/ws"

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
