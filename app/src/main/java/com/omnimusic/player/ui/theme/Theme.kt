package com.omnimusic.player.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

/**
 * Omni Music is a dark-only, Spotify-style (black/green) themed app per the
 * design spec. We intentionally always use the dark scheme regardless of
 * system theme, matching the reference screenshots exactly.
 */
private val OmniDarkColorScheme = darkColorScheme(
    primary = OmniGreen,
    onPrimary = OmniBlack,
    secondary = OmniGreen,
    background = OmniBlack,
    onBackground = OmniTextPrimary,
    surface = OmniSurface,
    onSurface = OmniTextPrimary,
    surfaceVariant = OmniSurfaceElevated,
    onSurfaceVariant = OmniTextSecondary,
    outline = OmniTextDisabled,
)

@Composable
fun OmniMusicTheme(
    // Reserved for future light/dark toggle; currently always dark per spec.
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = OmniDarkColorScheme,
        typography = OmniTypography,
        content = content
    )
}
