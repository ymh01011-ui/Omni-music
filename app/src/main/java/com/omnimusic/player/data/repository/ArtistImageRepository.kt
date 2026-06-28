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
 * TEMPORARY DEBUG carrier: full diagnostic trail of what each source
 * returned/threw, so failures are visible directly in a screenshot without
 * needing Logcat/adb access. [imageUrl] is the resolved result (or null);
 * [trace] is a human-readable line-by-line log of every source attempted.
 * Remove this and go back to a plain String? once image loading works.
 */
data class ArtistImageResult(
    val imageUrl: String?,
    val trace: String,
)

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
    suspend fun getArtistImageUrl(artistName: String): ArtistImageResult = withContext(Dispatchers.IO) {
        val cached = artistImageDao.getCachedImage(artistName)
        val isFresh = cached != null && (System.currentTimeMillis() - cached.resolvedAt) < CACHE_TTL_MS
        if (isFresh) {
            return@withContext ArtistImageResult(cached!!.imageUrl, "CACHE HIT: ${cached.imageUrl}")
        }

        val (resolved, trace) = resolveFromNetwork(artistName)
        artistImageDao.upsert(
            ArtistImageEntity(
                artistName = artistName,
                imageUrl = resolved,
                resolvedAt = System.currentTimeMillis(),
            )
        )
        ArtistImageResult(resolved, trace)
    }

    private suspend fun resolveFromNetwork(artistName: String): Pair<String?, String> {
        val traceLines = mutableListOf<String>()

        val deezerResult = fetchFromDeezer(artistName)
        traceLines += deezerResult.second
        if (deezerResult.first != null) return deezerResult.first to traceLines.joinToString(" | ")

        val itunesResult = fetchFromITunes(artistName)
        traceLines += itunesResult.second
        if (itunesResult.first != null) return itunesResult.first to traceLines.joinToString(" | ")

        val lastFmResult = fetchFromLastFm(artistName)
        traceLines += lastFmResult.second
        if (lastFmResult.first != null) return lastFmResult.first to traceLines.joinToString(" | ")

        return null to traceLines.joinToString(" | ")
    }

    private suspend fun fetchFromDeezer(artistName: String): Pair<String?, String> {
        return try {
            val response = deezerApi.searchArtist(artistName)
            val first = response.data.firstOrNull()
            val url = first?.bestPictureUrl
            url to "Deezer: count=${response.data.size}, name=${first?.name}, xl=${first?.picture_xl}"
        } catch (e: Exception) {
            null to "Deezer EXC: ${e.javaClass.simpleName}: ${e.message}"
        }
    }

    private suspend fun fetchFromITunes(artistName: String): Pair<String?, String> {
        return try {
            val response = iTunesApi.searchAlbumsByArtist(artistName)
            val url = response.results
                .firstOrNull { it.artworkUrl100 != null }
                ?.artworkUrl100
                ?.replace("100x100", "600x600")
            url to "iTunes: count=${response.resultCount}, url=$url"
        } catch (e: Exception) {
            null to "iTunes EXC: ${e.javaClass.simpleName}: ${e.message}"
        }
    }

    private suspend fun fetchFromLastFm(artistName: String): Pair<String?, String> {
        val apiKey = LAST_FM_API_KEY
        if (apiKey.isBlank()) return null to "LastFm: skipped (no key)"

        return try {
            val response = lastFmApi.getArtistInfo(artistName = artistName, apiKey = apiKey)
            val candidate = response.artist?.image
                ?.firstOrNull { it.size == "extralarge" || it.size == "large" }
                ?.text
            val usable = if (LastFmArtistImageFilter.isUsableImage(candidate)) candidate else null
            usable to "LastFm: candidate=$candidate, usable=$usable"
        } catch (e: Exception) {
            null to "LastFm EXC: ${e.javaClass.simpleName}: ${e.message}"
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
