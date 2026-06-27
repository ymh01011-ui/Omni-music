package com.omnimusic.player.data.remote.lastfm

import com.squareup.moshi.Json
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Last.fm artist.getInfo client, used as a fallback per spec section 6 when
 * Deezer/iTunes have no usable artwork for an artist.
 *
 * Known caveat: Last.fm's API has been widely reported to return a generic
 * placeholder "star" image for every artist instead of real photos (their
 * image hosting was deprecated). We still query it and keep the plumbing in
 * place in case that's restored, but [LastFmArtistImageFilter] strips out
 * the known placeholder hash so we don't show a useless gray star to users.
 */
interface LastFmApiService {

    @GET("2.0/")
    suspend fun getArtistInfo(
        @Query("artist") artistName: String,
        @Query("api_key") apiKey: String,
        @Query("method") method: String = "artist.getinfo",
        @Query("format") format: String = "json",
        @Query("autocorrect") autocorrect: Int = 1,
    ): LastFmArtistInfoResponse
}

data class LastFmArtistInfoResponse(
    val artist: LastFmArtist?,
)

data class LastFmArtist(
    val name: String?,
    val image: List<LastFmImage>?,
)

data class LastFmImage(
    @Json(name = "#text") val text: String?,
    val size: String?,
)

/**
 * Last.fm's artist.getInfo currently returns the same placeholder image
 * (a gray star icon) for every artist that lacks a real photo. We detect
 * that known placeholder by its stable filename hash and treat it as "no
 * image" rather than showing it to the user.
 */
object LastFmArtistImageFilter {
    private const val PLACEHOLDER_HASH = "2a96cbd8b46e442fc41c2b86b821562f"

    fun isUsableImage(url: String?): Boolean {
        if (url.isNullOrBlank()) return false
        return !url.contains(PLACEHOLDER_HASH)
    }
}
