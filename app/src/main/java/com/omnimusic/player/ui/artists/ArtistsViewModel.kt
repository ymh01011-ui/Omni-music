package com.omnimusic.player.ui.artists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omnimusic.player.data.model.Artist
import com.omnimusic.player.data.repository.ArtistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

enum class ArtistSortOption { NAME, SONG_COUNT }

data class ArtistsUiState(
    val artists: List<Artist> = emptyList(),
    val isLoading: Boolean = true,
    val sortOption: ArtistSortOption = ArtistSortOption.NAME,
    val sortAscending: Boolean = true,
)

@HiltViewModel
class ArtistsViewModel @Inject constructor(
    repository: ArtistRepository,
) : ViewModel() {

    private val sortOption = MutableStateFlow(ArtistSortOption.NAME)
    private val sortAscending = MutableStateFlow(true)

    val uiState: StateFlow<ArtistsUiState> = combine(
        repository.observeArtists(),
        sortOption,
        sortAscending,
    ) { artists, sort, ascending ->
        ArtistsUiState(
            artists = sortArtists(artists, sort, ascending),
            isLoading = false,
            sortOption = sort,
            sortAscending = ascending,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ArtistsUiState(),
    )

    fun setSortOption(option: ArtistSortOption) {
        if (sortOption.value == option) {
            sortAscending.value = !sortAscending.value
        } else {
            sortOption.value = option
            sortAscending.value = true
        }
    }

    private fun sortArtists(
        artists: List<Artist>,
        sort: ArtistSortOption,
        ascending: Boolean,
    ): List<Artist> {
        val sorted = when (sort) {
            ArtistSortOption.NAME -> artists.sortedBy { it.name.lowercase() }
            ArtistSortOption.SONG_COUNT -> artists.sortedBy { it.songCount }
        }
        return if (ascending) sorted else sorted.reversed()
    }
}
