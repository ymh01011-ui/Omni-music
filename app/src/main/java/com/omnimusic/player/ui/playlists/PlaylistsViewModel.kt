package com.omnimusic.player.ui.playlists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omnimusic.player.data.model.Playlist
import com.omnimusic.player.data.model.Track
import com.omnimusic.player.data.repository.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Which top toggle is active on the Playlists screen, per spec section 3's
 * "Favorites | History" outline buttons.
 */
enum class PlaylistsViewMode { GRID, FAVORITES, HISTORY }

data class PlaylistsUiState(
    val playlists: List<Playlist> = emptyList(),
    val favoritesTracks: List<Track> = emptyList(),
    val historyTracks: List<Track> = emptyList(),
    val viewMode: PlaylistsViewMode = PlaylistsViewMode.GRID,
    val isLoading: Boolean = true,
)

@HiltViewModel
class PlaylistsViewModel @Inject constructor(
    private val repository: PlaylistRepository,
) : ViewModel() {

    private val viewMode = MutableStateFlow(PlaylistsViewMode.GRID)

    val uiState: StateFlow<PlaylistsUiState> = combine(
        repository.observePlaylists(),
        repository.observeTracksInPlaylist(PlaylistRepository.FAVORITES_PLAYLIST_ID),
        repository.observeHistoryTracks(),
        viewMode,
    ) { playlists, favoritesTracks, historyTracks, mode ->
        PlaylistsUiState(
            playlists = playlists,
            favoritesTracks = favoritesTracks,
            historyTracks = historyTracks,
            viewMode = mode,
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PlaylistsUiState(),
    )

    /** Resolves the mosaic cover art for a playlist card, lazily and cached by the caller composable. */
    suspend fun getCoverArtUris(playlistId: Long): List<String> = repository.getCoverArtUris(playlistId)

    fun setViewMode(mode: PlaylistsViewMode) {
        viewMode.value = if (viewMode.value == mode) PlaylistsViewMode.GRID else mode
    }

    fun createPlaylist(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch { repository.createPlaylist(name.trim()) }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch { repository.deletePlaylist(playlistId) }
    }

    fun renamePlaylist(playlistId: Long, newName: String) {
        if (newName.isBlank()) return
        viewModelScope.launch { repository.renamePlaylist(playlistId, newName.trim()) }
    }
}
