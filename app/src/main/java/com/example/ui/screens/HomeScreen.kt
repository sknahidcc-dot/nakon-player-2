package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LocalVideo
import com.example.player.PlayerUiState
import com.example.ui.components.YouTubeFilterChips
import com.example.ui.components.YouTubeVideoCard
import com.example.ui.theme.YouTubeRed

@Composable
fun HomeScreen(
    uiState: PlayerUiState,
    onVideoClick: (LocalVideo) -> Unit,
    onFilterSelect: (String) -> Unit,
    onRequestPermission: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Filter Chips Row (All, Downloads, Camera, Movies, Shorts)
        YouTubeFilterChips(
            selectedFilter = uiState.selectedFilter,
            onFilterSelected = onFilterSelect
        )

        if (uiState.isLoadingVideos) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = YouTubeRed)
            }
        } else if (uiState.filteredVideos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = if (uiState.searchQuery.isNotEmpty()) Icons.Default.SearchOff else Icons.Default.FolderOpen,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (uiState.searchQuery.isNotEmpty()) "No videos found for '${uiState.searchQuery}'" else "No offline videos in this category",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Make sure you grant storage permission or check your phone's media storage.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onRefresh,
                        colors = ButtonDefaults.buttonColors(containerColor = YouTubeRed)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Rescan Device")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("home_video_list"),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(uiState.filteredVideos, key = { it.id }) { video ->
                    YouTubeVideoCard(
                        video = video,
                        onClick = { onVideoClick(video) },
                        onPlayInMiniPlayer = { onVideoClick(video) },
                        onPlayInBackground = { onVideoClick(video) }
                    )
                }
            }
        }
    }
}
