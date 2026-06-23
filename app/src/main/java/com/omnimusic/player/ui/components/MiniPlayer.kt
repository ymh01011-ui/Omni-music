package com.omnimusic.player.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.omnimusic.player.ui.theme.OmniGreen
import com.omnimusic.player.ui.theme.OmniSurfaceElevated

/**
 * Minimal representation of "now playing" state needed to render the mini
 * player. The real playback layer (Media3) will expose this via a Flow from
 * the PlaybackViewModel in a later step.
 */
data class MiniPlayerState(
    val title: String,
    val artist: String,
    val isPlaying: Boolean,
    val progressFraction: Float, // 0f..1f
)

@Composable
fun MiniPlayer(
    state: MiniPlayerState?,
    onClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onPreviousClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state == null) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(OmniSurfaceElevated)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Artwork placeholder (Coil AsyncImage will replace this Box once
            // track artwork URIs are wired from the repository layer).
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 10.dp)
            ) {
                Text(
                    text = state.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = state.artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            IconButton(onClick = onPreviousClick) {
                Icon(
                    imageVector = Icons.Filled.SkipPrevious,
                    contentDescription = "Previous",
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
            IconButton(onClick = onPlayPauseClick) {
                Icon(
                    imageVector = if (state.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (state.isPlaying) "Pause" else "Play",
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
            IconButton(onClick = onNextClick) {
                Icon(
                    imageVector = Icons.Filled.SkipNext,
                    contentDescription = "Next",
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        // Thin progress indicator spanning the full width, per spec.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(state.progressFraction.coerceIn(0f, 1f))
                    .height(2.dp)
                    .background(OmniGreen)
            )
        }
    }
}
