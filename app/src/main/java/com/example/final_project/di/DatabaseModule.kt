package com.example.final_project.di

import org.koin.dsl.module

/**
 * Room database and DataStore bindings — Phase 2.
 */
val databaseModule = module {
    // single { provideRoomDatabase(get()) }
    // single { get<MelodifyDatabase>().searchHistoryDao() }
}
