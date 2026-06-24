package com.omnimusic.player.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.omnimusic.player.data.model.Track

/**
 * Room-persisted cache of [Track]. We cache MediaStore scan results locally
 * so the Songs screen has instant data on subsequent app launches instead of
 * re-querying MediaStore (and re-splitting multi-value tags) every time.
 * The MediaStore scan still runs on launch/content-change to keep this in
 * sync; this table is a read-through cache, not the source of truth.
 */
@Entity(tableName = "tracks")
data class TrackEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val artists: String, // delimited list, see Converters
    val albumArtist: String?,
    val album: String?,
    val albumId: Long?,
    val genres: String, // delimited list, see Converters
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
)

private const val LIST_DELIMITER = "\u001F" // unit separator; won't collide with real tag text

fun Track.toEntity(): TrackEntity = TrackEntity(
    id = id,
    title = title,
    artists = artists.joinToString(LIST_DELIMITER),
    albumArtist = albumArtist,
    album = album,
    albumId = albumId,
    genres = genres.joinToString(LIST_DELIMITER),
    composer = composer,
    year = year,
    trackNumber = trackNumber,
    trackTotal = trackTotal,
    discNumber = discNumber,
    discTotal = discTotal,
    durationMs = durationMs,
    dateAdded = dateAdded,
    dateModified = dateModified,
    filePath = filePath,
    folderPath = folderPath,
    contentUri = contentUri,
    albumArtUri = albumArtUri,
    mimeType = mimeType,
    size = size,
)

fun TrackEntity.toModel(): Track = Track(
    id = id,
    title = title,
    artists = if (artists.isEmpty()) emptyList() else artists.split(LIST_DELIMITER),
    albumArtist = albumArtist,
    album = album,
    albumId = albumId,
    genres = if (genres.isEmpty()) emptyList() else genres.split(LIST_DELIMITER),
    composer = composer,
    year = year,
    trackNumber = trackNumber,
    trackTotal = trackTotal,
    discNumber = discNumber,
    discTotal = discTotal,
    durationMs = durationMs,
    dateAdded = dateAdded,
    dateModified = dateModified,
    filePath = filePath,
    folderPath = folderPath,
    contentUri = contentUri,
    albumArtUri = albumArtUri,
    mimeType = mimeType,
    size = size,
)
