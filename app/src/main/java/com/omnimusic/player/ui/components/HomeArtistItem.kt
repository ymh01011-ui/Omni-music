package com.omnimusic.player.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.omnimusic.player.data.model.Artist
import com.omnimusic.player.data.repository.ArtistImageResult
import com.omnimusic.player.ui.theme.OmniGreen

/**
 * Circular artist item for the Home "Recent Artists" horizontal row.
 * Resolves the artist's image via the same Deezer -> iTunes -> Last.fm
 * pipeline as [ArtistCard]; results are cached by the repository so this is
 * cheap even though Home and the Artists tab both render the same artist.
 */
@Composable
fun HomeArtistItem(
    artist: Artist,
    resolveImageUrl: suspend (String) -> ArtistImageResult,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var resolvedImageUrl by remember(artist.name) { mutableStateOf<String?>(null) }

    LaunchedEffect(artist.name) {
        resolvedImageUrl = resolveImageUrl(artist.name).imageUrl
    }

    Column(
        modifier = modifier
            .width(96.dp)
            .clickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape),
        ) {
            if (resolvedImageUrl != null) {
                AsyncImage(
                    model = resolvedImageUrl,
                    contentDescription = artist.name,
                    modifier = Modifier.size(96.dp),
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = artist.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                        style = MaterialTheme.typography.headlineMedium,
                        color = OmniGreen,
                    )
                }
            }
        }
        Text(
            text = artist.name,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}
