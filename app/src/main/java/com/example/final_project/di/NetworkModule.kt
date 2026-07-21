package com.example.final_project.di

import android.content.Context
import com.example.final_project.core.constants.AppConstants
import com.example.final_project.data.remote.PlaylistApiService
import com.example.final_project.data.remote.SongApiService
import com.russhwolf.settings.SharedPreferencesSettings
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.SettingsSessionManager
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val networkModule = module {
    single {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            explicitNulls = false
        }
    }

    single {
        val context: Context = androidContext()
        val supabaseUrl = AppConstants.BASE_URL.trimEnd('/')
        // Explicit SharedPreferences-backed session store.
        // Avoids Settings() no-arg NPE (appContext!!) when androidx.startup hasn't run yet.
        val authSettings = SharedPreferencesSettings(
            context.getSharedPreferences("telify_supabase_auth", Context.MODE_PRIVATE),
        )
        createSupabaseClient(
            supabaseUrl = supabaseUrl,
            supabaseKey = AppConstants.SUPABASE_ANON_KEY,
        ) {
            // The Android engine (HttpURLConnection) can't do WebSockets, which
            // Realtime needs — OkHttp can.
            httpEngine = OkHttp.create()
            install(Auth) {
                sessionManager = SettingsSessionManager(settings = authSettings)
                autoLoadFromStorage = true
                autoSaveToStorage = true
            }
            install(Postgrest)
            install(Realtime)
            install(Storage)
        }
    }

    single {
        val supabase: SupabaseClient = get()
        HttpClient(Android) {
            expectSuccess = true

            install(ContentNegotiation) {
                json(get())
            }

            install(Logging) {
                level = LogLevel.INFO
            }

            defaultRequest {
                url(AppConstants.BASE_URL.trimEnd('/') + "/")
                contentType(ContentType.Application.Json)
                header("apikey", AppConstants.SUPABASE_ANON_KEY)
                // Use the signed-in user's JWT so RLS-protected rows (e.g. private
                // playlists) are visible; fall back to anon before login.
                val token = supabase.auth.currentAccessTokenOrNull()
                    ?: AppConstants.SUPABASE_ANON_KEY
                header("Authorization", "Bearer $token")
            }
        }
    }

    single { SongApiService(client = get()) }
    single { PlaylistApiService(client = get()) }
}
