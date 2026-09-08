package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.model.LocalVideo
import com.example.player.PlayerUiState
import com.example.player.RepeatState
import com.example.player.SeekDirection
import com.example.ui.theme.YouTubeRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YouTubeWatchPage(
    uiState: PlayerUiState,
    exoPlayer: ExoPlayer,
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
    onToggleAutoPlay: () -> Unit,
    onToggleSubtitles: () -> Unit,
    onSetPlaybackSpeed: (Float) -> Unit,
    onToggleRepeatMode: () -> Unit,
    onToggleBackgroundPlay: () -> Unit,
    onAttachSubtitle: (Uri) -> Unit,
    onEnterPiP: () -> Unit,
    onSelectVideo: (LocalVideo) -> Unit,
    onToggleLike: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentVideo = uiState.currentVideo ?: return
    var showSettingsSheet by remember { mutableStateOf(false) }
    var isDescriptionExpanded by remember { mutableStateOf(false) }

    // Subtitle file picker launcher (.srt, .vtt, .ass)
    val subtitlePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { onAttachSubtitle(it) }
    }

    val isLiked = uiState.likedVideoIds.contains(currentVideo.id)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ================= VIDEO PLAYER CONTAINER =================
        Box(
            modifier = if (uiState.isFullscreen) {
                Modifier.fillMaxSize()
            } else {
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
            }
                .background(Color.Black)
        ) {
            // Android Media3 PlayerView
            AndroidView(
                factory = { context ->
                    PlayerView(context).apply {
                        useController = false
                        player = exoPlayer
                        setShowSubtitleButton(false)
                    }
                },
                update = { view ->
                    if (view.player != exoPlayer) {
                        view.player = exoPlayer
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Custom YouTube Gestures and Controls Overlay
            YouTubePlayerControls(
                video = currentVideo,
                isPlaying = uiState.isPlaying,
                currentPosition = uiState.currentPosition,
                duration = uiState.duration,
                bufferedPosition = uiState.bufferedPosition,
                isFullscreen = uiState.isFullscreen,
                isAutoPlay = uiState.isAutoPlayEnabled,
                repeatState = uiState.repeatState,
                isSubtitlesEnabled = uiState.isSubtitlesEnabled,
                volumePercent = uiState.volumePercent,
                isVolumeHudVisible = uiState.isVolumeHudVisible,
                brightnessPercent = uiState.brightnessPercent,
                isBrightnessHudVisible = uiState.isBrightnessHudVisible,
                seekDifferenceSeconds = uiState.seekDifferenceSeconds,
                isSeekHudVisible = uiState.isSeekHudVisible,
                doubleTapSide = uiState.doubleTapSide,
                onPlayPause = onPlayPause,
                onSeekTo = onSeekTo,
                onDoubleTapSeek = onDoubleTapSeek,
                onHorizontalDragSeek = onHorizontalDragSeek,
                onVolumeAdjust = onVolumeAdjust,
                onBrightnessAdjust = onBrightnessAdjust,
                onNext = onNext,
                onPrevious = onPrevious,
                onCollapse = onCollapse,
                onToggleFullscreen = onToggleFullscreen,
                onToggleAutoPlay = onToggleAutoPlay,
                onToggleSubtitles = onToggleSubtitles,
                onOpenSettings = { showSettingsSheet = true },
                onEnterPiP = onEnterPiP
            )
        }

        // When fullscreen, only the video surface is shown
        if (!uiState.isFullscreen) {
            // ================= VIDEO DETAILS & UP NEXT =================
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                // Title and Meta
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        // Title
                        Text(
                            text = currentVideo.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                lineHeight = 22.sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Video meta stats
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { isDescriptionExpanded = !isDescriptionExpanded }
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "${currentVideo.resolution} • ${currentVideo.formattedSize} • ${currentVideo.bucketName}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isDescriptionExpanded) "Show less" else "...more",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        // Expanded Description Box (YouTube style)
                        AnimatedVisibility(visible = isDescriptionExpanded) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "File Information",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Path: ${if (currentVideo.path.isNotEmpty()) currentVideo.path else currentVideo.uri.toString()}\nMIME: ${currentVideo.mimeType}\nDuration: ${currentVideo.formattedDuration}\nSubtitles: ${if (uiState.subtitleUri != null) "Active" else "None attached"}",
                                        fontSize = 12.sp,
                                        lineHeight = 18.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Channel / Folder bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(YouTubeRed),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = currentVideo.bucketName.firstOrNull()?.uppercase() ?: "L",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = currentVideo.bucketName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "Offline Local Media",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Folder",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // YouTube Action Pill Buttons (Like, Subtitle, Speed, PiP, Audio)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Like Pill
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { onToggleLike(currentVideo.id) }
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isLiked) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                                    contentDescription = "Like",
                                    tint = if (isLiked) YouTubeRed else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isLiked) "Liked" else "Like",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // Subtitle Pill
                            PillButton(
                                icon = Icons.Default.Subtitles,
                                label = if (uiState.subtitleUri != null) "CC Active" else "Load CC",
                                isActive = uiState.subtitleUri != null,
                                onClick = {
                                    // Open system file picker for subtitles (.srt, .vtt)
                                    subtitlePicker.launch(arrayOf("*/*"))
                                }
                            )

                            // Speed Pill
                            PillButton(
                                icon = Icons.Default.Speed,
                                label = "${uiState.playbackSpeed}x Speed",
                                isActive = uiState.playbackSpeed != 1.0f,
                                onClick = { showSettingsSheet = true }
                            )

                            // Repeat Pill
                            val repeatLabel = when (uiState.repeatState) {
                                RepeatState.OFF -> "Repeat Off"
                                RepeatState.ONE -> "Repeat 1"
                                RepeatState.ALL -> "Repeat All"
                            }
                            PillButton(
                                icon = if (uiState.repeatState == RepeatState.ONE) Icons.Default.RepeatOne else Icons.Default.Repeat,
                                label = repeatLabel,
                                isActive = uiState.repeatState != RepeatState.OFF,
                                onClick = onToggleRepeatMode
                            )

                            // Background Audio Play Pill
                            PillButton(
                                icon = Icons.Default.Headphones,
                                label = if (uiState.isBackgroundPlayEnabled) "Background On" else "Background Off",
                                isActive = uiState.isBackgroundPlayEnabled,
                                onClick = onToggleBackgroundPlay
                            )

                            // Floating PiP Pill
                            PillButton(
                                icon = Icons.Default.PictureInPictureAlt,
                                label = "Pop-up PiP",
                                isActive = false,
                                onClick = onEnterPiP
                            )
                        }
                    }
                }

                item {
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                // Up Next Section Header
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Up Next (Local Videos)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "Autoplay: ${if (uiState.isAutoPlayEnabled) "ON" else "OFF"}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (uiState.isAutoPlayEnabled) YouTubeRed else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Up Next List
                val otherVideos = uiState.playlist.filter { it.id != currentVideo.id }
                items(otherVideos, key = { it.id }) { video ->
                    YouTubeVideoCard(
                        video = video,
                        onClick = { onSelectVideo(video) },
                        onPlayInMiniPlayer = { onSelectVideo(video) },
                        onPlayInBackground = { onSelectVideo(video) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(48.dp))
                }
            }
        }
    }

    // Playback Settings Bottom Sheet
    if (showSettingsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSettingsSheet = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Playback Settings",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Playback Speed",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                val speeds = listOf(0.25f, 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    speeds.forEach { speed ->
                        val isSelected = uiState.playbackSpeed == speed
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (isSelected) YouTubeRed else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable {
                                    onSetPlaybackSpeed(speed)
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = if (speed == 1.0f) "Normal" else "${speed}x",
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Repeat Mode Options
                Text(
                    text = "Repeat Mode",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        RepeatState.OFF to "Off",
                        RepeatState.ONE to "Loop Current",
                        RepeatState.ALL to "Repeat All"
                    ).forEach { (state, title) ->
                        val isSelected = uiState.repeatState == state
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) YouTubeRed else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable {
                                    if (!isSelected) onToggleRepeatMode()
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Load Subtitle button inside sheet
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable {
                            showSettingsSheet = false
                            subtitlePicker.launch(arrayOf("*/*"))
                        }
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Subtitles,
                            contentDescription = null,
                            tint = YouTubeRed
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Load Subtitle File (.srt, .vtt)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (uiState.subtitleUri != null) "Active subtitle attached" else "Choose from phone storage",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun PillButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (isActive) YouTubeRed.copy(alpha = 0.2f)
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isActive) YouTubeRed else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium,
            color = if (isActive) YouTubeRed else MaterialTheme.colorScheme.onSurface
        )
    }
}
