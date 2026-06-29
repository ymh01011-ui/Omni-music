package com.omnimusic.player.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

/**
 * Aggregated view of a playlist with its track count and a cover art URI
 * (the most recently added track's art is the primary one; up to 4 are
 * fetched separately via [PlaylistDao.getCoverArtUris] for the mosaic).
 */
data class PlaylistWithStats(
    val id: Long,
    val name: String,
    val createdAt: Long,
    val songCount: Int,
    val coverArtUri: String?,
)

@Dao
interface PlaylistDao {

    @Insert
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Query("UPDATE playlists SET name = :newName WHERE id = :playlistId")
    suspend fun renamePlaylist(playlistId: Long, newName: String)

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylistById(playlistId: Long)

    @Query(
        """
        SELECT p.id AS id, p.name AS name, p.createdAt AS createdAt,
               COUNT(ptcr.trackId) AS songCount,
               (SELECT t.albumArtUri FROM playlist_track_cross_ref ptcr2
                JOIN tracks t ON t.id = ptcr2.trackId
                WHERE ptcr2.playlistId = p.id
                ORDER BY ptcr2.position ASC LIMIT 1) AS coverArtUri
        FROM playlists p
        LEFT JOIN playlist_track_cross_ref ptcr ON ptcr.playlistId = p.id
        GROUP BY p.id
        ORDER BY p.createdAt DESC
        """
    )
    fun observePlaylistsWithCovers(): Flow<List<PlaylistWithStats>>

    /** Up to 4 distinct album art URIs for a playlist's tracks, for the mosaic cover. */
    @Query(
        """
        SELECT DISTINCT t.albumArtUri FROM playlist_track_cross_ref ptcr
        JOIN tracks t ON t.id = ptcr.trackId
        WHERE ptcr.playlistId = :playlistId AND t.albumArtUri IS NOT NULL
        ORDER BY ptcr.position ASC LIMIT 4
        """
    )
    suspend fun getCoverArtUris(playlistId: Long): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addTrackToPlaylist(crossRef: PlaylistTrackCrossRef)

    @Query("DELETE FROM playlist_track_cross_ref WHERE playlistId = :playlistId AND trackId = :trackId")
    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long)

    @Query("SELECT COALESCE(MAX(position), -1) + 1 FROM playlist_track_cross_ref WHERE playlistId = :playlistId")
    suspend fun nextPositionFor(playlistId: Long): Int

    @Transaction
    suspend fun appendTrackToPlaylist(playlistId: Long, trackId: Long) {
        val position = nextPositionFor(playlistId)
        addTrackToPlaylist(
            PlaylistTrackCrossRef(
                playlistId = playlistId,
                trackId = trackId,
                position = position,
                addedAt = System.currentTimeMillis(),
            )
        )
    }

    @Query(
        """
        SELECT t.* FROM tracks t
        JOIN playlist_track_cross_ref ptcr ON ptcr.trackId = t.id
        WHERE ptcr.playlistId = :playlistId
        ORDER BY ptcr.position ASC
        """
    )
    fun observeTracksInPlaylist(playlistId: Long): Flow<List<TrackEntity>>

    // --- Favorites ---

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE trackId = :trackId")
    suspend fun removeFavorite(trackId: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE trackId = :trackId)")
    fun observeIsFavorite(trackId: Long): Flow<Boolean>

    @Query(
        """
        SELECT t.* FROM tracks t
        JOIN favorites f ON f.trackId = t.id
        ORDER BY f.addedAt DESC
        """
    )
    fun observeFavoriteTracks(): Flow<List<TrackEntity>>

    @Query(
        """
        SELECT DISTINCT albumArtUri FROM tracks t
        JOIN favorites f ON f.trackId = t.id
        WHERE albumArtUri IS NOT NULL
        ORDER BY f.addedAt DESC LIMIT 4
        """
    )
    suspend fun getFavoritesCoverArtUris(): List<String>

    @Query("SELECT COUNT(*) FROM favorites")
    fun observeFavoritesCount(): Flow<Int>

    // --- History ---

    @Insert
    suspend fun recordPlay(entry: PlayHistoryEntity)

    @Query(
        """
        SELECT t.* FROM tracks t
        JOIN play_history h ON h.trackId = t.id
        GROUP BY t.id
        ORDER BY MAX(h.playedAt) DESC
        """
    )
    fun observeHistoryTracks(): Flow<List<TrackEntity>>

    /** Tracks ordered by play count descending, for the Home "Most played" section. */
    @Query(
        """
        SELECT t.* FROM tracks t
        JOIN play_history h ON h.trackId = t.id
        GROUP BY t.id
        ORDER BY COUNT(h.id) DESC
        LIMIT :limit
        """
    )
    fun observeMostPlayedTracks(limit: Int): Flow<List<TrackEntity>>

    /**
     * Distinct album IDs ordered by most recent play, for the Home
     * "Recently played albums" section. We derive this from play_history
     * rather than a dedicated "recently played albums" table, since an
     * album's recency is just the most recent play of any of its tracks.
     */
    @Query(
        """
        SELECT t.albumId FROM tracks t
        JOIN play_history h ON h.trackId = t.id
        WHERE t.albumId IS NOT NULL
        GROUP BY t.albumId
        ORDER BY MAX(h.playedAt) DESC
        LIMIT :limit
        """
    )
    fun observeRecentlyPlayedAlbumIds(limit: Int): Flow<List<Long>>

    /**
     * Tracks ordered by most recent play (one row per track, latest play
     * wins), for the Home "Recent Artists" section - recency of an artist
     * is derived in the repository layer from these tracks' [TrackEntity.artists].
     */
    @Query(
        """
        SELECT t.* FROM tracks t
        JOIN play_history h ON h.trackId = t.id
        GROUP BY t.id
        ORDER BY MAX(h.playedAt) DESC
        """
    )
    fun observeRecentlyPlayedTracksForArtists(): Flow<List<TrackEntity>>
}
