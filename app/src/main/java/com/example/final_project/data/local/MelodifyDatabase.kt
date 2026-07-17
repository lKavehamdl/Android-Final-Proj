package com.example.final_project.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.final_project.data.local.dao.DownloadsDao
import com.example.final_project.data.local.dao.MessagesDao
import com.example.final_project.data.local.dao.SearchHistoryDao
import com.example.final_project.data.local.entity.DownloadedSongEntity
import com.example.final_project.data.local.entity.MessageEntity
import com.example.final_project.data.local.entity.SearchHistoryEntity

@Database(
    entities = [
        DownloadedSongEntity::class,
        MessageEntity::class,
        SearchHistoryEntity::class,
    ],
    version = 4,
    exportSchema = false,
)
abstract class MelodifyDatabase : RoomDatabase() {
    abstract fun downloadsDao(): DownloadsDao
    abstract fun messagesDao(): MessagesDao
    abstract fun searchHistoryDao(): SearchHistoryDao
}
