package com.omnimusic.player.data.remote.deezer

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Deezer's public search API. Used as the PRIMARY artist image source per
 * spec section 6 (revised): unlike iTunes, Deezer's artist object exposes
 * real artist photos directly (picture_xl/picture_big/etc.), and the basic
 * search/lookup endpoints require no API key or authentication - this is
 * the same approach used by several well-known open-source local players
 * for sourcing artist art.
 *
 * Docs reference: https://github.com/antoineraulin/deezer-api/wiki/Artist
 */
interface DeezerApiService {

    @GET("search/artist")
    suspend fun searchArtist(
        @Query("q") artistName: String,
        @Query("limit") limit: Int = 1,
    ): DeezerArtistSearchResponse
}

data class DeezerArtistSearchResponse(
    val data: List<DeezerArtist>,
)

data class DeezerArtist(
    val id: Long,
    val name: String,
    val picture_xl: String?,
    val picture_big: String?,
    val picture_medium: String?,
) {
    /** Best available image, preferring the highest resolution. */
    val bestPictureUrl: String?
        get() = picture_xl ?: picture_big ?: picture_medium
}
