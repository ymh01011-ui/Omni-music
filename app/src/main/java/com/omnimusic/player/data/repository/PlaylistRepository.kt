package com.omnimusic.player.data.repository

import com.omnimusic.player.data.local.db.FavoriteEntity
import com.omnimusic.player.data.local.db.PlayHistoryEntity
import com.omnimusic.player.data.local.db.PlaylistDao
import com.omnimusic.player.data.local.db.PlaylistEntity
import com.omnimusic.player.data.local.db.toModel
import com.omnimusic.player.data.model.Playlist
import com.omnimusic.player.data.model.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Backs the Playlists screen (spec section 3): user-created playlists plus
 * the system Favorites entry, each rendered with a mosaic cover built from
 * up to 4 of their tracks' album art. History is exposed separately since
 * the reference design treats it as a toggle/filter rather than a playlist
 * card in the grid.
 */
@Singleton
class PlaylistRepository @Inject constructor(
    private val playlistDao: PlaylistDao,
) {

    /**
     * All user playlists plus a synthetic "Favorites" entry first (matching
     * the reference design's "Most Favorite" card), each with up to 4
     * cover art URIs for the mosaic.
     */
    fun observePlaylists(): Flow<List<Playlist>> = combine(
        playlistDao.observeFavoritesCount(),
        playlistDao.observePlaylistsWithCovers(),
    ) { favoritesCount, userPlaylists ->
        val favoritesEntry = Playlist(
            id = FAVORITES_PLAYLIST_ID,
            name = "Most Favorite",
            songCount = favoritesCount,
            coverArtUris = emptyList(), // hydrated lazily by the UI via getCoverArtUris()
            isSystemFavorites = true,
        )
        val userEntries = userPlaylists.map { stats ->
            Playlist(
                id = stats.id,
                name = stats.name,
                songCount = stats.songCount,
                coverArtUris = listOfNotNull(stats.coverArtUri),
            )
        }
        listOf(favoritesEntry) + userEntries
    }

    suspend fun getCoverArtUris(playlistId: Long): List<String> =
        if (playlistId == FAVORITES_PLAYLIST_ID) {
            playlistDao.getFavoritesCoverArtUris()
        } else {
            playlistDao.getCoverArtUris(playlistId)
        }

    fun observeTracksInPlaylist(playlistId: Long): Flow<List<Track>> =
        if (playlistId == FAVORITES_PLAYLIST_ID) {
            playlistDao.observeFavoriteTracks().map { entities -> entities.map { it.toModel() } }
        } else {
            playlistDao.observeTracksInPlaylist(playlistId).map { entities -> entities.map { it.toModel() } }
        }

    fun observeHistoryTracks(): Flow<List<Track>> =
        playlistDao.observeHistoryTracks().map { entities -> entities.map { it.toModel() } }

    suspend fun createPlaylist(name: String): Long =
        playlistDao.insertPlaylist(PlaylistEntity(name = name, createdAt = System.currentTimeMillis()))

    suspend fun renamePlaylist(playlistId: Long, newName: String) =
        playlistDao.renamePlaylist(playlistId, newName)

    suspend fun deletePlaylist(playlistId: Long) =
        playlistDao.deletePlaylistById(playlistId)

    suspend fun addTrackToPlaylist(playlistId: Long, trackId: Long) =
        playlistDao.appendTrackToPlaylist(playlistId, trackId)

    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long) =
        playlistDao.removeTrackFromPlaylist(playlistId, trackId)

    fun observeIsFavorite(trackId: Long): Flow<Boolean> = playlistDao.observeIsFavorite(trackId)

    suspend fun setFavorite(trackId: Long, isFavorite: Boolean) {
        if (isFavorite) {
            playlistDao.addFavorite(FavoriteEntity(trackId = trackId, addedAt = System.currentTimeMillis()))
        } else {
            playlistDao.removeFavorite(trackId)
        }
    }

    suspend fun recordPlay(trackId: Long) =
        playlistDao.recordPlay(PlayHistoryEntity(trackId = trackId, playedAt = System.currentTimeMillis()))

    companion object {
        /**
         * Sentinel ID for the synthetic Favorites entry, which isn't a real
         * row in the `playlists` table - Favorites lives in its own
         * dedicated table ([FavoriteEntity]) per the spec's "system playlist"
         * treatment. Real user playlist IDs are Room-generated autoincrement
         * values starting at 1, so a negative sentinel can't collide.
         */
        const val FAVORITES_PLAYLIST_ID = -1L
    }
}
