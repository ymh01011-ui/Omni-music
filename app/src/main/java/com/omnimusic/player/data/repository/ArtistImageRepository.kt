package com.omnimusic.player.data.repository

import com.omnimusic.player.data.local.db.ArtistImageDao
import com.omnimusic.player.data.local.db.ArtistImageEntity
import com.omnimusic.player.data.remote.deezer.DeezerApiService
import com.omnimusic.player.data.remote.itunes.ITunesApiService
import com.omnimusic.player.data.remote.lastfm.LastFmApiService
import com.omnimusic.player.data.remote.lastfm.LastFmArtistImageFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Resolves an artist's representative image per spec section 6 (revised
 * source order based on verified research): Deezer first - their public
 * search/artist endpoint returns real artist photos (picture_xl etc.) and
 * needs no API key - then iTunes (using best album artwork as a stand-in,
 * since iTunes' API does not expose real artist photos - see
 * ITunesApiService docs), then Last.fm as a last fallback, then null (UI
 * shows the initial-letter placeholder).
 *
 * Results are cached in Room ([ArtistImageDao]) so repeat app launches don't
 * re-hit the network for artists we've already resolved (or confirmed have
 * no image).
 */
@Singleton
class ArtistImageRepository @Inject constructor(
    private val deezerApi: DeezerApiService,
    private val iTunesApi: ITunesApiService,
    private val lastFmApi: LastFmApiService,
    private val artistImageDao: ArtistImageDao,
) {

    /**
     * Returns a cached image URL immediately if we have one (or have already
     * confirmed there isn't one within [CACHE_TTL_MS]), otherwise resolves
     * it from the network and caches the result either way.
     */
    suspend fun getArtistImageUrl(artistName: String): String? = withContext(Dispatchers.IO) {
        val cached = artistImageDao.getCachedImage(artistName)
        val isFresh = cached != null && (System.currentTimeMillis() - cached.resolvedAt) < CACHE_TTL_MS
        if (isFresh) return@withContext cached!!.imageUrl

        val resolved = resolveFromNetwork(artistName)
        artistImageDao.upsert(
            ArtistImageEntity(
                artistName = artistName,
                imageUrl = resolved,
                resolvedAt = System.currentTimeMillis(),
            )
        )
        resolved
    }

    private suspend fun resolveFromNetwork(artistName: String): String? {
        fetchFromDeezer(artistName)?.let { return it }
        fetchFromITunes(artistName)?.let { return it }
        fetchFromLastFm(artistName)?.let { return it }
        return null
    }

    private suspend fun fetchFromDeezer(artistName: String): String? {
        return try {
            val response = deezerApi.searchArtist(artistName)
            response.data.firstOrNull()?.bestPictureUrl
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun fetchFromITunes(artistName: String): String? {
        return try {
            val response = iTunesApi.searchAlbumsByArtist(artistName)
            response.results
                .firstOrNull { it.artworkUrl100 != null }
                ?.artworkUrl100
                // iTunes thumbnails are 100x100 by default; requesting a
                // larger size by editing the URL gives noticeably better
                // quality for the artist grid without an extra request.
                ?.replace("100x100", "600x600")
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun fetchFromLastFm(artistName: String): String? {
        val apiKey = LAST_FM_API_KEY
        if (apiKey.isBlank()) return null

        return try {
            val response = lastFmApi.getArtistInfo(artistName = artistName, apiKey = apiKey)
            val candidate = response.artist?.image
                ?.firstOrNull { it.size == "extralarge" || it.size == "large" }
                ?.text
            if (LastFmArtistImageFilter.isUsableImage(candidate)) candidate else null
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        private const val CACHE_TTL_MS = 30L * 24 * 60 * 60 * 1000 // 30 days

        /**
         * Last.fm requires a free API key (register at last.fm/api/account/create).
         * Without one, the Last.fm fallback step is silently skipped - Deezer
         * and iTunes remain fully functional either way since neither needs a key.
         */
        private const val LAST_FM_API_KEY = ""
    }
}
