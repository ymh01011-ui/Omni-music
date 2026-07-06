package com.omnimusic.player.ui.playlists

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.omnimusic.player.data.model.Playlist
import com.omnimusic.player.data.model.Track
import com.omnimusic.player.ui.rememberOmniTopPadding
import com.omnimusic.player.ui.components.PlaylistCard
import com.omnimusic.player.ui.components.TrackRow
import com.omnimusic.player.ui.theme.AccentFavoritesRed
import com.omnimusic.player.ui.theme.OmniGreen

@Composable
fun PlaylistsScreen(
    modifier: Modifier = Modifier,
    viewModel: PlaylistsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    val topPadding = rememberOmniTopPadding()

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = OmniGreen,
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Create playlist")
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(top = topPadding)
        ) {
            when {
                uiState.isLoading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
                else -> Column(modifier = Modifier.fillMaxSize()) {
                    FavoritesHistoryToggle(
                        viewMode = uiState.viewMode,
                        onToggle = viewModel::setViewMode,
                    )

                    when (uiState.viewMode) {
                        PlaylistsViewMode.GRID -> PlaylistsGrid(
                            playlists = uiState.playlists,
                            resolveCoverArtUris = viewModel::getCoverArtUris,
                        )
                        PlaylistsViewMode.FAVORITES -> TrackListBody(
                            tracks = uiState.favoritesTracks,
                            emptyMessage = "No favorite songs yet",
                        )
                        PlaylistsViewMode.HISTORY -> TrackListBody(
                            tracks = uiState.historyTracks,
                            emptyMessage = "No listening history yet",
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreatePlaylistDialog(
            onConfirm = { name ->
                viewModel.createPlaylist(name)
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false },
        )
    }
}

@Composable
private fun FavoritesHistoryToggle(
    viewMode: PlaylistsViewMode,
    onToggle: (PlaylistsViewMode) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ToggleChip(
            label = "Favorites",
            icon = Icons.Filled.Favorite,
            selected = viewMode == PlaylistsViewMode.FAVORITES,
            color = AccentFavoritesRed,
            onClick = { onToggle(PlaylistsViewMode.FAVORITES) },
            modifier = Modifier.weight(1f),
        )
        ToggleChip(
            label = "History",
            icon = Icons.Filled.History,
            selected = viewMode == PlaylistsViewMode.HISTORY,
            color = OmniGreen,
            onClick = { onToggle(PlaylistsViewMode.HISTORY) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ToggleChip(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = if (selected) color else MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) color else MaterialTheme.colorScheme.outline,
        ),
    ) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
        Text(label)
    }
}

@Composable
private fun PlaylistsGrid(
    playlists: List<Playlist>,
    resolveCoverArtUris: suspend (Long) -> List<String>,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
    ) {
        gridItems(playlists, key = { it.id }) { playlist ->
            PlaylistCard(
                playlist = playlist,
                resolveCoverArtUris = resolveCoverArtUris,
                onClick = { /* TODO: navigate to playlist detail once that destination exists */ },
                onPlayClick = { /* TODO: start playback once the playback engine exists */ },
                onMoreClick = { /* TODO: rename/delete context menu */ },
            )
        }
    }
}

@Composable
private fun TrackListBody(tracks: List<Track>, emptyMessage: String) {
    if (tracks.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = emptyMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(tracks, key = { it.id }) { track ->
            TrackRow(
                track = track,
                onClick = { /* TODO: start playback once the playback engine exists */ },
                onMoreClick = { /* TODO: open the full 3-dot context menu */ },
            )
        }
    }
}

@Composable
private fun CreatePlaylistDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New playlist") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text("Playlist name") },
                singleLine = true,
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(name) }, enabled = name.isNotBlank()) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
