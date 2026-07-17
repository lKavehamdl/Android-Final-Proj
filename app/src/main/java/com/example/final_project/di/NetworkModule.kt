package com.example.final_project.di

import com.example.final_project.core.constants.AppConstants
import com.example.final_project.data.remote.SongApiService
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import com.example.final_project.data.remote.PlaylistApiService

val networkModule = module {
    single {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            explicitNulls = false
        }
    }

    single {
        io.github.jan.supabase.createSupabaseClient(
            supabaseUrl = AppConstants.BASE_URL,
            supabaseKey = AppConstants.SUPABASE_ANON_KEY
        ) {
            install(io.github.jan.supabase.auth.Auth)
            install(io.github.jan.supabase.postgrest.Postgrest)
            install(io.github.jan.supabase.realtime.Realtime)
            install(io.github.jan.supabase.storage.Storage)
        }
    }

    single {
        HttpClient(Android) {
            expectSuccess = true

            install(ContentNegotiation) {
                json(get())
            }

            install(Logging) {
                level = LogLevel.INFO
            }

            defaultRequest {
                url(AppConstants.BASE_URL)
                contentType(ContentType.Application.Json)
                header("apikey", AppConstants.SUPABASE_ANON_KEY)
                header("Authorization", "Bearer ${AppConstants.SUPABASE_ANON_KEY}")
            }
        }
    }

    single { SongApiService(client = get()) }
    single { PlaylistApiService(client = get()) }
}
