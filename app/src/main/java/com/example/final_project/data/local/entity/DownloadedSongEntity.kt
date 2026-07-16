package com.example.final_project.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.final_project.domain.model.DownloadedSong
import com.example.final_project.domain.model.Song
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "downloaded_songs")
data class DownloadedSongEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val artistName: String,
    val coverImageUrl: String,
    val audioUrl: String,
    val localFilePath: String,
    val downloadedAt: Long = System.currentTimeMillis()
)

fun DownloadedSongEntity.toDomainModel(): Song {
    return Song(
        id = id,
        title = title,
        artistName = artistName,
        albumName = null,
        coverImageUrl = coverImageUrl,
        audioUrl = audioUrl, // original remote URL
        durationSeconds = 0, // not strictly needed for the downloaded list UI
        genre = "Unknown",
        isLocal = false,
        playCount = 0,
        createdAt = ""
    )
}

fun DownloadedSongEntity.toDownloadedSong(): DownloadedSong {
    return DownloadedSong(
        id = id,
        song = toDomainModel(),
        localPath = localFilePath,
        fileSizeBytes = File(localFilePath).length(),
        downloadedAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date(downloadedAt))
    )
}

fun Song.toDownloadedEntity(localFilePath: String): DownloadedSongEntity {
    return DownloadedSongEntity(
        id = id,
        title = title,
        artistName = artistName,
        coverImageUrl = coverImageUrl ?: "",
        audioUrl = audioUrl,
        localFilePath = localFilePath
    )
}
