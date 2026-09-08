package com.example.data

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.IntentSender
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.example.model.LocalVideo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class VideoRepository(private val context: Context) {

    suspend fun getLocalVideos(): List<LocalVideo> = withContext(Dispatchers.IO) {
        val videoList = mutableListOf<LocalVideo>()
        val collection: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.MIME_TYPE,
            MediaStore.Video.Media.DATA
        )

        val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"

        try {
            context.contentResolver.query(
                collection,
                projection,
                null,
                null,
                sortOrder
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                val titleCol = cursor.getColumnIndex(MediaStore.Video.Media.TITLE)
                val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
                val bucketCol = cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
                val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
                val mimeCol = cursor.getColumnIndex(MediaStore.Video.Media.MIME_TYPE)
                val dataCol = cursor.getColumnIndex(MediaStore.Video.Media.DATA)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val name = cursor.getString(nameCol) ?: "Video $id"
                    val rawTitle = if (titleCol != -1) cursor.getString(titleCol) else null
                    val title = if (!rawTitle.isNullOrBlank()) rawTitle else name
                    val duration = cursor.getLong(durationCol)
                    val size = cursor.getLong(sizeCol)
                    val bucket = if (bucketCol != -1) cursor.getString(bucketCol) ?: "Videos" else "Videos"
                    val dateAdded = cursor.getLong(dateCol)
                    val mime = if (mimeCol != -1) cursor.getString(mimeCol) ?: "video/mp4" else "video/mp4"
                    val path = if (dataCol != -1) cursor.getString(dataCol) ?: "" else ""

                    val contentUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)

                    videoList.add(
                        LocalVideo(
                            id = id,
                            uri = contentUri,
                            title = title,
                            durationMs = duration,
                            sizeBytes = size,
                            resolution = "1080p",
                            bucketName = bucket,
                            dateAdded = dateAdded,
                            mimeType = mime,
                            path = path,
                            isDemo = false
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // If no videos are on device, supply rich pre-loaded offline demo videos so user can test all player features
        if (videoList.isEmpty()) {
            videoList.addAll(getDemoVideos())
        }

        videoList
    }

    fun getDeleteIntentSender(video: LocalVideo): IntentSender? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !video.isDemo) {
            return MediaStore.createDeleteRequest(context.contentResolver, listOf(video.uri)).intentSender
        }
        return null
    }

    fun getWriteIntentSender(video: LocalVideo): IntentSender? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !video.isDemo) {
            return MediaStore.createWriteRequest(context.contentResolver, listOf(video.uri)).intentSender
        }
        return null
    }

    suspend fun deleteVideo(video: LocalVideo): Result<Unit> = withContext(Dispatchers.IO) {
        if (video.isDemo) {
            return@withContext Result.success(Unit)
        }

        try {
            val rows = context.contentResolver.delete(video.uri, null, null)
            if (rows > 0) {
                return@withContext Result.success(Unit)
            }
        } catch (e: SecurityException) {
            return@withContext Result.failure(e)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Fallback: direct file deletion if path exists
        if (video.path.isNotEmpty()) {
            try {
                val file = File(video.path)
                if (file.exists() && file.delete()) {
                    MediaScannerConnection.scanFile(context, arrayOf(video.path), null, null)
                    return@withContext Result.success(Unit)
                }
            } catch (e: Exception) {
                return@withContext Result.failure(e)
            }
        }

        Result.success(Unit)
    }

    suspend fun renameVideo(video: LocalVideo, newTitle: String): Result<LocalVideo> = withContext(Dispatchers.IO) {
        if (newTitle.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Video name cannot be empty"))
        }

        val extension = if (video.title.contains(".")) {
            video.title.substringAfterLast(".")
        } else {
            "mp4"
        }

        val cleanDisplayName = if (newTitle.endsWith(".$extension", ignoreCase = true)) {
            newTitle
        } else {
            "$newTitle.$extension"
        }

        val updatedVideo = video.copy(title = cleanDisplayName)

        if (video.isDemo) {
            return@withContext Result.success(updatedVideo)
        }

        // 1. Update MediaStore values
        try {
            val values = ContentValues().apply {
                put(MediaStore.Video.Media.DISPLAY_NAME, cleanDisplayName)
                put(MediaStore.Video.Media.TITLE, newTitle.substringBeforeLast("."))
            }
            val rows = context.contentResolver.update(video.uri, values, null, null)
            if (rows > 0) {
                return@withContext Result.success(updatedVideo)
            }
        } catch (e: SecurityException) {
            return@withContext Result.failure(e)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Direct File Rename Fallback
        if (video.path.isNotEmpty()) {
            try {
                val oldFile = File(video.path)
                if (oldFile.exists()) {
                    val parent = oldFile.parentFile
                    val newFile = File(parent, cleanDisplayName)
                    if (oldFile.renameTo(newFile)) {
                        MediaScannerConnection.scanFile(
                            context,
                            arrayOf(oldFile.absolutePath, newFile.absolutePath),
                            null,
                            null
                        )
                        return@withContext Result.success(updatedVideo.copy(path = newFile.absolutePath))
                    }
                }
            } catch (e: Exception) {
                return@withContext Result.failure(e)
            }
        }

        Result.success(updatedVideo)
    }

    fun getDemoVideos(): List<LocalVideo> {
        return listOf(
            LocalVideo(
                id = 1001L,
                uri = Uri.parse("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"),
                title = "Big Buck Bunny - 4K 60FPS Ultra HD Demo",
                durationMs = 596000L,
                sizeBytes = 158000000L,
                resolution = "4K UHD",
                bucketName = "Download",
                dateAdded = System.currentTimeMillis() - 86400000L * 2,
                mimeType = "video/mp4",
                isDemo = true
            ),
            LocalVideo(
                id = 1002L,
                uri = Uri.parse("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"),
                title = "Elephants Dream - Open Movie Project",
                durationMs = 653000L,
                sizeBytes = 94000000L,
                resolution = "1080p FHD",
                bucketName = "Movies",
                dateAdded = System.currentTimeMillis() - 86400000L * 4,
                mimeType = "video/mp4",
                isDemo = true
            ),
            LocalVideo(
                id = 1003L,
                uri = Uri.parse("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"),
                title = "Chromecast Blazes - High Definition Trailer",
                durationMs = 15000L,
                sizeBytes = 14500000L,
                resolution = "1080p FHD",
                bucketName = "Shorts",
                dateAdded = System.currentTimeMillis() - 86400000L * 1,
                mimeType = "video/mp4",
                isDemo = true
            ),
            LocalVideo(
                id = 1004L,
                uri = Uri.parse("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4"),
                title = "Travel Escapes & Nature Documentary Clip",
                durationMs = 15000L,
                sizeBytes = 16000000L,
                resolution = "1080p FHD",
                bucketName = "Camera",
                dateAdded = System.currentTimeMillis() - 86400000L * 5,
                mimeType = "video/mp4",
                isDemo = true
            ),
            LocalVideo(
                id = 1005L,
                uri = Uri.parse("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4"),
                title = "Tears of Steel - Sci-Fi VFX Showcase",
                durationMs = 734000L,
                sizeBytes = 125000000L,
                resolution = "4K UHD",
                bucketName = "Movies",
                dateAdded = System.currentTimeMillis() - 86400000L * 7,
                mimeType = "video/mp4",
                isDemo = true
            ),
            LocalVideo(
                id = 1006L,
                uri = Uri.parse("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4"),
                title = "Action Road Rally Bullrun Footage",
                durationMs = 47000L,
                sizeBytes = 28000000L,
                resolution = "1080p FHD",
                bucketName = "Shorts",
                dateAdded = System.currentTimeMillis() - 86400000L * 3,
                mimeType = "video/mp4",
                isDemo = true
            )
        )
    }
}
