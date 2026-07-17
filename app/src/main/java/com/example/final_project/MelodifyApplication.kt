package com.example.final_project

import android.app.Application
import com.example.final_project.core.notification.NotificationHelper
import com.example.final_project.di.appModule
import com.example.final_project.di.databaseModule
import com.example.final_project.di.networkModule
import com.example.final_project.di.repositoryModule
import com.russhwolf.settings.SettingsInitializer
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class MelodifyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Ensure multiplatform-settings no-arg factory has a Context before Auth touches storage.
        SettingsInitializer().create(this)
        NotificationHelper.createChannels(this)
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@MelodifyApplication)
            modules(
                appModule,
                networkModule,
                databaseModule,
                repositoryModule,
            )
        }
    }
}
