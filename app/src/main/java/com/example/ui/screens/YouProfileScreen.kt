package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.player.DarkThemeOption
import com.example.player.PlayerUiState
import com.example.ui.theme.YouTubeRed

@Composable
fun YouProfileScreen(
    uiState: PlayerUiState,
    onPlayVideo: (LocalVideo) -> Unit,
    onToggleDarkThemeOption: (DarkThemeOption) -> Unit,
    onToggleBackgroundPlay: () -> Unit,
    onToggleAutoPlay: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
            .testTag("you_profile_screen")
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))

            // User & Developer Profile Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(YouTubeRed),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "NH",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "Nahid Hasan",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "@sknahid • Developer Profile",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "sknahid.study@gmail.com",
                        style = MaterialTheme.typography.bodySmall,
                        color = YouTubeRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Developer Credentials Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = null,
                            tint = YouTubeRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Developer Information",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    ProfileInfoRow(
                        icon = Icons.Default.Person,
                        label = "Developer",
                        value = "Nahid Hasan"
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    ProfileInfoRow(
                        icon = Icons.Default.Email,
                        label = "Email",
                        value = "sknahid.study@gmail.com"
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    ProfileInfoRow(
                        icon = Icons.Default.LocationOn,
                        label = "Address",
                        value = "Jhenaidah, Bangladesh"
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    ProfileInfoRow(
                        icon = Icons.Default.Info,
                        label = "App Version",
                        value = "Nakon-Player v21.35.442 (Offline Edition)"
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Recent Videos / History section
        item {
            Text(
                text = "Recent Videos",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(10.dp))

            val recentVideos = uiState.allVideos.take(6)
            if (recentVideos.isEmpty()) {
                Text(
                    text = "No recent videos yet",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(recentVideos, key = { it.id }) { video ->
                        Column(
                            modifier = Modifier
                                .width(140.dp)
                                .clickable { onPlayVideo(video) }
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(140.dp)
                                    .height(84.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Black)
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

                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(4.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(Color(0xCC000000))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = video.formattedDuration,
                                        color = Color.White,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = video.title,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Settings section
        item {
            Text(
                text = "Player & Display Settings",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Dark/Light Theme selection (Automatic / Dark / Light)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(14.dp)
            ) {
                Text(
                    text = "Theme Mode (Auto Switch)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        DarkThemeOption.SYSTEM to "Auto (System)",
                        DarkThemeOption.DARK to "Dark",
                        DarkThemeOption.LIGHT to "Light"
                    ).forEach { (option, title) ->
                        val isSelected = uiState.darkThemeOption == option
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) YouTubeRed else MaterialTheme.colorScheme.surface
                                )
                                .clickable { onToggleDarkThemeOption(option) }
                                .padding(vertical = 8.dp),
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
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Background Play Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Headphones,
                    contentDescription = null,
                    tint = YouTubeRed
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Background Audio Playback",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Continue playing when app is minimized or screen off",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = uiState.isBackgroundPlayEnabled,
                    onCheckedChange = { onToggleBackgroundPlay() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = YouTubeRed
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Auto-play Next Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    tint = YouTubeRed
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Auto-Play Next Video",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Play following local media automatically",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = uiState.isAutoPlayEnabled,
                    onCheckedChange = { onToggleAutoPlay() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = YouTubeRed
                    )
                )
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun ProfileInfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$label: ",
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = value,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
