package com.omnimusic.player.data.repository

import com.omnimusic.player.data.local.db.TrackDao
import com.omnimusic.player.data.local.db.toModel
import com.omnimusic.player.data.mediastore.MediaStoreScanner
import com.omnimusic.player.data.model.Album
import com.omnimusic.player.data.model.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Derives [Album] groupings from the cached track list. MediaStore doesn't
 * give us a clean "songs per album" count directly usable for the Albums
 * grid, so we group the already-cached tracks by [Track.albumId] here.
 *
 * A track without an albumId (rare, but possible for malformed files) is
 * excluded from album grouping entirely rather than creating a fake
 * "Unknown Album" bucket, since spec section 3's Albums screen is meant to
 * mirror real album art grids.
 */
@Singleton
class AlbumRepository @Inject constructor(
    private val scanner: MediaStoreScanner,
    private val trackDao: TrackDao,
) {

    fun observeAlbums(): Flow<List<Album>> =
        trackDao.observeAllTracks()
            .map { entities -> entities.map { it.toModel() } }
            .map { tracks -> groupIntoAlbums(tracks) }

    private fun groupIntoAlbums(tracks: List<Track>): List<Album> {
        return tracks
            .filter { it.albumId != null && !it.album.isNullOrBlank() }
            .groupBy { it.albumId }
            .map { (albumId, tracksInAlbum) ->
                val first = tracksInAlbum.first()
                Album(
                    id = albumId!!,
                    name = first.album ?: "Unknown Album",
                    albumArtist = resolveAlbumArtist(tracksInAlbum),
                    year = tracksInAlbum.mapNotNull { it.year }.minOrNull(),
                    songCount = tracksInAlbum.size,
                    albumArtUri = first.albumArtUri,
                )
            }
            .sortedBy { it.name.lowercase() }
    }

    /**
     * Prefers the explicit ALBUM_ARTIST tag when present (handles the
     * "Various Artists" / compilation case correctly per spec section 4).
     * Falls back to the most common track artist if ALBUM_ARTIST is missing.
     */
    private fun resolveAlbumArtist(tracksInAlbum: List<Track>): String? {
        val explicit = tracksInAlbum.firstOrNull { !it.albumArtist.isNullOrBlank() }?.albumArtist
        if (explicit != null) return explicit

        return tracksInAlbum
            .flatMap { it.artists }
            .groupingBy { it }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key
    }
}
