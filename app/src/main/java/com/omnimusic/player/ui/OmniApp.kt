package com.omnimusic.player.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.omnimusic.player.ui.albums.AlbumsScreen
import com.omnimusic.player.ui.artists.ArtistsScreen
import com.omnimusic.player.ui.components.MiniPlayer
import com.omnimusic.player.ui.components.MiniPlayerState
import com.omnimusic.player.ui.home.HomeScreen
import com.omnimusic.player.ui.navigation.OmniDestination
import com.omnimusic.player.ui.playlists.PlaylistsScreen
import com.omnimusic.player.ui.songs.SongsScreen
import com.omnimusic.player.ui.theme.OmniGreen

/**
 * Animation timing per design spec section 2: short (200-300ms) fade-through
 * transitions for all tab switches and navigation, no slide/overshoot.
 */
private const val TRANSITION_DURATION_MS = 250

/**
 * Metro/RetroMusic-style timing + tints for the bottom navigation bar:
 * selected icon lifts up, label fades/expands in below it, and the pill
 * indicator behind the icon appears at a low accent-color opacity.
 */
private const val NAV_ITEM_ANIM_MS = 250
private const val NAV_INDICATOR_ALPHA = 0.12f
private val NAV_ICON_LIFT = (-8).dp

/**
 * App-wide [PlaybackViewModel] instance, shared by every screen that needs
 * to start or observe playback (Mini Player, Songs, Albums, Home, etc.).
 */
val LocalPlaybackViewModel = compositionLocalOf<PlaybackViewModel> {
    error("LocalPlaybackViewModel not provided - must be set in OmniApp")
}

@Composable
fun OmniApp() {
    val navController = rememberNavController()
    val playbackViewModel: PlaybackViewModel = hiltViewModel()
    val playbackState by playbackViewModel.playbackState.collectAsState()

    CompositionLocalProvider(LocalPlaybackViewModel provides playbackViewModel) {
        Scaffold(
            bottomBar = {
                Column {
                    MiniPlayer(
                        state = playbackState.currentTrack?.let { track ->
                            MiniPlayerState(
                                title = track.title,
                                artist = track.artistDisplay,
                                isPlaying = playbackState.isPlaying,
                                progressFraction = if (playbackState.durationMs > 0) {
                                    playbackState.positionMs.toFloat() / playbackState.durationMs.toFloat()
                                } else 0f,
                            )
                        },
                        onClick = { /* TODO: navigate to Now Playing once that screen exists */ },
                        onPlayPauseClick = playbackViewModel::togglePlayPause,
                        onNextClick = playbackViewModel::skipToNext,
                        onPreviousClick = playbackViewModel::skipToPrevious,
                    )
                    OmniBottomNavBar(navController)
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = OmniDestination.Home.route,
                modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()),
                enterTransition = { fadeIn(animationSpec = tween(TRANSITION_DURATION_MS)) },
                exitTransition = { fadeOut(animationSpec = tween(TRANSITION_DURATION_MS)) },
                popEnterTransition = { fadeIn(animationSpec = tween(TRANSITION_DURATION_MS)) },
                popExitTransition = { fadeOut(animationSpec = tween(TRANSITION_DURATION_MS)) },
            ) {
                composable(OmniDestination.Home.route) {
                    HomeScreen(modifier = Modifier.fillMaxSize())
                }
                composable(OmniDestination.Albums.route) { AlbumsScreen() }
                composable(OmniDestination.Songs.route) { SongsScreen() }
                composable(OmniDestination.Playlists.route) { PlaylistsScreen() }
                composable(OmniDestination.Artists.route) { ArtistsScreen() }
            }
        }
    }
}

@Composable
private fun OmniBottomNavBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar {
        OmniDestination.bottomNavItems.forEach { destination ->
            val selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true

            OmniNavItem(
                selected = selected,
                onClick = {
                    navController.navigate(destination.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = destination.icon,
                label = destination.label,
            )
        }
    }
}

/**
 * Custom nav bar item replicating Metro/RetroMusic's motion: the icon lifts
 * up and the label fades + expands in underneath it when selected. Unselected
 * items show the icon only, with no reserved space for a label.
 *
 * Ripple color is set to OmniGreen to match the theme.
 */
@Composable
private fun RowScope.OmniNavItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
) {
    val iconOffsetY by animateDpAsState(
        targetValue = if (selected) NAV_ICON_LIFT else 0.dp,
        animationSpec = tween(NAV_ITEM_ANIM_MS),
        label = "navIconOffset",
    )
    val indicatorColor by animateColorAsState(
        targetValue = if (selected) OmniGreen.copy(alpha = NAV_INDICATOR_ALPHA) else Color.Transparent,
        animationSpec = tween(NAV_ITEM_ANIM_MS),
        label = "navIndicatorColor",
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) OmniGreen else NavigationBarItemDefaults.colors().unselectedIconColor,
        animationSpec = tween(NAV_ITEM_ANIM_MS),
        label = "navContentColor",
    )

    Column(
        modifier = Modifier
            .weight(1f)
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.Tab,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(
                    bounded = false,
                    radius = 28.dp,
                ),
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .offset(y = iconOffsetY)
                .size(width = 56.dp, height = 28.dp)
                .clip(RoundedCornerShape(percent = 50))
                .background(indicatorColor),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
            )
        }
        AnimatedVisibility(
            visible = selected,
            enter = fadeIn(tween(NAV_ITEM_ANIM_MS)) + expandVertically(tween(NAV_ITEM_ANIM_MS)),
            exit = fadeOut(tween(NAV_ITEM_ANIM_MS)) + shrinkVertically(tween(NAV_ITEM_ANIM_MS)),
        ) {
            Text(
                text = label,
                color = contentColor,
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}
