package com.example.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.YouTubeRed

/**
 * Ultra-sleek, pixel-perfect YouTube timeline scrubber bar.
 * Has a thin, elegant 3dp track, smooth buffered indicators, and an interactive red thumb.
 */
@Composable
fun YouTubeTimelineScrubber(
    currentPosition: Long,
    duration: Long,
    bufferedPosition: Long,
    onSeekTo: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragFraction by remember { mutableFloatStateOf(0f) }

    val safeDuration = duration.coerceAtLeast(1L)
    val playbackFraction = (currentPosition.toFloat() / safeDuration).coerceIn(0f, 1f)
    val bufferedFraction = (bufferedPosition.toFloat() / safeDuration).coerceIn(0f, 1f)
    val effectiveFraction = if (isDragging) dragFraction else playbackFraction

    // Subtle track expansion on touch (3dp -> 5dp)
    val trackHeightDp by animateDpAsState(
        targetValue = if (isDragging) 5.dp else 3.dp,
        animationSpec = tween(durationMillis = 150),
        label = "track_height"
    )

    // Red Scrubber Thumb size expansion on touch (8dp -> 14dp)
    val thumbRadiusDp by animateDpAsState(
        targetValue = if (isDragging) 7.dp else 4.5.dp,
        animationSpec = tween(durationMillis = 150),
        label = "thumb_radius"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp) // Generous touch target
            .testTag("video_timeline_scrubber")
            .pointerInput(safeDuration) {
                detectTapGestures { offset ->
                    val frac = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                    onSeekTo((frac * safeDuration).toLong())
                }
            }
            .pointerInput(safeDuration) {
                detectHorizontalDragGestures(
                    onDragStart = { offset ->
                        isDragging = true
                        dragFraction = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        val newFraction = (dragFraction + (dragAmount / size.width.toFloat())).coerceIn(0f, 1f)
                        dragFraction = newFraction
                    },
                    onDragEnd = {
                        isDragging = false
                        onSeekTo((dragFraction * safeDuration).toLong())
                    },
                    onDragCancel = {
                        isDragging = false
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(16.dp)) {
            val width = size.width
            val centerY = size.height / 2f
            val trackHeightPx = trackHeightDp.toPx()
            val thumbRadiusPx = thumbRadiusDp.toPx()

            // 1. Inactive Track (Subtle translucent grey)
            drawRoundRect(
                color = Color(0x3DFFFFFF),
                topLeft = Offset(0f, centerY - (trackHeightPx / 2f)),
                size = Size(width, trackHeightPx),
                cornerRadius = CornerRadius(trackHeightPx / 2f)
            )

            // 2. Buffered Track (Lighter semi-white)
            val bufferedWidth = width * bufferedFraction
            if (bufferedWidth > 0f) {
                drawRoundRect(
                    color = Color(0x70FFFFFF),
                    topLeft = Offset(0f, centerY - (trackHeightPx / 2f)),
                    size = Size(bufferedWidth, trackHeightPx),
                    cornerRadius = CornerRadius(trackHeightPx / 2f)
                )
            }

            // 3. Active Progress Track (YouTube Red)
            val activeWidth = width * effectiveFraction
            if (activeWidth > 0f) {
                drawRoundRect(
                    color = YouTubeRed,
                    topLeft = Offset(0f, centerY - (trackHeightPx / 2f)),
                    size = Size(activeWidth, trackHeightPx),
                    cornerRadius = CornerRadius(trackHeightPx / 2f)
                )
            }

            // 4. Red Thumb Dot
            val thumbCenter = Offset(activeWidth.coerceIn(thumbRadiusPx, width - thumbRadiusPx), centerY)

            // Outer subtle glow while scrubbing
            if (isDragging) {
                drawCircle(
                    color = YouTubeRed.copy(alpha = 0.35f),
                    radius = thumbRadiusPx + 4.dp.toPx(),
                    center = thumbCenter
                )
            }

            // Main Red Scrubber Circle
            drawCircle(
                color = YouTubeRed,
                radius = thumbRadiusPx,
                center = thumbCenter
            )
        }
    }
}
