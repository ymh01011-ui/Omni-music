package com.omnimusic.player.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
                // التعديل الجوهري: نأخذ الحشو السفلي فقط لحماية أزرار التنقل والـ MiniPlayer، ونلغي الحشو العلوي تماماً لتتحرر الشاشة وتصعد لأعلى
                modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()),
                enterTransition = { fadeIn(animationSpec = tween(TRANSITION_DURATION_MS)) },
                exitTransition = { fadeOut(animationSpec = tween(TRANSITION_DURATION_MS)) },
                popEnterTransition = { fadeIn(animationSpec = tween(TRANSITION_DURATION_MS)) },
                popExitTransition = { fadeOut(animationSpec = tween(TRANSITION_DURATION_MS)) },
            ) {
                composable(OmniDestination.Home.route) { 
                    // تمرير Modifier.fillMaxSize() لضمان مطابقة الشاشة
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

            NavigationBarItem(
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
                icon = {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = destination.label,
                    )
                },
                label = { Text(destination.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = OmniGreen,
                    selectedTextColor = OmniGreen,
                ),
            )
        }
    }
}
