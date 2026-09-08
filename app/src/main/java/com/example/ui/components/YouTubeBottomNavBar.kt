package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.player.BottomTab
import com.example.ui.theme.YouTubeRed

@Composable
fun YouTubeBottomNavBar(
    currentTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
    ) {
        HorizontalDivider(
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.surfaceVariant
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem(
                icon = if (currentTab == BottomTab.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                label = "Home",
                isSelected = currentTab == BottomTab.HOME,
                onClick = { onTabSelected(BottomTab.HOME) },
                testTag = "tab_home"
            )

            NavItem(
                icon = if (currentTab == BottomTab.SHORTS) Icons.Filled.Movie else Icons.Outlined.Movie,
                label = "Shorts",
                isSelected = currentTab == BottomTab.SHORTS,
                onClick = { onTabSelected(BottomTab.SHORTS) },
                testTag = "tab_shorts"
            )

            NavItem(
                icon = if (currentTab == BottomTab.LIBRARY) Icons.Filled.VideoLibrary else Icons.Outlined.VideoLibrary,
                label = "Library",
                isSelected = currentTab == BottomTab.LIBRARY,
                onClick = { onTabSelected(BottomTab.LIBRARY) },
                testTag = "tab_library"
            )

            // You / Profile Tab
            Column(
                modifier = Modifier
                    .clickable { onTabSelected(BottomTab.YOU) }
                    .padding(vertical = 4.dp, horizontal = 12.dp)
                    .testTag("tab_you"),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(if (currentTab == BottomTab.YOU) YouTubeRed else MaterialTheme.colorScheme.surfaceVariant)
                        .border(
                            width = if (currentTab == BottomTab.YOU) 1.5.dp else 0.dp,
                            color = if (currentTab == BottomTab.YOU) YouTubeRed else Color.Transparent,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "NH",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "You",
                    fontSize = 10.sp,
                    fontWeight = if (currentTab == BottomTab.YOU) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (currentTab == BottomTab.YOU) MaterialTheme.colorScheme.onBackground
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun NavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(vertical = 4.dp, horizontal = 12.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
