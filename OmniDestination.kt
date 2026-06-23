package com.omnimusic.player.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * The 5 fixed bottom navigation destinations, matching the Pulsar-style
 * structure from the design spec: Home / Albums / Songs / Playlists / Artists.
 */
sealed class OmniDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    data object Home : OmniDestination("home", "Home", Icons.Filled.Home)
    data object Albums : OmniDestination("albums", "Albums", Icons.Filled.Album)
    data object Songs : OmniDestination("songs", "Songs", Icons.Filled.MusicNote)
    data object Playlists : OmniDestination("playlists", "Playlists", Icons.Filled.LibraryMusic)
    data object Artists : OmniDestination("artists", "Artists", Icons.Filled.Person)

    companion object {
        val bottomNavItems = listOf(Home, Albums, Songs, Playlists, Artists)
    }
}

/** Routes reached by navigating away from the bottom-nav tabs (not shown in the bar itself). */
object OmniRoutes {
    const val NOW_PLAYING = "now_playing"
    const val TAG_EDITOR = "tag_editor/{trackId}"
    const val EDIT_LYRICS = "edit_lyrics/{trackId}"
    const val ALBUM_DETAIL = "album/{albumId}"
    const val ARTIST_DETAIL = "artist/{artistId}"
    const val PLAYLIST_DETAIL = "playlist/{playlistId}"
    const val SETTINGS = "settings"
}
