package com.omnimusic.player.ui.albums

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omnimusic.player.data.model.Album
import com.omnimusic.player.data.repository.AlbumRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

enum class AlbumSortOption { NAME, ARTIST, YEAR, SONG_COUNT }

data class AlbumsUiState(
    val albums: List<Album> = emptyList(),
    val isLoading: Boolean = true,
    val sortOption: AlbumSortOption = AlbumSortOption.NAME,
    val sortAscending: Boolean = true,
    val includeSingles: Boolean = false,
)

/**
 * A "single" is treated as an album with very few tracks (1-2), matching the
 * common convention used by reference players (spec section 3's "Include
 * Singles" checkbox). This is a heuristic since MediaStore has no explicit
 * "is single" flag.
 */
private const val SINGLE_TRACK_THRESHOLD = 2

@HiltViewModel
class AlbumsViewModel @Inject constructor(
    repository: AlbumRepository,
) : ViewModel() {

    private val sortOption = MutableStateFlow(AlbumSortOption.NAME)
    private val sortAscending = MutableStateFlow(true)
    private val includeSingles = MutableStateFlow(false)

    val uiState: StateFlow<AlbumsUiState> = combine(
        repository.observeAlbums(),
        sortOption,
        sortAscending,
        includeSingles,
    ) { albums, sort, ascending, singles ->
        val filtered = if (singles) albums else albums.filter { it.songCount > SINGLE_TRACK_THRESHOLD }
        AlbumsUiState(
            albums = sortAlbums(filtered, sort, ascending),
            isLoading = false,
            sortOption = sort,
            sortAscending = ascending,
            includeSingles = singles,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AlbumsUiState(),
    )

    fun setSortOption(option: AlbumSortOption) {
        if (sortOption.value == option) {
            sortAscending.value = !sortAscending.value
        } else {
            sortOption.value = option
            sortAscending.value = true
        }
    }

    fun setIncludeSingles(include: Boolean) {
        includeSingles.value = include
    }

    private fun sortAlbums(
        albums: List<Album>,
        sort: AlbumSortOption,
        ascending: Boolean,
    ): List<Album> {
        val sorted = when (sort) {
            AlbumSortOption.NAME -> albums.sortedBy { it.name.lowercase() }
            AlbumSortOption.ARTIST -> albums.sortedBy { (it.albumArtist ?: "").lowercase() }
            AlbumSortOption.YEAR -> albums.sortedBy { it.year ?: 0 }
            AlbumSortOption.SONG_COUNT -> albums.sortedBy { it.songCount }
        }
        return if (ascending) sorted else sorted.reversed()
    }
}
