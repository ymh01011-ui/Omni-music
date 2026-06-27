package com.omnimusic.player.data.remote.itunes

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * iTunes Search API client. Used to resolve an artist image per spec
 * section 6.
 *
 * Important: the iTunes Search API's artist entity (entity=musicArtist)
 * does NOT return an artist photo - only artist ID/genre/name. The only
 * actual images the API returns live on album/song results (artworkUrl*).
 * So to get a representative "artist image", we search for albums by the
 * artist name and use the highest-resolution artwork from their most
 * popular/relevant album as a stand-in - the same approach used by other
 * reference players' iTunes-based artist art pipelines.
 */
interface ITunesApiService {

    @GET("search")
    suspend fun searchAlbumsByArtist(
        @Query("term") artistName: String,
        @Query("entity") entity: String = "album",
        @Query("limit") limit: Int = 5,
    ): ITunesSearchResponse
}

data class ITunesSearchResponse(
    val resultCount: Int,
    val results: List<ITunesResult>,
)

data class ITunesResult(
    val artistName: String?,
    val collectionName: String?,
    val artworkUrl100: String?,
)
