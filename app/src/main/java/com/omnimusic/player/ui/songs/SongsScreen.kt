package com.omnimusic.player.ui.songs

import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.omnimusic.player.ui.rememberOmniTopPadding
import com.omnimusic.player.ui.LocalOmniPlaybackViewModel
import com.omnimusic.player.ui.components.TrackRow
import com.omnimusic.player.util.AudioPermission

@Composable
fun SongsScreen(
    modifier: Modifier = Modifier,
    viewModel: SongsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val playbackViewModel = LocalOmniPlaybackViewModel.current
    val topPadding = rememberOmniTopPadding()

    var hasPermission by rememberSaveable {
        mutableStateOf(
            context.checkSelfPermission(AudioPermission.permissionString) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (granted) viewModel.refreshLibrary()
    }

    LaunchedEffect(hasPermission) {
        if (hasPermission) viewModel.refreshLibrary()
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            !hasPermission -> PermissionRequest(
                onRequestPermission = { permissionLauncher.launch(AudioPermission.permissionString) }
            )
            uiState.isLoading && uiState.tracks.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            uiState.tracks.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "No songs found on this device",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            else -> SongsList(
                uiState = uiState,
                topPadding = topPadding,
                onSortOptionClick = viewModel::setSortOption,
                onTrackClick = { index -> playbackViewModel.playQueue(uiState.tracks, index) },
            )
        }
    }
}

@Composable
private fun PermissionRequest(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Omni Music needs access to your audio files to build your library.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(
            onClick = onRequestPermission,
            modifier = Modifier.padding(top = 16.dp),
        ) {
            Text("Grant access")
        }
    }
}

@Composable
private fun SongsList(
    uiState: SongsUiState,
    topPadding: Dp,
    onSortOptionClick: (SongSortOption) -> Unit,
    onTrackClick: (index: Int) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = topPadding)
    ) {
        SongsHeader(
            sortOption = uiState.sortOption,
            sortAscending = uiState.sortAscending,
            songCount = uiState.tracks.size,
            onSortOptionClick = onSortOptionClick,
        )

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            itemsIndexed(uiState.tracks, key = { _, track -> track.id }) { index, track ->
                TrackRow(
                    track = track,
                    onClick = { onTrackClick(index) },
                    onMoreClick = { /* TODO */ },
                )
            }
        }
    }
}

@Composable
private fun SongsHeader(
    sortOption: SongSortOption,
    sortAscending: Boolean,
    songCount: Int,
    onSortOptionClick: (SongSortOption) -> Unit,
) {
    var sortMenuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box {
            Row(
                modifier = Modifier.clickable { sortMenuExpanded = true },
                verticalAlignment = Alignment.CenterVertically
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
                SongSortOption.entries.forEach { option ->
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

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$songCount Songs",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            IconButton(onClick = { /* TODO */ }) {
                Icon(
                    imageVector = Icons.Filled.Shuffle,
                    contentDescription = "Shuffle",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun SongSortOption.label(): String = when (this) {
    SongSortOption.NAME -> "Name"
    SongSortOption.ARTIST -> "Artist"
    SongSortOption.YEAR -> "Year"
    SongSortOption.DATE_ADDED -> "Date added"
    SongSortOption.DURATION -> "Duration"
}
