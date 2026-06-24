package com.omnimusic.player.ui.songs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omnimusic.player.data.model.Track
import com.omnimusic.player.data.repository.MusicLibraryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SongSortOption { NAME, ARTIST, YEAR, DATE_ADDED, DURATION }

data class SongsUiState(
    val tracks: List<Track> = emptyList(),
    val isLoading: Boolean = true,
    val sortOption: SongSortOption = SongSortOption.NAME,
    val sortAscending: Boolean = true,
)

@HiltViewModel
class SongsViewModel @Inject constructor(
    private val repository: MusicLibraryRepository,
) : ViewModel() {

    private val sortOption = MutableStateFlow(SongSortOption.NAME)
    private val sortAscending = MutableStateFlow(true)
    private val isLoading = MutableStateFlow(true)

    val uiState: StateFlow<SongsUiState> = combine(
        repository.observeTracks(),
        sortOption,
        sortAscending,
        isLoading,
    ) { tracks, sort, ascending, loading ->
        SongsUiState(
            tracks = sortTracks(tracks, sort, ascending),
            isLoading = loading,
            sortOption = sort,
            sortAscending = ascending,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SongsUiState(),
    )

    /**
     * Triggers a MediaStore re-scan. Should be called once permission is
     * granted (on first launch) and can be called again later for manual
     * refresh / pull-to-refresh.
     */
    fun refreshLibrary() {
        viewModelScope.launch {
            isLoading.value = true
            repository.refreshFromMediaStore()
            isLoading.value = false
        }
    }

    fun setSortOption(option: SongSortOption) {
        if (sortOption.value == option) {
            sortAscending.value = !sortAscending.value
        } else {
            sortOption.value = option
            sortAscending.value = true
        }
    }

    private fun sortTracks(
        tracks: List<Track>,
        sort: SongSortOption,
        ascending: Boolean,
    ): List<Track> {
        val sorted = when (sort) {
            SongSortOption.NAME -> tracks.sortedBy { it.title.lowercase() }
            SongSortOption.ARTIST -> tracks.sortedBy { it.artistDisplay.lowercase() }
            SongSortOption.YEAR -> tracks.sortedBy { it.year ?: 0 }
            SongSortOption.DATE_ADDED -> tracks.sortedBy { it.dateAdded }
            SongSortOption.DURATION -> tracks.sortedBy { it.durationMs }
        }
        return if (ascending) sorted else sorted.reversed()
    }
}
