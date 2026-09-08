package com.example.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.util.LruCache
import android.util.Size
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import com.example.model.LocalVideo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Global in-memory cache for ultra-fast instant thumbnail rendering
private val thumbnailCache = LruCache<String, Bitmap>(60)

private var globalCoilVideoLoader: ImageLoader? = null

fun getCoilVideoLoader(context: Context): ImageLoader {
    return globalCoilVideoLoader ?: synchronized(ImageLoader::class.java) {
        globalCoilVideoLoader ?: ImageLoader.Builder(context.applicationContext)
            .components {
                add(VideoFrameDecoder.Factory())
            }
            .crossfade(true)
            .build().also { globalCoilVideoLoader = it }
    }
}

@Composable
fun VideoThumbnailImage(
    video: LocalVideo,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current
    val cacheKey = "${video.id}_${video.dateAdded}"
    var cachedBitmap by remember(cacheKey) { mutableStateOf(thumbnailCache.get(cacheKey)) }

    // Load native thumbnail if not yet cached
    LaunchedEffect(cacheKey) {
        if (cachedBitmap == null) {
            val bitmap = withContext(Dispatchers.IO) {
                loadVideoBitmap(context, video)
            }
            if (bitmap != null) {
                thumbnailCache.put(cacheKey, bitmap)
                cachedBitmap = bitmap
            }
        }
    }

    Crossfade(targetState = cachedBitmap, label = "thumbnail_crossfade") { bitmap ->
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = modifier.fillMaxSize()
            )
        } else {
            // Fallback to Coil with VideoFrameDecoder
            val imageLoader = remember { getCoilVideoLoader(context) }
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(video.uri)
                        .videoFrameMillis(1500)
                        .crossfade(true)
                        .build(),
                    imageLoader = imageLoader,
                    contentDescription = contentDescription,
                    contentScale = contentScale,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

private fun loadVideoBitmap(context: Context, video: LocalVideo): Bitmap? {
    // 1. If running on Android 10+ (API 29+), try ContentResolver.loadThumbnail
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !video.isDemo) {
        try {
            val thumb = context.contentResolver.loadThumbnail(
                video.uri,
                Size(640, 360),
                null
            )
            if (thumb != null) return thumb
        } catch (_: Throwable) {
            // Fall through to retriever
        }
    }

    // 2. Try MediaMetadataRetriever
    val retriever = MediaMetadataRetriever()
    try {
        if (video.path.isNotEmpty() && java.io.File(video.path).exists()) {
            retriever.setDataSource(video.path)
        } else if (video.uri.scheme == "content" || video.uri.scheme == "file") {
            retriever.setDataSource(context, video.uri)
        } else {
            // Network demo uri
            retriever.setDataSource(video.uri.toString(), HashMap())
        }

        // Extract frame at 1.5 seconds or closest frame
        val frame = retriever.getFrameAtTime(
            1500000L,
            MediaMetadataRetriever.OPTION_CLOSEST_SYNC
        ) ?: retriever.frameAtTime

        if (frame != null) {
            // Scale down if overly large to conserve memory
            if (frame.width > 720) {
                val ratio = frame.height.toFloat() / frame.width.toFloat()
                val targetWidth = 640
                val targetHeight = (targetWidth * ratio).toInt()
                return Bitmap.createScaledBitmap(frame, targetWidth, targetHeight, true)
            }
            return frame
        }
    } catch (_: Throwable) {
        // Ignored, will fallback to Coil
    } finally {
        try {
            retriever.release()
        } catch (_: Throwable) { }
    }

    return null
}
