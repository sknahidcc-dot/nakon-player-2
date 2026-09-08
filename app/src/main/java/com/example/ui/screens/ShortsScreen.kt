package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.model.LocalVideo
import com.example.player.PlayerUiState
import com.example.ui.theme.YouTubeRed

@Composable
fun ShortsScreen(
    uiState: PlayerUiState,
    onPlayVideo: (LocalVideo) -> Unit,
    onToggleLike: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val shortsList = uiState.allVideos.filter {
        it.durationMs in 1..90000 || it.bucketName.contains("Short", ignoreCase = true)
    }.ifEmpty { uiState.allVideos }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(shortsList, key = { it.id }) { video ->
                val isLiked = uiState.likedVideoIds.contains(video.id)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(520.dp)
                        .padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black)
                        .clickable { onPlayVideo(video) }
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(video.uri)
                            .crossfade(true)
                            .build(),
                        contentDescription = video.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Dark gradient overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color(0xCC000000)),
                                    startY = 300f
                                )
                            )
                    )

                    // Center Play Button Watermark
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(0x66000000))
                            .align(Alignment.Center),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    // Side action column (Like, Share)
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 12.dp, bottom = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        IconButton(
                            onClick = { onToggleLike(video.id) },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0x44000000))
                        ) {
                            Icon(
                                imageVector = if (isLiked) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                                contentDescription = "Like",
                                tint = if (isLiked) YouTubeRed else Color.White
                            )
                        }

                        IconButton(
                            onClick = { /* Share */ },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0x44000000))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = Color.White
                            )
                        }
                    }

                    // Video Bottom Info
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth(0.8f)
                            .padding(start = 16.dp, bottom = 20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(YouTubeRed),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = video.bucketName.firstOrNull()?.uppercase() ?: "S",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "@${video.bucketName}",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = video.title,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "${video.formattedDuration} • ${video.resolution}",
                            color = Color(0xCCFFFFFF),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}
