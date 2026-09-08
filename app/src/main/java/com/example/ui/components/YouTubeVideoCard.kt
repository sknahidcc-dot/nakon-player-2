package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.model.LocalVideo
import com.example.ui.theme.YouTubeRed

@Composable
fun YouTubeVideoCard(
    video: LocalVideo,
    onClick: () -> Unit,
    onPlayInMiniPlayer: () -> Unit,
    onPlayInBackground: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(bottom = 16.dp)
            .testTag("video_card_${video.id}")
    ) {
        // 16:9 Thumbnail
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(MaterialTheme.colorScheme.surfaceVariant)
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

            // Fallback decorative gradient overlay for crisp visibility
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color(0x88000000)),
                            startY = 100f
                        )
                    )
            )

            // Center play watermark indicator for video look
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0x55000000))
                    .align(Alignment.Center),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(28.dp)
                )
            }

            // Duration Badge (bottom right corner)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xCC000000))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = video.formattedDuration,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Format Badge (bottom left corner)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0x99000000))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = video.resolution,
                    color = Color(0xFFEEEEEE),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Info Row: Avatar + Title/Details + 3 dots
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Folder / Channel avatar
            val avatarLetter = video.bucketName.firstOrNull()?.uppercase() ?: "V"
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(YouTubeRed),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = avatarLetter,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Title & Meta details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = video.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        lineHeight = 18.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "${video.bucketName} • ${video.formattedSize} • Local Video",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 12.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // 3-dots Menu
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("video_menu_${video.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) {
                    DropdownMenuItem(
                        text = { Text("Play in Floating Player") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.PictureInPictureAlt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        },
                        onClick = {
                            showMenu = false
                            onPlayInMiniPlayer()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Play in Background") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        },
                        onClick = {
                            showMenu = false
                            onPlayInBackground()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Folder: ${video.bucketName}") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Movie,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        },
                        onClick = { showMenu = false }
                    )
                }
            }
        }
    }
}
