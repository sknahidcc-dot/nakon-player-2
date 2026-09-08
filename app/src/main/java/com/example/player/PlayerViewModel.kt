package com.example.player

import android.app.Application
import android.content.Context
import android.media.AudioManager
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.data.VideoRepository
import com.example.model.LocalVideo
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class RepeatState {
    OFF, ONE, ALL
}

enum class SeekDirection {
    BACKWARD_10, FORWARD_10
}

enum class DarkThemeOption {
    SYSTEM, DARK, LIGHT
}

enum class BottomTab {
    HOME, SHORTS, LIBRARY, YOU
}

data class PlayerUiState(
    val currentVideo: LocalVideo? = null,
    val playlist: List<LocalVideo> = emptyList(),
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val bufferedPosition: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val repeatState: RepeatState = RepeatState.OFF,
    val isAutoPlayEnabled: Boolean = true,
    val isBackgroundPlayEnabled: Boolean = true,
    val isMiniPlayerActive: Boolean = false,
    val isFullscreen: Boolean = false,
    val subtitleUri: Uri? = null,
    val isSubtitlesEnabled: Boolean = true,
    val activeSubtitleText: String = "",
    // Gesture HUD states
    val volumePercent: Int = 50,
    val isVolumeHudVisible: Boolean = false,
    val brightnessPercent: Int = 50,
    val isBrightnessHudVisible: Boolean = false,
    val seekDifferenceSeconds: Int = 0,
    val isSeekHudVisible: Boolean = false,
    val doubleTapSide: SeekDirection? = null,
    // General app state
    val allVideos: List<LocalVideo> = emptyList(),
    val filteredVideos: List<LocalVideo> = emptyList(),
    val searchQuery: String = "",
    val isSearchOpen: Boolean = false,
    val selectedFilter: String = "All",
    val likedVideoIds: Set<Long> = emptySet(),
    val darkThemeOption: DarkThemeOption = DarkThemeOption.SYSTEM,
    val selectedTab: BottomTab = BottomTab.HOME,
    val isLoadingVideos: Boolean = true,
    val hasStoragePermission: Boolean = false
)

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = VideoRepository(application)
    private val audioManager = application.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    val exoPlayer: ExoPlayer = ExoPlayer.Builder(application)
        .setHandleAudioBecomingNoisy(true)
        .setWakeMode(C.WAKE_MODE_LOCAL)
        .build()

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private var progressJob: Job? = null
    private var hudDismissJob: Job? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    init {
        setupPlayerListener()
        readInitialVolume()
        loadVideos()
    }

    private fun readInitialVolume() {
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val curVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val percent = if (maxVolume > 0) (curVolume * 100) / maxVolume else 50
        _uiState.update { it.copy(volumePercent = percent) }
    }

    private fun setupPlayerListener() {
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _uiState.update { it.copy(isPlaying = isPlaying) }
                if (isPlaying) {
                    startProgressTracker()
                } else {
                    stopProgressTracker()
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        _uiState.update {
                            it.copy(
                                duration = exoPlayer.duration.coerceAtLeast(0L),
                                bufferedPosition = exoPlayer.bufferedPosition.coerceAtLeast(0L)
                            )
                        }
                    }
                    Player.STATE_ENDED -> {
                        handlePlaybackEnded()
                    }
                    else -> Unit
                }
            }
        })
    }

    fun loadVideos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingVideos = true) }
            val videos = repository.getLocalVideos()
            _uiState.update {
                it.copy(
                    allVideos = videos,
                    filteredVideos = filterVideosList(videos, it.searchQuery, it.selectedFilter),
                    isLoadingVideos = false,
                    playlist = videos
                )
            }
        }
    }

    fun setPermissionGranted(granted: Boolean) {
        _uiState.update { it.copy(hasStoragePermission = granted) }
        if (granted) {
            loadVideos()
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update {
            it.copy(
                searchQuery = query,
                filteredVideos = filterVideosList(it.allVideos, query, it.selectedFilter)
            )
        }
    }

    fun toggleSearch(open: Boolean) {
        _uiState.update {
            val nextOpen = open
            val query = if (!nextOpen) "" else it.searchQuery
            it.copy(
                isSearchOpen = nextOpen,
                searchQuery = query,
                filteredVideos = filterVideosList(it.allVideos, query, it.selectedFilter)
            )
        }
    }

    fun onFilterChipSelected(filter: String) {
        _uiState.update {
            it.copy(
                selectedFilter = filter,
                filteredVideos = filterVideosList(it.allVideos, it.searchQuery, filter)
            )
        }
    }

    private fun filterVideosList(videos: List<LocalVideo>, query: String, filter: String): List<LocalVideo> {
        return videos.filter { video ->
            val matchesFilter = when (filter) {
                "All" -> true
                "Shorts" -> video.durationMs in 1..60000 || video.bucketName.equals("Shorts", ignoreCase = true)
                "Downloads" -> video.bucketName.contains("Download", ignoreCase = true)
                "Camera" -> video.bucketName.contains("Camera", ignoreCase = true) || video.bucketName.contains("DCIM", ignoreCase = true)
                "Movies" -> video.bucketName.contains("Movie", ignoreCase = true) || video.bucketName.contains("Video", ignoreCase = true)
                else -> video.bucketName.contains(filter, ignoreCase = true)
            }
            val matchesQuery = query.isBlank() || video.title.contains(query, ignoreCase = true) ||
                    video.bucketName.contains(query, ignoreCase = true)
            matchesFilter && matchesQuery
        }
    }

    fun playVideo(video: LocalVideo, expandToWatchPage: Boolean = true) {
        val mediaItemBuilder = MediaItem.Builder()
            .setUri(video.uri)

        // Subtitle integration if attached
        video.subtitleUri?.let { subUri ->
            val subtitleConfig = MediaItem.SubtitleConfiguration.Builder(subUri)
                .setMimeType(MimeTypes.APPLICATION_SUBRIP)
                .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                .build()
            mediaItemBuilder.setSubtitleConfigurations(listOf(subtitleConfig))
        }

        exoPlayer.setMediaItem(mediaItemBuilder.build())
        exoPlayer.playbackParameters = PlaybackParameters(_uiState.value.playbackSpeed)
        exoPlayer.prepare()
        exoPlayer.play()

        _uiState.update {
            it.copy(
                currentVideo = video,
                isMiniPlayerActive = !expandToWatchPage,
                subtitleUri = video.subtitleUri,
                currentPosition = 0L,
                duration = video.durationMs
            )
        }
    }

    fun togglePlayPause() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            exoPlayer.play()
        }
    }

    fun seekTo(positionMs: Long) {
        val safePos = positionMs.coerceIn(0L, exoPlayer.duration.coerceAtLeast(0L))
        exoPlayer.seekTo(safePos)
        _uiState.update { it.copy(currentPosition = safePos) }
    }

    // Double tap seeking (+10s / -10s)
    fun doubleTapSeek(isForward: Boolean) {
        val current = exoPlayer.currentPosition
        val duration = exoPlayer.duration.coerceAtLeast(0L)
        val offset = if (isForward) 10000L else -10000L
        val target = (current + offset).coerceIn(0L, duration)

        exoPlayer.seekTo(target)
        val side = if (isForward) SeekDirection.FORWARD_10 else SeekDirection.BACKWARD_10

        _uiState.update {
            it.copy(
                currentPosition = target,
                doubleTapSide = side
            )
        }

        mainHandler.removeCallbacksAndMessages(null)
        mainHandler.postDelayed({
            _uiState.update { it.copy(doubleTapSide = null) }
        }, 850)
    }

    // Horizontal swipe gesture seeking
    fun onHorizontalSeekDrag(deltaSeconds: Int) {
        val current = exoPlayer.currentPosition
        val duration = exoPlayer.duration.coerceAtLeast(0L)
        val newTarget = (current + (deltaSeconds * 1000L)).coerceIn(0L, duration)
        exoPlayer.seekTo(newTarget)

        _uiState.update {
            it.copy(
                currentPosition = newTarget,
                seekDifferenceSeconds = deltaSeconds,
                isSeekHudVisible = true
            )
        }
        scheduleHudDismiss()
    }

    // Vertical right swipe -> Volume Control
    fun adjustVolumeByDelta(deltaPercent: Float) {
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val currentPercent = _uiState.value.volumePercent
        val newPercent = (currentPercent + deltaPercent.toInt()).coerceIn(0, 100)
        val newVolumeIndex = (newPercent * maxVolume) / 100
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolumeIndex, 0)

        _uiState.update {
            it.copy(
                volumePercent = newPercent,
                isVolumeHudVisible = true,
                isBrightnessHudVisible = false
            )
        }
        scheduleHudDismiss()
    }

    // Vertical left swipe -> Brightness Control
    fun adjustBrightnessByDelta(deltaPercent: Float) {
        val currentPercent = _uiState.value.brightnessPercent
        val newPercent = (currentPercent + deltaPercent.toInt()).coerceIn(0, 100)
        _uiState.update {
            it.copy(
                brightnessPercent = newPercent,
                isBrightnessHudVisible = true,
                isVolumeHudVisible = false
            )
        }
        scheduleHudDismiss()
    }

    private fun scheduleHudDismiss() {
        hudDismissJob?.cancel()
        hudDismissJob = viewModelScope.launch {
            delay(1200)
            _uiState.update {
                it.copy(
                    isVolumeHudVisible = false,
                    isBrightnessHudVisible = false,
                    isSeekHudVisible = false
                )
            }
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        exoPlayer.playbackParameters = PlaybackParameters(speed)
        _uiState.update { it.copy(playbackSpeed = speed) }
    }

    fun toggleRepeatMode() {
        val nextMode = when (_uiState.value.repeatState) {
            RepeatState.OFF -> RepeatState.ALL
            RepeatState.ALL -> RepeatState.ONE
            RepeatState.ONE -> RepeatState.OFF
        }
        _uiState.update { it.copy(repeatState = nextMode) }
    }

    fun toggleAutoPlay() {
        _uiState.update { it.copy(isAutoPlayEnabled = !it.isAutoPlayEnabled) }
    }

    fun toggleBackgroundPlay() {
        _uiState.update { it.copy(isBackgroundPlayEnabled = !it.isBackgroundPlayEnabled) }
    }

    fun toggleSubtitles(enabled: Boolean) {
        _uiState.update { it.copy(isSubtitlesEnabled = enabled) }
    }

    fun attachSubtitleUri(uri: Uri) {
        _uiState.update { it.copy(subtitleUri = uri, isSubtitlesEnabled = true) }
        val current = _uiState.value.currentVideo ?: return
        val currentPos = exoPlayer.currentPosition
        val updatedVideo = current.copy(subtitleUri = uri)

        val subConfig = MediaItem.SubtitleConfiguration.Builder(uri)
            .setMimeType(MimeTypes.APPLICATION_SUBRIP)
            .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
            .build()

        val mediaItem = MediaItem.Builder()
            .setUri(updatedVideo.uri)
            .setSubtitleConfigurations(listOf(subConfig))
            .build()

        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.seekTo(currentPos)
        exoPlayer.play()
    }

    fun playNextVideo() {
        val current = _uiState.value.currentVideo ?: return
        val playlist = _uiState.value.playlist
        if (playlist.isEmpty()) return
        val currentIndex = playlist.indexOfFirst { it.id == current.id }
        if (currentIndex != -1 && currentIndex + 1 < playlist.size) {
            playVideo(playlist[currentIndex + 1], expandToWatchPage = !_uiState.value.isMiniPlayerActive)
        } else if (_uiState.value.repeatState == RepeatState.ALL && playlist.isNotEmpty()) {
            playVideo(playlist[0], expandToWatchPage = !_uiState.value.isMiniPlayerActive)
        }
    }

    fun playPreviousVideo() {
        val current = _uiState.value.currentVideo ?: return
        val playlist = _uiState.value.playlist
        if (playlist.isEmpty()) return
        val currentIndex = playlist.indexOfFirst { it.id == current.id }
        if (currentIndex > 0) {
            playVideo(playlist[currentIndex - 1], expandToWatchPage = !_uiState.value.isMiniPlayerActive)
        } else {
            seekTo(0L)
        }
    }

    private fun handlePlaybackEnded() {
        when (_uiState.value.repeatState) {
            RepeatState.ONE -> {
                seekTo(0L)
                exoPlayer.play()
            }
            RepeatState.ALL -> {
                playNextVideo()
            }
            RepeatState.OFF -> {
                if (_uiState.value.isAutoPlayEnabled) {
                    playNextVideo()
                }
            }
        }
    }

    fun setMiniPlayer(active: Boolean) {
        _uiState.update { it.copy(isMiniPlayerActive = active) }
    }

    fun closePlayer() {
        exoPlayer.stop()
        _uiState.update {
            it.copy(
                currentVideo = null,
                isMiniPlayerActive = false,
                isFullscreen = false
            )
        }
    }

    fun toggleFullscreen() {
        _uiState.update { it.copy(isFullscreen = !it.isFullscreen) }
    }

    fun setFullscreen(fullscreen: Boolean) {
        _uiState.update { it.copy(isFullscreen = fullscreen) }
    }

    fun toggleLikeVideo(videoId: Long) {
        _uiState.update {
            val currentLikes = it.likedVideoIds.toMutableSet()
            if (currentLikes.contains(videoId)) {
                currentLikes.remove(videoId)
            } else {
                currentLikes.add(videoId)
            }
            it.copy(likedVideoIds = currentLikes)
        }
    }

    fun setDarkThemeOption(option: DarkThemeOption) {
        _uiState.update { it.copy(darkThemeOption = option) }
    }

    fun selectTab(tab: BottomTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (isActive) {
                if (exoPlayer.isPlaying) {
                    _uiState.update {
                        it.copy(
                            currentPosition = exoPlayer.currentPosition.coerceAtLeast(0L),
                            duration = exoPlayer.duration.coerceAtLeast(0L),
                            bufferedPosition = exoPlayer.bufferedPosition.coerceAtLeast(0L)
                        )
                    }
                }
                delay(400)
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
    }

    override fun onCleared() {
        super.onCleared()
        progressJob?.cancel()
        hudDismissJob?.cancel()
        exoPlayer.release()
    }
}
