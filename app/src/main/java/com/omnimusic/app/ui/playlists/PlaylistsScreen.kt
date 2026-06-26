package com.omnimusic.app.ui.playlists

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import com.omnimusic.app.data.localdb.entities.PlaylistWithSongs
import com.omnimusic.app.ui.components.MosaicCover

val SpotifyBlack = Color(0xFF000000)
val SpotifyGreen = Color(0xFF1ED760)
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFFB3B3B3)

@Composable
fun PlaylistsScreen(
    playlistsWithSongs: List<PlaylistWithSongs>,
    onPlaylistClick: (Long) -> Unit,
    onCreatePlaylistClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedToggle by remember { mutableStateOf("Playlists") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = SpotifyBlack,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreatePlaylistClick,
                containerColor = SpotifyGreen,
                contentColor = SpotifyBlack,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Create Playlist")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PlaylistOutlineToggle(
                    text = "Favorites",
                    isSelected = selectedToggle == "Favorites",
                    onClick = { selectedToggle = "Favorites" }
                )
                PlaylistOutlineToggle(
                    text = "History",
                    isSelected = selectedToggle == "History",
                    onClick = { selectedToggle = "History" }
                )
            }

            if (playlistsWithSongs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "No playlists created yet.", color = TextSecondary)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(playlistsWithSongs) { item ->
                        val artUris = item.songs.map { it.albumArtUri }
                        
                        PlaylistItemCard(
                            title = item.playlist.name,
                            trackCount = item.songs.size,
                            artUris = artUris,
                            onClick = { onPlaylistClick(item.playlist.playlistId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PlaylistOutlineToggle(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .border(
                width = 1.dp,
                color = if (isSelected) SpotifyGreen else TextSecondary.copy(alpha = 0.4f),
                shape = RoundedCornerShape(20.dp)
            )
            .background(
                color = if (isSelected) SpotifyGreen.copy(alpha = 0.1f) else Color.Transparent,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) SpotifyGreen else TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun PlaylistItemCard(
    title: String,
    trackCount: Int,
    artUris: List<String?>,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        MosaicCover(
            artUris = artUris,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            color = TextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "$trackCount songs",
            color = TextSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Regular
        )
    }
}
