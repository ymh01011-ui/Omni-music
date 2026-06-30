package com.omnimusic.player.playback

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.omnimusic.player.data.model.Track
import com.omnimusic.player.data.repository.PlaylistRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/** What the UI needs to render the Mini Player / Now Playing screen. */
data class PlaybackState(
    val currentTrack: Track? = null,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val hasNext: Boolean = false,
    val hasPrevious: Boolean = false,
)

/**
 * Single source of truth for playback, used by every screen that needs to
 * start playback or render the Mini Player/Now Playing state. Wraps a
 * [MediaController] connected to [PlaybackService] - per spec section 7,
 * queue management lives here, built fresh from whatever list the user
 * tapped Play within (Songs/Album/Playlist), per the clarified scope.
 *
 * Connects via MediaController.Builder.buildAsync() + the future's own
 * addListener (the standard Media3 pattern) rather than
 * kotlinx-coroutines-guava's await(), to avoid adding an extra Gradle
 * dependency for a single call.
 *
 * Every track that starts playing is recorded into play_history via
 * [PlaylistRepository], which is what powers the Home screen's History /
 * Most played / Recently played albums / Recent Artists sections - so any
 * screen that calls [playQueue] automatically contributes to those without
 * needing to remember to record anything itself.
 */
@Singleton
class PlaybackRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val playlistRepository: PlaylistRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var controller: MediaController? = null

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    /** Tracks currently loaded into the player, in queue order. */
    private var currentQueue: List<Track> = emptyList()

    /** Avoids re-recording the same track repeatedly while paused/resumed without changing tracks. */
    private var lastRecordedTrackId: Long? = null

    /** Called once (e.g. from the app's root composable) to connect to the playback service. */
    fun connect() {
        if (controller != null) return

        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val future = MediaController.Builder(context, sessionToken).buildAsync()
        future.addListener(
            {
                controller = future.get()
                controller?.addListener(playerListener)
                syncStateFromController()
            },
            { command -> command.run() }, // run the callback immediately on whatever thread completes the future
        )
    }

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _playbackState.value = _playbackState.value.copy(isPlaying = isPlaying)
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            syncStateFromController()
            recordCurrentTrackPlay()
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            syncStateFromController()
        }
    }

    private fun syncStateFromController() {
        val mediaController = controller ?: return
        val index = mediaController.currentMediaItemIndex
        val track = currentQueue.getOrNull(index)

        _playbackState.value = PlaybackState(
            currentTrack = track,
            isPlaying = mediaController.isPlaying,
            positionMs = mediaController.currentPosition.coerceAtLeast(0),
            durationMs = mediaController.duration.coerceAtLeast(0),
            hasNext = mediaController.hasNextMediaItem(),
            hasPrevious = mediaController.hasPreviousMediaItem(),
        )
    }

    private fun recordCurrentTrackPlay() {
        val track = _playbackState.value.currentTrack ?: return
        if (track.id == lastRecordedTrackId) return
        lastRecordedTrackId = track.id
        scope.launch { playlistRepository.recordPlay(track.id) }
    }

    /**
     * Builds a queue from [queue] (the full list of tracks the user was
     * looking at - e.g. the whole Songs list or an album's tracks) and
     * starts playback at [startIndex]. The queue is exactly the currently
     * displayed list, not a broader "everything in the library" queue.
     */
    fun playQueue(queue: List<Track>, startIndex: Int) {
        val mediaController = controller ?: return
        currentQueue = queue

        val mediaItems = queue.map { it.toMediaItem() }
        mediaController.setMediaItems(mediaItems, startIndex.coerceIn(0, mediaItems.lastIndex), 0L)
        mediaController.prepare()
        mediaController.play()
    }

    fun togglePlayPause() {
        val mediaController = controller ?: return
        if (mediaController.isPlaying) mediaController.pause() else mediaController.play()
    }

    fun skipToNext() {
        controller?.seekToNextMediaItem()
    }

    fun skipToPrevious() {
        controller?.seekToPreviousMediaItem()
    }

    fun seekTo(positionMs: Long) {
        controller?.seekTo(positionMs)
    }

    /** Polls the controller for the current position; call periodically from the UI for a live progress bar. */
    fun refreshPosition() {
        syncStateFromController()
    }

    private fun Track.toMediaItem(): MediaItem {
        val metadata = MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artistDisplay)
            .setAlbumTitle(album)
            .setArtworkUri(albumArtUri?.let { android.net.Uri.parse(it) })
            .build()

        return MediaItem.Builder()
            .setMediaId(id.toString())
            .setUri(contentUri)
            .setMediaMetadata(metadata)
            .build()
    }
}
