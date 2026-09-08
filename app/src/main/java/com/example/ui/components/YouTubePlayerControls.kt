package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.ClosedCaptionOff
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LocalVideo
import com.example.player.RepeatState
import com.example.player.SeekDirection
import com.example.player.VideoResizeMode
import com.example.ui.theme.YouTubeRed
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.abs

@Composable
fun YouTubePlayerControls(
    video: LocalVideo,
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    bufferedPosition: Long,
    isFullscreen: Boolean,
    isAutoPlay: Boolean,
    repeatState: RepeatState,
    isSubtitlesEnabled: Boolean,
    resizeMode: VideoResizeMode = VideoResizeMode.FIT,
    resizeHudMessage: String? = null,
    // Gesture States from ViewModel
    volumePercent: Int,
    isVolumeHudVisible: Boolean,
    brightnessPercent: Int,
    isBrightnessHudVisible: Boolean,
    seekDifferenceSeconds: Int,
    isSeekHudVisible: Boolean,
    doubleTapSide: SeekDirection?,
    // Event Callbacks
    onPlayPause: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onDoubleTapSeek: (Boolean) -> Unit,
    onHorizontalDragSeek: (Int) -> Unit,
    onVolumeAdjust: (Float) -> Unit,
    onBrightnessAdjust: (Float) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onCollapse: () -> Unit,
    onToggleFullscreen: () -> Unit,
    onToggleResizeMode: () -> Unit = {},
    onToggleAutoPlay: () -> Unit,
    onToggleSubtitles: () -> Unit,
    onOpenSettings: () -> Unit,
    onEnterPiP: () -> Unit,
    modifier: Modifier = Modifier
) {
    var areControlsVisible by remember { mutableStateOf(true) }
    var isDraggingSlider by remember { mutableStateOf(false) }
    var sliderTempPosition by remember { mutableFloatStateOf(0f) }

    // Auto-hide controls after 4 seconds of inactivity if playing
    LaunchedEffect(areControlsVisible, isPlaying, isDraggingSlider) {
        if (areControlsVisible && isPlaying && !isDraggingSlider) {
            delay(4000)
            areControlsVisible = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            // Gesture handler for Taps & Double Taps
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        areControlsVisible = !areControlsVisible
                    },
                    onDoubleTap = { offset ->
                        val isRightSide = offset.x > size.width / 2f
                        onDoubleTapSeek(isRightSide)
                    }
                )
            }
            // Gesture handler for vertical (volume/brightness) and horizontal (seek) drags
            .pointerInput(Unit) {
                var totalDragX = 0f
                var totalDragY = 0f
                var isHorizontalDrag = false
                var isVerticalDrag = false
                var startX = 0f

                detectDragGestures(
                    onDragStart = { offset ->
                        startX = offset.x
                        totalDragX = 0f
                        totalDragY = 0f
                        isHorizontalDrag = false
                        isVerticalDrag = false
                    },
                    onDragEnd = {
                        totalDragX = 0f
                        totalDragY = 0f
                        isHorizontalDrag = false
                        isVerticalDrag = false
                    },
                    onDragCancel = {
                        totalDragX = 0f
                        totalDragY = 0f
                        isHorizontalDrag = false
                        isVerticalDrag = false
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        totalDragX += dragAmount.x
                        totalDragY += dragAmount.y

                        if (!isHorizontalDrag && !isVerticalDrag) {
                            if (abs(totalDragX) > 20f && abs(totalDragX) > abs(totalDragY)) {
                                isHorizontalDrag = true
                            } else if (abs(totalDragY) > 20f && abs(totalDragY) > abs(totalDragX)) {
                                isVerticalDrag = true
                            }
                        }

                        if (isHorizontalDrag) {
                            // 10 pixels roughly 1 second seek
                            val deltaSecs = (dragAmount.x / 8f).toInt()
                            if (deltaSecs != 0) {
                                onHorizontalDragSeek(deltaSecs)
                            }
                        } else if (isVerticalDrag) {
                            val deltaPercent = -(dragAmount.y / 6f)
                            val isRightSide = startX > size.width / 2f
                            if (isRightSide) {
                                onVolumeAdjust(deltaPercent)
                            } else {
                                onBrightnessAdjust(deltaPercent)
                            }
                        }
                    }
                )
            }
    ) {
        // ================= Double Tap Animation Overlays =================
        AnimatedVisibility(
            visible = doubleTapSide == SeekDirection.BACKWARD_10,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(Color(0x77000000)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.FastRewind,
                        contentDescription = "Rewind",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = "10 seconds",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = doubleTapSide == SeekDirection.FORWARD_10,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(Color(0x77000000)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.FastForward,
                        contentDescription = "Forward",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = "10 seconds",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // ================= Vertical Volume HUD =================
        AnimatedVisibility(
            visible = isVolumeHudVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xCC111111))
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Volume",
                        tint = YouTubeRed,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Volume: $volumePercent%",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { volumePercent / 100f },
                            modifier = Modifier
                                .width(90.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = YouTubeRed,
                            trackColor = Color.DarkGray
                        )
                    }
                }
            }
        }

        // ================= Vertical Brightness HUD =================
        AnimatedVisibility(
            visible = isBrightnessHudVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xCC111111))
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.BrightnessMedium,
                        contentDescription = "Brightness",
                        tint = Color(0xFFFFD54F),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Brightness: $brightnessPercent%",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { brightnessPercent / 100f },
                            modifier = Modifier
                                .width(90.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = Color(0xFFFFD54F),
                            trackColor = Color.DarkGray
                        )
                    }
                }
            }
        }

        // ================= Horizontal Seek HUD =================
        AnimatedVisibility(
            visible = isSeekHudVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 70.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xCC1A1A1A))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                val formattedTarget = formatTime(currentPosition)
                val sign = if (seekDifferenceSeconds >= 0) "+" else ""
                Text(
                    text = "$sign${seekDifferenceSeconds}s  [$formattedTarget]",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // ================= Resize Mode HUD Badge =================
        AnimatedVisibility(
            visible = resizeHudMessage != null,
            enter = fadeIn() + scaleIn(initialScale = 0.85f),
            exit = fadeOut() + scaleOut(targetScale = 0.85f),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xDD111111))
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FitScreen,
                        contentDescription = null,
                        tint = YouTubeRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = resizeHudMessage ?: "",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // ================= Main Controls Overlay =================
        AnimatedVisibility(
            visible = areControlsVisible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x80000000))
            ) {
                // Top Action Bar inside player
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .align(Alignment.TopStart),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Down Chevron to minimize/collapse
                    IconButton(
                        onClick = onCollapse,
                        modifier = Modifier.testTag("player_collapse_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Collapse",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Text(
                        text = video.title,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    // AutoPlay Toggle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Text(
                            text = "Auto",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Switch(
                            checked = isAutoPlay,
                            onCheckedChange = { onToggleAutoPlay() },
                            modifier = Modifier.size(width = 36.dp, height = 24.dp),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = YouTubeRed,
                                uncheckedThumbColor = Color.LightGray,
                                uncheckedTrackColor = Color.DarkGray
                            )
                        )
                    }

                    // CC / Subtitles Toggle
                    IconButton(
                        onClick = onToggleSubtitles,
                        modifier = Modifier.testTag("subtitles_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isSubtitlesEnabled) Icons.Default.ClosedCaption else Icons.Default.ClosedCaptionOff,
                            contentDescription = "Subtitles",
                            tint = if (isSubtitlesEnabled) YouTubeRed else Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // PiP button
                    IconButton(
                        onClick = onEnterPiP,
                        modifier = Modifier.testTag("pip_player_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureInPictureAlt,
                            contentDescription = "PiP",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Settings / Playback Options
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.testTag("player_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Center Play / Pause / Next / Prev Controls
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(28.dp)
                ) {
                    // Previous Video
                    IconButton(
                        onClick = onPrevious,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0x33000000))
                            .testTag("player_previous_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Previous",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Play / Pause big button
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color(0x55000000)),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = onPlayPause,
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("player_play_pause_button")
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = Color.White,
                                modifier = Modifier.size(38.dp)
                            )
                        }
                    }

                    // Next Video
                    IconButton(
                        onClick = onNext,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0x33000000))
                            .testTag("player_next_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // Bottom Controls: Time, Slider Scrubber, Fullscreen
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    // Ultra-sleek, pixel-perfect YouTube Timeline Scrubber (3dp thin line)
                    YouTubeTimelineScrubber(
                        currentPosition = currentPosition,
                        duration = duration,
                        bufferedPosition = bufferedPosition,
                        onSeekTo = onSeekTo,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 2.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Current Time / Duration
                        Text(
                            text = "${formatTime(currentPosition)} / ${formatTime(duration)}",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        // Fit to Screen / Resize Mode Button
                        IconButton(
                            onClick = onToggleResizeMode,
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("fit_to_screen_toggle_button")
                        ) {
                            Icon(
                                imageVector = when (resizeMode) {
                                    VideoResizeMode.FIT -> Icons.Default.AspectRatio
                                    VideoResizeMode.ZOOM -> Icons.Default.FitScreen
                                    VideoResizeMode.FILL -> Icons.Default.CropFree
                                },
                                contentDescription = "Fit to Screen",
                                tint = if (resizeMode != VideoResizeMode.FIT) YouTubeRed else Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Fullscreen button
                        IconButton(
                            onClick = onToggleFullscreen,
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("fullscreen_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                contentDescription = if (isFullscreen) "Exit Fullscreen" else "Fullscreen",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatTime(millis: Long): String {
    if (millis <= 0) return "0:00"
    val totalSecs = millis / 1000
    val hours = totalSecs / 3600
    val minutes = (totalSecs % 3600) / 60
    val seconds = totalSecs % 60
    return if (hours > 0) {
        String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.US, "%d:%02d", minutes, seconds)
    }
}
