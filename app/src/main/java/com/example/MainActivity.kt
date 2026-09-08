package com.example

import android.Manifest
import android.app.PictureInPictureParams
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.player.BottomTab
import com.example.player.DarkThemeOption
import com.example.player.PlayerViewModel
import com.example.ui.components.YouTubeBottomNavBar
import com.example.ui.components.YouTubeMiniPlayer
import com.example.ui.components.YouTubeTopBar
import com.example.ui.components.YouTubeWatchPage
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.ShortsScreen
import com.example.ui.screens.YouProfileScreen
import com.example.ui.theme.NakonPlayerTheme

class MainActivity : ComponentActivity() {

    private var isPipModeActive by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val viewModel: PlayerViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            // Keep screen on while playing video
            DisposableEffect(uiState.isPlaying) {
                if (uiState.isPlaying) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
                onDispose {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }

            // Sync brightness when adjusted via left vertical swipe HUD
            LaunchedEffect(uiState.brightnessPercent, uiState.isBrightnessHudVisible) {
                if (uiState.isBrightnessHudVisible) {
                    val lp = window.attributes
                    lp.screenBrightness = (uiState.brightnessPercent / 100f).coerceIn(0.01f, 1.0f)
                    window.attributes = lp
                }
            }

            // Fullscreen orientation and system bars handling (Sensor auto-rotation when fullscreen)
            LaunchedEffect(uiState.isFullscreen) {
                val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
                if (uiState.isFullscreen) {
                    windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
                    windowInsetsController.systemBarsBehavior =
                        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    // Auto-rotate with sensor when fullscreen
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
                } else {
                    windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                }
            }

            // Permission check
            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                viewModel.setPermissionGranted(isGranted)
            }

            LaunchedEffect(Unit) {
                val requiredPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Manifest.permission.READ_MEDIA_VIDEO
                } else {
                    Manifest.permission.READ_EXTERNAL_STORAGE
                }

                val hasPermission = ContextCompat.checkSelfPermission(
                    this@MainActivity,
                    requiredPermission
                ) == PackageManager.PERMISSION_GRANTED

                viewModel.setPermissionGranted(hasPermission)
                if (!hasPermission) {
                    permissionLauncher.launch(requiredPermission)
                }
            }

            // Theme selection logic (System / Dark / Light)
            val isDarkTheme = when (uiState.darkThemeOption) {
                DarkThemeOption.SYSTEM -> isSystemInDarkTheme()
                DarkThemeOption.DARK -> true
                DarkThemeOption.LIGHT -> false
            }

            // Handle back button: if in full WatchPage, collapse to MiniPlayer!
            BackHandler(enabled = uiState.currentVideo != null && !uiState.isMiniPlayerActive) {
                if (uiState.isFullscreen) {
                    viewModel.setFullscreen(false)
                } else {
                    viewModel.setMiniPlayer(true)
                }
            }

            NakonPlayerTheme(darkTheme = isDarkTheme) {
                if (isPipModeActive && uiState.currentVideo != null) {
                    // Minimalistic Picture-in-Picture display
                    YouTubeWatchPage(
                        uiState = uiState,
                        exoPlayer = viewModel.exoPlayer,
                        onPlayPause = viewModel::togglePlayPause,
                        onSeekTo = viewModel::seekTo,
                        onDoubleTapSeek = viewModel::doubleTapSeek,
                        onHorizontalDragSeek = viewModel::onHorizontalSeekDrag,
                        onVolumeAdjust = viewModel::adjustVolumeByDelta,
                        onBrightnessAdjust = viewModel::adjustBrightnessByDelta,
                        onNext = viewModel::playNextVideo,
                        onPrevious = viewModel::playPreviousVideo,
                        onCollapse = { viewModel.setMiniPlayer(true) },
                        onToggleFullscreen = viewModel::toggleFullscreen,
                        onToggleAutoPlay = viewModel::toggleAutoPlay,
                        onToggleSubtitles = { viewModel.toggleSubtitles(!uiState.isSubtitlesEnabled) },
                        onSetPlaybackSpeed = viewModel::setPlaybackSpeed,
                        onToggleRepeatMode = viewModel::toggleRepeatMode,
                        onToggleBackgroundPlay = viewModel::toggleBackgroundPlay,
                        onAttachSubtitle = viewModel::attachSubtitleUri,
                        onEnterPiP = { enterPictureInPicture() },
                        onSelectVideo = { video -> viewModel.playVideo(video, expandToWatchPage = true) },
                        onToggleLike = viewModel::toggleLikeVideo
                    )
                } else {
                    Scaffold(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("app_root_scaffold"),
                        topBar = {
                            if (!uiState.isFullscreen && (uiState.currentVideo == null || uiState.isMiniPlayerActive)) {
                                Column(modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)) {
                                    YouTubeTopBar(
                                        isSearchOpen = uiState.isSearchOpen,
                                        searchQuery = uiState.searchQuery,
                                        onSearchQueryChange = viewModel::onSearchQueryChanged,
                                        onToggleSearch = viewModel::toggleSearch,
                                        darkThemeOption = uiState.darkThemeOption,
                                        onToggleDarkTheme = {
                                            val nextOption = when (uiState.darkThemeOption) {
                                                DarkThemeOption.SYSTEM -> DarkThemeOption.DARK
                                                DarkThemeOption.DARK -> DarkThemeOption.LIGHT
                                                DarkThemeOption.LIGHT -> DarkThemeOption.SYSTEM
                                            }
                                            viewModel.setDarkThemeOption(nextOption)
                                        },
                                        onProfileClick = { viewModel.selectTab(BottomTab.YOU) }
                                    )
                                }
                            }
                        },
                        bottomBar = {
                            if (!uiState.isFullscreen && (uiState.currentVideo == null || uiState.isMiniPlayerActive)) {
                                Column(modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)) {
                                    // Docked Mini-Player above Bottom Nav
                                    if (uiState.currentVideo != null && uiState.isMiniPlayerActive) {
                                        YouTubeMiniPlayer(
                                            video = uiState.currentVideo!!,
                                            isPlaying = uiState.isPlaying,
                                            currentPosition = uiState.currentPosition,
                                            duration = uiState.duration,
                                            onExpand = { viewModel.setMiniPlayer(false) },
                                            onPlayPause = viewModel::togglePlayPause,
                                            onClose = viewModel::closePlayer
                                        )
                                    }

                                    // YouTube Bottom Navigation Bar
                                    YouTubeBottomNavBar(
                                        currentTab = uiState.selectedTab,
                                        onTabSelected = viewModel::selectTab
                                    )
                                }
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            // Main Feed Views based on selected Bottom Tab
                            when (uiState.selectedTab) {
                                BottomTab.HOME -> {
                                    HomeScreen(
                                        uiState = uiState,
                                        onVideoClick = { video ->
                                            viewModel.playVideo(video, expandToWatchPage = true)
                                        },
                                        onFilterSelect = viewModel::onFilterChipSelected,
                                        onRequestPermission = {
                                            val req = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                                Manifest.permission.READ_MEDIA_VIDEO
                                            } else {
                                                Manifest.permission.READ_EXTERNAL_STORAGE
                                            }
                                            permissionLauncher.launch(req)
                                        },
                                        onRefresh = viewModel::loadVideos
                                    )
                                }
                                BottomTab.SHORTS -> {
                                    ShortsScreen(
                                        uiState = uiState,
                                        onPlayVideo = { video ->
                                            viewModel.playVideo(video, expandToWatchPage = true)
                                        },
                                        onToggleLike = viewModel::toggleLikeVideo
                                    )
                                }
                                BottomTab.LIBRARY -> {
                                    LibraryScreen(
                                        uiState = uiState,
                                        onFolderClick = { folderName ->
                                            viewModel.onFilterChipSelected(folderName)
                                            viewModel.selectTab(BottomTab.HOME)
                                        }
                                    )
                                }
                                BottomTab.YOU -> {
                                    YouProfileScreen(
                                        uiState = uiState,
                                        onPlayVideo = { video ->
                                            viewModel.playVideo(video, expandToWatchPage = true)
                                        },
                                        onToggleDarkThemeOption = viewModel::setDarkThemeOption,
                                        onToggleBackgroundPlay = viewModel::toggleBackgroundPlay,
                                        onToggleAutoPlay = viewModel::toggleAutoPlay
                                    )
                                }
                            }

                            // Full YouTube Watch Page when expanded
                            AnimatedVisibility(
                                visible = uiState.currentVideo != null && !uiState.isMiniPlayerActive,
                                enter = slideInVertically(initialOffsetY = { it }),
                                exit = slideOutVertically(targetOffsetY = { it })
                            ) {
                                YouTubeWatchPage(
                                    uiState = uiState,
                                    exoPlayer = viewModel.exoPlayer,
                                    onPlayPause = viewModel::togglePlayPause,
                                    onSeekTo = viewModel::seekTo,
                                    onDoubleTapSeek = viewModel::doubleTapSeek,
                                    onHorizontalDragSeek = viewModel::onHorizontalSeekDrag,
                                    onVolumeAdjust = viewModel::adjustVolumeByDelta,
                                    onBrightnessAdjust = viewModel::adjustBrightnessByDelta,
                                    onNext = viewModel::playNextVideo,
                                    onPrevious = viewModel::playPreviousVideo,
                                    onCollapse = { viewModel.setMiniPlayer(true) },
                                    onToggleFullscreen = viewModel::toggleFullscreen,
                                    onToggleAutoPlay = viewModel::toggleAutoPlay,
                                    onToggleSubtitles = { viewModel.toggleSubtitles(!uiState.isSubtitlesEnabled) },
                                    onSetPlaybackSpeed = viewModel::setPlaybackSpeed,
                                    onToggleRepeatMode = viewModel::toggleRepeatMode,
                                    onToggleBackgroundPlay = viewModel::toggleBackgroundPlay,
                                    onAttachSubtitle = viewModel::attachSubtitleUri,
                                    onEnterPiP = { enterPictureInPicture() },
                                    onSelectVideo = { video -> viewModel.playVideo(video, expandToWatchPage = true) },
                                    onToggleLike = viewModel::toggleLikeVideo
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun enterPictureInPicture() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val aspectRatio = Rational(16, 9)
            val params = PictureInPictureParams.Builder()
                .setAspectRatio(aspectRatio)
                .build()
            enterPictureInPictureMode(params)
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        isPipModeActive = isInPictureInPictureMode
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        // Automatically enter PiP when home button is pressed if video is playing
        val viewModel: PlayerViewModel? = try {
            androidx.lifecycle.ViewModelProvider(this)[PlayerViewModel::class.java]
        } catch (e: Exception) {
            null
        }
        if (viewModel?.exoPlayer?.isPlaying == true && viewModel.uiState.value.currentVideo != null) {
            enterPictureInPicture()
        }
    }

    override fun onStop() {
        super.onStop()
        // If background play is enabled, let audio continue playing in background
        val viewModel: PlayerViewModel? = try {
            androidx.lifecycle.ViewModelProvider(this)[PlayerViewModel::class.java]
        } catch (e: Exception) {
            null
        }
        val isBgEnabled = viewModel?.uiState?.value?.isBackgroundPlayEnabled == true
        if (!isBgEnabled && !isPipModeActive) {
            viewModel?.exoPlayer?.pause()
        }
    }
}
