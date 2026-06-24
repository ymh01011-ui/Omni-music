package com.omnimusic.player.data.model

/**
 * A single playable audio track read from the device's MediaStore.
 *
 * Per design spec section 4, artists and genres are modeled as lists (not a
 * single String) so a track can belong to multiple artists (e.g. features,
 * collaborations) and multiple genres. [albumArtist] is kept separate from
 * [artists] because they can legitimately differ (compilations, the "Various
 * Artists" pattern, or a track artist who isn't the album's primary artist) -
 * this distinction drives the separate "Go to artist" vs "Go to album artist"
 * context menu actions from the spec.
 */
data class Track(
    val id: Long,
    val title: String,
    val artists: List<String>,
    val albumArtist: String?,
    val album: String?,
    val albumId: Long?,
    val genres: List<String>,
    val composer: String?,
    val year: Int?,
    val trackNumber: Int?,
    val trackTotal: Int?,
    val discNumber: Int?,
    val discTotal: Int?,
    val durationMs: Long,
    val dateAdded: Long,
    val dateModified: Long,
    val filePath: String,
    val folderPath: String,
    val contentUri: String,
    val albumArtUri: String?,
    val mimeType: String?,
    val size: Long,
) {
    /** Display string for list rows: "Artist A, Artist B" per spec section 4. */
    val artistDisplay: String
        get() = if (artists.isEmpty()) "Unknown Artist" else artists.joinToString(", ")
}

/** Lightweight album grouping derived from [Track]s sharing the same [albumId]. */
data class Album(
    val id: Long,
    val name: String,
    val albumArtist: String?,
    val year: Int?,
    val songCount: Int,
    val albumArtUri: String?,
)

/** Lightweight artist grouping. A track contributes to every artist in its [Track.artists]. */
data class Artist(
    val name: String,
    val songCount: Int,
    /** Populated later from iTunes -> Last.fm -> fallback per spec section 6. */
    val imageUrl: String? = null,
)

/** Lightweight genre grouping. A track contributes to every genre in its [Track.genres]. */
data class Genre(
    val name: String,
    val songCount: Int,
)

/** A filesystem folder containing one or more tracks, for the "Go to folder" action. */
data class MusicFolder(
    val path: String,
    val name: String,
    val songCount: Int,
)
