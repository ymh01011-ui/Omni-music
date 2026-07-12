package com.omnimusic.player.ui.artists

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.omnimusic.player.data.model.Artist
import com.omnimusic.player.data.repository.ArtistImageResult
import com.omnimusic.player.ui.components.ArtistCard

/**
 * Real Artists screen: a 3-column grid of circular artist images (per spec
 * section 3), sortable by Name / Song count. Tapping a card will navigate
 * to the artist detail screen once that destination exists; for now it's a
 * no-op stub.
 */
@Composable
fun ArtistsScreen(
    modifier: Modifier = Modifier,
    viewModel: ArtistsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            uiState.artists.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "No artists found",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            else -> Column(modifier = Modifier.fillMaxSize()) {
                ArtistsHeader(
                    sortOption = uiState.sortOption,
                    sortAscending = uiState.sortAscending,
                    onSortOptionClick = viewModel::setSortOption,
                )
                ArtistsGrid(
                    artists = uiState.artists,
                    resolveImageUrl = viewModel::getArtistImageUrl,
                )
            }
        }
    }
}

@Composable
private fun ArtistsGrid(
    artists: List<Artist>,
    resolveImageUrl: suspend (String) -> ArtistImageResult,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(artists, key = { it.name }) { artist ->
            ArtistCard(
                artist = artist,
                resolveImageUrl = resolveImageUrl,
                onClick = { /* TODO: navigate to artist detail once that destination exists */ },
            )
        }
    }
}

@Composable
private fun ArtistsHeader(
    sortOption: ArtistSortOption,
    sortAscending: Boolean,
    onSortOptionClick: (ArtistSortOption) -> Unit,
) {
    var sortMenuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box {
            Row(
                modifier = Modifier.clickable { sortMenuExpanded = true },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = sortOption.label(),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Icon(
                    imageVector = Icons.Filled.ArrowUpward,
                    contentDescription = if (sortAscending) "Ascending" else "Descending",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .size(16.dp)
                        .graphicsLayer(rotationZ = if (sortAscending) 0f else 180f),
                )
            }

            DropdownMenu(
                expanded = sortMenuExpanded,
                onDismissRequest = { sortMenuExpanded = false },
            ) {
                ArtistSortOption.entries.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.label()) },
                        onClick = {
                            onSortOptionClick(option)
                            sortMenuExpanded = false
                        },
                    )
                }
            }
        }
    }
}

private fun ArtistSortOption.label(): String = when (this) {
    ArtistSortOption.NAME -> "Name"
    ArtistSortOption.SONG_COUNT -> "Song count"
}
