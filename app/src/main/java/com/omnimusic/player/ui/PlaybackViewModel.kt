package com.omnimusic.player.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omnimusic.player.data.model.Track
import com.omnimusic.player.playback.PlaybackRepository
import com.omnimusic.player.playback.PlaybackState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Thin wrapper around [PlaybackRepository] for the app-level Mini Player and
 * any screen that needs to start playback. Kept separate from per-screen
 * ViewModels (SongsViewModel etc.) since playback state is global, not
 * scoped to a single screen.
 */
@HiltViewModel
class PlaybackViewModel @Inject constructor(
    private val repository: PlaybackRepository,
) : ViewModel() {

    val playbackState: StateFlow<PlaybackState> = repository.playbackState

    init {
        repository.connect()
    }

    fun playQueue(queue: List<Track>, startIndex: Int) {
        viewModelScope.launch { repository.playQueue(queue, startIndex) }
    }

    fun togglePlayPause() {
        repository.togglePlayPause()
    }

    fun skipToNext() {
        repository.skipToNext()
    }

    fun skipToPrevious() {
        repository.skipToPrevious()
    }

    fun seekTo(positionMs: Long) {
        repository.seekTo(positionMs)
    }
}
