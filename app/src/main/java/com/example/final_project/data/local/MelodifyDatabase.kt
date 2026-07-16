package com.example.final_project.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.final_project.data.local.dao.DownloadsDao
import com.example.final_project.data.local.entity.DownloadedSongEntity

@Database(
    entities = [DownloadedSongEntity::class],
    version = 1,
    exportSchema = false
)
abstract class MelodifyDatabase : RoomDatabase() {
    abstract fun downloadsDao(): DownloadsDao
}
