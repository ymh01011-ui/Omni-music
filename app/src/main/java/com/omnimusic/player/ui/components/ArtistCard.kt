package com.omnimusic.player.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.omnimusic.player.data.model.Artist
import com.omnimusic.player.data.repository.ArtistImageResult
import com.omnimusic.player.ui.theme.OmniGreen

/**
 * Grid cell for the Artists screen: circular image (per spec section 3),
 * name below. [resolveImageUrl] is called the first time this card is
 * composed for a given artist to lazily fetch their image via the Deezer ->
 * iTunes -> Last.fm -> fallback pipeline (spec section 6); the caller (the
 * ArtistsViewModel) is responsible for caching so repeat calls are cheap.
 * While unresolved (or if nothing was found anywhere), we show a themed
 * placeholder circle with the artist's first initial.
 *
 * TEMPORARY DEBUG: shows the full source-by-source trace as small red text,
 * so failures are visible directly in a screenshot without needing
 * Logcat/adb access. Remove once image loading is confirmed working.
 */
@Composable
fun ArtistCard(
    artist: Artist,
    resolveImageUrl: suspend (String) -> ArtistImageResult,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var resolvedImageUrl by remember(artist.name) { mutableStateOf<String?>(null) }
    var debugTrace by remember(artist.name) { mutableStateOf("loading...") }

    LaunchedEffect(artist.name) {
        try {
            val result = resolveImageUrl(artist.name)
            resolvedImageUrl = result.imageUrl
            debugTrace = result.trace
        } catch (e: Exception) {
            debugTrace = "CALLER EXC: ${e.javaClass.simpleName}: ${e.message}"
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
                .clip(CircleShape),
        ) {
            if (resolvedImageUrl != null) {
                AsyncImage(
                    model = resolvedImageUrl,
                    contentDescription = artist.name,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
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
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 8.dp),
        )
        Text(
            text = "${artist.songCount} songs",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = debugTrace,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Red,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
