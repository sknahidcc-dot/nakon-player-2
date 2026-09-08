package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.player.DarkThemeOption
import com.example.ui.theme.YouTubeRed

@Composable
fun YouTubeTopBar(
    isSearchOpen: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onToggleSearch: (Boolean) -> Unit,
    darkThemeOption: DarkThemeOption,
    onToggleDarkTheme: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        if (isSearchOpen) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onToggleSearch(false) },
                    modifier = Modifier.testTag("close_search_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = "Search offline videos...",
                            style = TextStyle(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 15.sp
                            )
                        )
                    }
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        singleLine = true,
                        cursorBrush = SolidColor(YouTubeRed),
                        textStyle = TextStyle(
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 15.sp
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_input_field")
                    )
                }

                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { onSearchQueryChange("") },
                        modifier = Modifier.testTag("clear_search_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // YouTube style logo + Nakon-Player text
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { /* Reset or scroll to top */ }
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp, 22.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(YouTubeRed),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Logo",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Nakon",
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                    Text(
                        text = "Player",
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Normal,
                            letterSpacing = (-0.5).sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Cast icon
                IconButton(onClick = { /* Cast to screen */ }) {
                    Icon(
                        imageVector = Icons.Default.Cast,
                        contentDescription = "Cast",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Dark/Light mode switcher
                IconButton(
                    onClick = onToggleDarkTheme,
                    modifier = Modifier.testTag("theme_toggle_button")
                ) {
                    Icon(
                        imageVector = if (darkThemeOption == DarkThemeOption.DARK) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Toggle Theme",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Search icon
                IconButton(
                    onClick = { onToggleSearch(true) },
                    modifier = Modifier.testTag("open_search_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Notifications
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Default.NotificationsNone,
                        contentDescription = "Notifications",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(2.dp))

                // User Profile Avatar (Developer Nahid Hasan)
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(YouTubeRed)
                        .clickable { onProfileClick() }
                        .testTag("profile_avatar_topbar"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "NH",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
