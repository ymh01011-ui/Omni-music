package com.omnimusic.player.ui.artists

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * Placeholder Artists screen. Will be replaced with a 3-column grid of
 * circular artist images (sourced from iTunes -> Last.fm -> fallback) +
 * name, sortable by Song count / Name. Must respect the multi-artist data
 * model: a track's secondary artists also surface here.
 */
@Composable
fun ArtistsScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Artists",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}
