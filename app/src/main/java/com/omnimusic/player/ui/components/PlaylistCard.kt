package com.omnimusic.player.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.omnimusic.player.data.model.Playlist
import com.omnimusic.player.ui.theme.AccentFavoritesRed

/**
 * Grid cell for the Playlists screen: a mosaic cover built from up to 4 of
 * the playlist's tracks' album art, a circular Play overlay button
 * (bottom-left, matching the reference design), a 3-dot menu (top-right),
 * name, and song count. The Favorites system entry gets a heart icon next
 * to its name instead of the plain title, per the reference design's
 * "Most Favorite" card.
 */
@Composable
fun PlaylistCard(
    playlist: Playlist,
    resolveCoverArtUris: suspend (Long) -> List<String>,
    onClick: () -> Unit,
    onPlayClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var coverArtUris by remember(playlist.id) { mutableStateOf(playlist.coverArtUris) }

    LaunchedEffect(playlist.id) {
        if (coverArtUris.isEmpty()) {
            coverArtUris = resolveCoverArtUris(playlist.id)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(8.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(6.dp)),
        ) {
            MosaicCover(coverArtUris = coverArtUris)

            IconButton(
                onClick = onMoreClick,
                modifier = Modifier.align(Alignment.TopEnd),
            ) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "More options",
                    tint = Color.White,
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable(onClick = onPlayClick),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Play playlist",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        Row(
            modifier = Modifier.padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (playlist.isSystemFavorites) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = null,
                    tint = AccentFavoritesRed,
                    modifier = Modifier
                        .size(16.dp)
                        .padding(end = 4.dp),
                )
            }
            Text(
                text = playlist.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = "${playlist.songCount} Songs",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * Renders up to 4 cover images as a 2x2 mosaic. With fewer than 4 images,
 * the available ones are simply repeated to fill the grid rather than
 * leaving empty quadrants, so a brand-new playlist with 1 track still
 * looks intentional rather than broken.
 */
@Composable
private fun MosaicCover(coverArtUris: List<String>) {
    if (coverArtUris.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            MosaicTile(
                uri = coverArtUris.getOrElse(0) { coverArtUris[0] },
                modifier = Modifier.weight(1f).fillMaxSize(),
            )
            MosaicTile(
                uri = coverArtUris.getOrElse(1) { coverArtUris[0] },
                modifier = Modifier.weight(1f).fillMaxSize(),
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            MosaicTile(
                uri = coverArtUris.getOrElse(2) { coverArtUris[0] },
                modifier = Modifier.weight(1f).fillMaxSize(),
            )
            MosaicTile(
                uri = coverArtUris.getOrElse(3) { coverArtUris[0] },
                modifier = Modifier.weight(1f).fillMaxSize(),
            )
        }
    }
}

@Composable
private fun MosaicTile(uri: String, modifier: Modifier) {
    AsyncImage(
        model = uri,
        contentDescription = null,
        modifier = modifier,
    )
}
