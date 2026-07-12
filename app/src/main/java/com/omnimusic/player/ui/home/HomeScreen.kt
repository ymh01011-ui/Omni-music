package com.omnimusic.player.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.omnimusic.player.data.repository.HomeData
import com.omnimusic.player.ui.components.HomeAlbumCard
import com.omnimusic.player.ui.components.HomeArtistItem
import com.omnimusic.player.ui.components.HomeSongCard
import com.omnimusic.player.ui.components.QuickAccessRow
import com.omnimusic.player.ui.components.SearchBar
import com.omnimusic.player.ui.components.SectionHeader

/**
 * Real Home screen, per spec section 3: search bar, the 4 quick-access
 * circles, then Recently added songs / Recently played albums / Recent
 * Artists / Favorites sections. Each section shows its 10 most relevant
 * items (see [com.omnimusic.player.data.repository.HomeRepository]); the
 * "See All" arrows are wired up visually but are no-ops until the
 * corresponding full-list screens exist.
 */
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isLoading) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp),
    ) {
        item {
            SearchBar(onClick = { /* TODO: open search once that screen exists */ })
        }
        item {
            QuickAccessRow(
                onHistoryClick = { /* TODO: navigate to History (Playlists tab toggle) */ },
                onFavoritesClick = { /* TODO: navigate to Favorites (Playlists tab toggle) */ },
                onMostPlayedClick = { /* TODO: navigate to a Most Played list once that screen exists */ },
                onShuffleClick = { /* TODO: shuffle-play the whole library once the playback engine exists */ },
            )
        }

        item {
            RecentlyAddedSongsSection(data = uiState.data)
        }
        item {
            RecentlyPlayedAlbumsSection(data = uiState.data)
        }
        item {
            RecentArtistsSection(data = uiState.data, viewModel = viewModel)
        }
        item {
            FavoritesSection(data = uiState.data)
        }
    }
}

@Composable
private fun RecentlyAddedSongsSection(data: HomeData) {
    if (data.recentlyAddedSongs.isEmpty()) return

    SectionHeader(title = "Recently added songs", onSeeAllClick = { /* TODO: last 100 screen */ })
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(data.recentlyAddedSongs, key = { it.id }) { track ->
            HomeSongCard(
                track = track,
                onClick = { /* TODO: start playback once the playback engine exists */ },
            )
        }
    }
}

@Composable
private fun RecentlyPlayedAlbumsSection(data: HomeData) {
    if (data.recentlyPlayedAlbums.isEmpty()) return

    SectionHeader(title = "Recently played albums", onSeeAllClick = { /* TODO: last 100 screen */ })
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(data.recentlyPlayedAlbums, key = { it.id }) { album ->
            HomeAlbumCard(
                album = album,
                onClick = { /* TODO: navigate to album detail once that destination exists */ },
                onPlayClick = { /* TODO: start playback once the playback engine exists */ },
                onMenuClick = { /* TODO: implement album menu handler */ },
            )
        }
    }
}

@Composable
private fun RecentArtistsSection(data: HomeData, viewModel: HomeViewModel) {
    if (data.recentArtists.isEmpty()) return

    SectionHeader(title = "Recent Artists", onSeeAllClick = { /* TODO: last 100 screen */ })
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(data.recentArtists, key = { it.name }) { artist ->
            HomeArtistItem(
                artist = artist,
                resolveImageUrl = viewModel::getArtistImageUrl,
                onClick = { /* TODO: navigate to artist detail once that destination exists */ },
            )
        }
    }
}

@Composable
private fun FavoritesSection(data: HomeData) {
    if (data.favoriteSongs.isEmpty()) return

    SectionHeader(title = "Favorites", onSeeAllClick = { /* TODO: last 100 screen */ })
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(data.favoriteSongs, key = { it.id }) { track ->
            HomeSongCard(
                track = track,
                onClick = { /* TODO: start playback once the playback engine exists */ },
            )
        }
    }
}
