package com.example.final_project.di

import androidx.room.Room
import com.example.final_project.core.datastore.SettingsDataStore
import com.example.final_project.data.local.MelodifyDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Room database and DataStore bindings — Phase 2 & 5.
 */
val databaseModule = module {
    single { SettingsDataStore(androidContext()) }
    
    single {
        Room.databaseBuilder(
            androidContext(),
            MelodifyDatabase::class.java,
            "melodify_database.db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }
    
    single { get<MelodifyDatabase>().downloadsDao() }
    single { get<MelodifyDatabase>().messagesDao() }
}
