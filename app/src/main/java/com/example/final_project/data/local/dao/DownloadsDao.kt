package com.example.final_project.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.final_project.data.local.entity.DownloadedSongEntity
import kotlinx.coroutines.flow.Flow

@Dao
@JvmSuppressWildcards
interface DownloadsDao {
    @Query("SELECT * FROM downloaded_songs ORDER BY downloadedAt DESC")
    fun getAllDownloads(): Flow<List<DownloadedSongEntity>>

    @Query("SELECT * FROM downloaded_songs WHERE id = :songId LIMIT 1")
    suspend fun getDownloadById(songId: String): DownloadedSongEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(song: DownloadedSongEntity): Long

    @Query("DELETE FROM downloaded_songs WHERE id = :songId")
    suspend fun deleteDownload(songId: String): Int
}
