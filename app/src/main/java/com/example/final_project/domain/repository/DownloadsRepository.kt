package com.example.final_project.domain.repository

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.final_project.data.local.dao.DownloadsDao
import com.example.final_project.data.local.entity.toDownloadedSong
import com.example.final_project.data.worker.DownloadWorker
import com.example.final_project.domain.model.DownloadedSong
import com.example.final_project.domain.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File

class DownloadsRepository(
    private val context: Context,
    private val downloadsDao: DownloadsDao
) {
    val downloadedSongs: Flow<List<DownloadedSong>> = downloadsDao.getAllDownloads().map { list ->
        list.map { it.toDownloadedSong() }
    }

    suspend fun getLocalPath(songId: String): String? {
        return downloadsDao.getDownloadById(songId)?.localFilePath
    }

    fun startDownload(song: Song) {
        val inputData = DownloadWorker.createInputData(
            songId = song.id,
            title = song.title,
            artistName = song.artistName,
            coverImageUrl = song.coverImageUrl,
            audioUrl = song.audioUrl
        )

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setConstraints(constraints)
            .setInputData(inputData)
            .build()

        // Enqueue unique work to avoid multiple downloads of the same song
        WorkManager.getInstance(context).enqueueUniqueWork(
            "download_${song.id}",
            ExistingWorkPolicy.KEEP,
            request
        )
    }

    suspend fun deleteDownload(songId: String) {
        val entity = downloadsDao.getDownloadById(songId)
        if (entity != null) {
            val file = File(entity.localFilePath)
            if (file.exists()) {
                file.delete()
            }
            downloadsDao.deleteDownload(songId)
        }
    }
}
