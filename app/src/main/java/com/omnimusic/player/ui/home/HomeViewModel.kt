package com.omnimusic.player.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omnimusic.player.data.repository.ArtistImageRepository
import com.omnimusic.player.data.repository.ArtistImageResult
import com.omnimusic.player.data.repository.HomeData
import com.omnimusic.player.data.repository.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class HomeUiState(
    val data: HomeData = HomeData(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    repository: HomeRepository,
    private val imageRepository: ArtistImageRepository,
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = repository.observeHomeData()
        .map { HomeUiState(data = it, isLoading = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeUiState(),
        )

    /** Resolves an artist's image for [com.omnimusic.player.ui.components.HomeArtistItem]. */
    suspend fun getArtistImageUrl(artistName: String): ArtistImageResult =
        imageRepository.getArtistImageUrl(artistName)
}
