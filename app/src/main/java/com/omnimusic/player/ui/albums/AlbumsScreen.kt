package com.omnimusic.player.ui.albums

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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
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
import com.omnimusic.player.data.model.Album
import com.omnimusic.player.ui.rememberOmniTopPadding
import com.omnimusic.player.ui.components.AlbumCard

@Composable
fun AlbumsScreen(
    modifier: Modifier = Modifier,
    viewModel: AlbumsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val topPadding = rememberOmniTopPadding()

    Box(modifier = modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            uiState.albums.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "No albums found",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = topPadding)
            ) {
                AlbumsHeader(
                    sortOption = uiState.sortOption,
                    sortAscending = uiState.sortAscending,
                    includeSingles = uiState.includeSingles,
                    onSortOptionClick = viewModel::setSortOption,
                    onIncludeSinglesChange = viewModel::setIncludeSingles,
                )
                AlbumsGrid(albums = uiState.albums)
            }
        }
    }
}

@Composable
private fun AlbumsGrid(albums: List<Album>) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(albums, key = { it.id }) { album ->
            AlbumCard(
                album = album,
                onClick = { /* TODO: navigate to album detail once that destination exists */ },
                onPlayClick = { /* TODO: start playback once the playback engine exists */ },
            )
        }
    }
}

@Composable
private fun AlbumsHeader(
    sortOption: AlbumSortOption,
    sortAscending: Boolean,
    includeSingles: Boolean,
    onSortOptionClick: (AlbumSortOption) -> Unit,
    onIncludeSinglesChange: (Boolean) -> Unit,
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
                AlbumSortOption.entries.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.label()) },
                        onClick = {
                            onSortOptionClick(option)
                            sortMenuExpanded = false
                        },
                    )
                }
                HorizontalDivider()
                Row(
                    modifier = Modifier
                        .clickable { onIncludeSinglesChange(!includeSingles) }
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = includeSingles,
                        onCheckedChange = onIncludeSinglesChange,
                    )
                    Text(
                        text = "Include Singles",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

private fun AlbumSortOption.label(): String = when (this) {
    AlbumSortOption.NAME -> "Name"
    AlbumSortOption.ARTIST -> "Artist"
    AlbumSortOption.YEAR -> "Year"
    AlbumSortOption.SONG_COUNT -> "Song count"
}
