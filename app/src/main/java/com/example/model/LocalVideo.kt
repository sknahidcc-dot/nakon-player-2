package com.example.model

import android.net.Uri
import java.util.Locale

data class LocalVideo(
    val id: Long,
    val uri: Uri,
    val title: String,
    val durationMs: Long,
    val sizeBytes: Long,
    val resolution: String = "1080p",
    val bucketName: String = "Internal",
    val dateAdded: Long = System.currentTimeMillis(),
    val mimeType: String = "video/mp4",
    val path: String = "",
    val isDemo: Boolean = false,
    val subtitleUri: Uri? = null
) {
    val formattedDuration: String
        get() {
            if (durationMs <= 0) return "0:00"
            val totalSeconds = durationMs / 1000
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60
            return if (hours > 0) {
                String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format(Locale.US, "%d:%02d", minutes, seconds)
            }
        }

    val formattedSize: String
        get() {
            if (sizeBytes <= 0) return "Local File"
            val kb = sizeBytes / 1024.0
            val mb = kb / 1024.0
            val gb = mb / 1024.0
            return when {
                gb >= 1.0 -> String.format(Locale.US, "%.1f GB", gb)
                mb >= 1.0 -> String.format(Locale.US, "%.1f MB", mb)
                else -> String.format(Locale.US, "%.0f KB", kb)
            }
        }
}
