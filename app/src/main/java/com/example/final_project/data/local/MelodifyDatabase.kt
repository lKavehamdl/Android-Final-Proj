package com.example.final_project.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.final_project.data.local.dao.DownloadsDao
import com.example.final_project.data.local.dao.MessagesDao
import com.example.final_project.data.local.dao.NotificationsDao
import com.example.final_project.data.local.dao.SearchHistoryDao
import com.example.final_project.data.local.entity.DownloadedSongEntity
import com.example.final_project.data.local.entity.MessageEntity
import com.example.final_project.data.local.entity.NotificationEntity
import com.example.final_project.data.local.entity.SearchHistoryEntity

@Database(
    entities = [
        DownloadedSongEntity::class,
        MessageEntity::class,
        SearchHistoryEntity::class,
        NotificationEntity::class,
    ],
    version = 5,
    exportSchema = false,
)
abstract class MelodifyDatabase : RoomDatabase() {
    abstract fun downloadsDao(): DownloadsDao
    abstract fun messagesDao(): MessagesDao
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun notificationsDao(): NotificationsDao
}
