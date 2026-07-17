package com.example.final_project.core.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.final_project.R

/**
 * Creates Melodify notification channels and helpers for runtime permission checks.
 * Media3 MediaSessionService posts the playback notification; this ensures channels exist
 * and the app can request POST_NOTIFICATIONS on API 33+.
 */
object NotificationHelper {

    const val CHANNEL_PLAYBACK = "melodify_playback"
    const val CHANNEL_DOWNLOAD = "melodify_download"
    const val CHANNEL_SOCIAL = "melodify_social"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return

        val channels = listOf(
            NotificationChannel(
                CHANNEL_PLAYBACK,
                context.getString(R.string.notification_channel_playback),
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = context.getString(R.string.notification_channel_playback)
                setShowBadge(false)
            },
            NotificationChannel(
                CHANNEL_DOWNLOAD,
                context.getString(R.string.notification_channel_download),
                NotificationManager.IMPORTANCE_DEFAULT,
            ),
            NotificationChannel(
                CHANNEL_SOCIAL,
                context.getString(R.string.notification_channel_social),
                NotificationManager.IMPORTANCE_DEFAULT,
            ),
        )
        manager.createNotificationChannels(channels)
    }

    fun hasPostNotificationsPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    }
}
