package com.example.final_project.data.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.final_project.core.constants.AppConstants
import com.example.final_project.data.local.dao.DownloadsDao
import com.example.final_project.data.local.entity.DownloadedSongEntity
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.request.get
import io.ktor.client.statement.readBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File

class DownloadWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams), KoinComponent {

    private val downloadsDao: DownloadsDao by inject()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val songId = inputData.getString(KEY_SONG_ID) ?: return@withContext Result.failure()
        val title = inputData.getString(KEY_TITLE) ?: return@withContext Result.failure()
        val artistName = inputData.getString(KEY_ARTIST_NAME) ?: return@withContext Result.failure()
        val coverImageUrl = inputData.getString(KEY_COVER_IMAGE_URL) ?: ""
        val audioUrl = inputData.getString(KEY_AUDIO_URL) ?: return@withContext Result.failure()

        try {
            // 1. Download file using Ktor (we create a fresh client here to avoid complex DI, or we could inject it)
            val client = HttpClient(Android)
            val responseBytes = client.get(audioUrl).readBytes()
            client.close()

            // 2. Save to local storage
            val downloadsDir = File(context.getExternalFilesDir(null), AppConstants.DOWNLOADS_DIRECTORY)
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }
            val fileName = "song_${songId}.mp3"
            val outputFile = File(downloadsDir, fileName)
            outputFile.writeBytes(responseBytes)

            // 3. Save to Room
            val entity = DownloadedSongEntity(
                id = songId,
                title = title,
                artistName = artistName,
                coverImageUrl = coverImageUrl,
                audioUrl = audioUrl,
                localFilePath = outputFile.absolutePath,
                fileSizeBytes = outputFile.length(),
            )
            downloadsDao.insertDownload(entity)

            Result.success()
        } catch (e: Exception) {
            Log.e("DownloadWorker", "Failed to download song $songId", e)
            Result.failure()
        }
    }

    companion object {
        const val KEY_SONG_ID = "SONG_ID"
        const val KEY_TITLE = "TITLE"
        const val KEY_ARTIST_NAME = "ARTIST_NAME"
        const val KEY_COVER_IMAGE_URL = "COVER_IMAGE_URL"
        const val KEY_AUDIO_URL = "AUDIO_URL"

        fun createInputData(
            songId: String,
            title: String,
            artistName: String,
            coverImageUrl: String?,
            audioUrl: String
        ) = workDataOf(
            KEY_SONG_ID to songId,
            KEY_TITLE to title,
            KEY_ARTIST_NAME to artistName,
            KEY_COVER_IMAGE_URL to coverImageUrl,
            KEY_AUDIO_URL to audioUrl
        )
    }
}
