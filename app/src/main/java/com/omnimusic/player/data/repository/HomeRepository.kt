package com.omnimusic.player.data.repository

import com.omnimusic.player.data.local.db.PlaylistDao
import com.omnimusic.player.data.local.db.TrackDao
import com.omnimusic.player.data.local.db.toModel
import com.omnimusic.player.data.model.Album
import com.omnimusic.player.data.model.Artist
import com.omnimusic.player.data.model.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** Aggregated state for the Home screen's sections (spec section 3). */
data class HomeData(
    val recentlyAddedSongs: List<Track> = emptyList(),
    val recentlyPlayedAlbums: List<Album> = emptyList(),
    val recentArtists: List<Artist> = emptyList(),
    val favoriteSongs: List<Track> = emptyList(),
)

private const val HOME_SECTION_PREVIEW_LIMIT = 10

/**
 * Backs the Home screen. Each section shows its 10 most relevant items
 * (per spec: out front each section shows the last 10, and tapping the
 * arrow shows the last 100 - the See All/100-item screens are a later
 * step; this repository already limits to 10 here so that future screen
 * can simply re-query with a higher limit).
 */
@Singleton
class HomeRepository @Inject constructor(
    private val trackDao: TrackDao,
    private val playlistDao: PlaylistDao,
    private val albumRepository: AlbumRepository,
) {

    fun observeHomeData(): Flow<HomeData> = combine(
        trackDao.observeRecentlyAddedTracks(HOME_SECTION_PREVIEW_LIMIT),
        recentlyPlayedAlbums(),
        recentArtists(),
        playlistDao.observeFavoriteTracks(),
    ) { recentSongs, recentAlbums, recentArtists, favoriteTracks ->
        HomeData(
            recentlyAddedSongs = recentSongs.map { it.toModel() },
            recentlyPlayedAlbums = recentAlbums,
            recentArtists = recentArtists,
            favoriteSongs = favoriteTracks.take(HOME_SECTION_PREVIEW_LIMIT).map { it.toModel() },
        )
    }

    /**
     * Joins the recently-played album ID order (derived from play history)
     * against the full album list from [AlbumRepository], preserving the
     * recency order rather than the album repository's default alphabetical
     * sort.
     */
    private fun recentlyPlayedAlbums(): Flow<List<Album>> = combine(
        playlistDao.observeRecentlyPlayedAlbumIds(HOME_SECTION_PREVIEW_LIMIT),
        albumRepository.observeAlbums(),
    ) { recentIds, allAlbums ->
        val albumsById = allAlbums.associateBy { it.id }
        recentIds.mapNotNull { albumsById[it] }
    }

    /**
     * Derives a recency-ordered, de-duplicated artist list from recently
     * played tracks. A track can have multiple artists (spec section 4);
     * we take the first artist per track as "the" artist credited for that
     * play, which keeps this list short and matches what a person expects
     * to see for "who did I just listen to".
     */
    private fun recentArtists(): Flow<List<Artist>> =
        playlistDao.observeRecentlyPlayedTracksForArtists().map { entities ->
            val seen = LinkedHashSet<String>()
            for (entity in entities) {
                val primaryArtist = entity.toModel().artists.firstOrNull() ?: continue
                seen.add(primaryArtist)
                if (seen.size >= HOME_SECTION_PREVIEW_LIMIT) break
            }
            seen.map { name -> Artist(name = name, songCount = 0) }
        }
}
