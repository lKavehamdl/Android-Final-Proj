package com.example.final_project.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.final_project.data.local.dao.DownloadsDao
import com.example.final_project.data.local.entity.DownloadedSongEntity
import com.example.final_project.data.local.entity.MessageEntity
import com.example.final_project.data.local.dao.MessagesDao

@Database(
    entities = [DownloadedSongEntity::class, MessageEntity::class],
    version = 2,
    exportSchema = false
)
abstract class MelodifyDatabase : RoomDatabase() {
    abstract fun downloadsDao(): DownloadsDao
    abstract fun messagesDao(): MessagesDao
}
