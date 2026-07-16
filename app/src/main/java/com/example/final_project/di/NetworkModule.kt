package com.example.final_project.di

import org.koin.dsl.module

/**
 * Network client bindings (Retrofit/Ktor, WebSocket) — Phase 1.
 */
val networkModule = module {
    // single { provideHttpClient() }
    // single { provideSongApi(get()) }
}
