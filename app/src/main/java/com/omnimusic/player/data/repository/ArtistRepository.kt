package com.omnimusic.player.data.repository

import com.omnimusic.player.data.local.db.TrackDao
import com.omnimusic.player.data.local.db.toModel
import com.omnimusic.player.data.model.Artist
import com.omnimusic.player.data.model.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Derives [Artist] groupings from the cached track list.
 *
 * Per spec section 4, an artist's page must include every track they
 * contribute to, even as a secondary/featured artist - not just tracks
 * where they're the primary artist. We achieve that here by flat-mapping
 * over every track's full [Track.artists] list when grouping, so a track
 * with artists = ["Sean Paul", "Keyshia Cole"] counts toward both artists'
 * song counts and will appear on both artist detail pages.
 */
@Singleton
class ArtistRepository @Inject constructor(
    private val trackDao: TrackDao,
) {

    fun observeArtists(): Flow<List<Artist>> =
        trackDao.observeAllTracks()
            .map { entities -> entities.map { it.toModel() } }
            .map { tracks -> groupIntoArtists(tracks) }

    /** All tracks contributed to by [artistName], including as a secondary artist. */
    fun tracksForArtist(tracks: List<Track>, artistName: String): List<Track> =
        tracks.filter { it.artists.contains(artistName) }

    private fun groupIntoArtists(tracks: List<Track>): List<Artist> {
        return tracks
            .flatMap { track -> track.artists.map { artistName -> artistName to track } }
            .groupBy({ it.first }, { it.second })
            .map { (name, tracksForArtist) ->
                Artist(
                    name = name,
                    songCount = tracksForArtist.size,
                    // imageUrl populated later via iTunes -> Last.fm -> fallback (spec section 6).
                    imageUrl = null,
                )
            }
            .sortedBy { it.name.lowercase() }
    }
}
