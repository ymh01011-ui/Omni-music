package com.omnimusic.player.data.mediastore

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.os.Build
import android.provider.MediaStore
import com.omnimusic.player.data.model.Track
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reads every audio track visible to the app from the device's MediaStore.
 *
 * Per spec section 3, the app reads the *entire* device library automatically
 * (no folder allow/deny list in v1). Genres require a second, per-track query
 * because [MediaStore.Audio.Genres] is indexed by genre -> track IDs, not
 * exposed as a column on the main Audio table.
 *
 * Multiple artists (spec section 4): MediaStore's tagger only exposes a
 * single ARTIST string per track (Android does not natively support
 * multi-valued ID3 TPE1 frames the way some files actually tag them). We
 * split on common separators (";", "/", " feat. ", " ft. ", "&") as a
 * pragmatic first pass; this can be refined later from raw tag data when the
 * Tag Editor reads files directly rather than through MediaStore.
 */
@Singleton
class MediaStoreScanner @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    suspend fun scanAllTracks(): List<Track> = withContext(Dispatchers.IO) {
        val tracks = mutableListOf<Track>()

        val projection = buildList {
            add(MediaStore.Audio.Media._ID)
            add(MediaStore.Audio.Media.TITLE)
            add(MediaStore.Audio.Media.ARTIST)
            add(MediaStore.Audio.Media.ALBUM_ARTIST)
            add(MediaStore.Audio.Media.ALBUM)
            add(MediaStore.Audio.Media.ALBUM_ID)
            add(MediaStore.Audio.Media.COMPOSER)
            add(MediaStore.Audio.Media.YEAR)
            add(MediaStore.Audio.Media.TRACK)
            add(MediaStore.Audio.Media.DURATION)
            add(MediaStore.Audio.Media.DATE_ADDED)
            add(MediaStore.Audio.Media.DATE_MODIFIED)
            add(MediaStore.Audio.Media.DATA) // full file path; deprecated on 29+ but still readable for owned/scoped media rows
            add(MediaStore.Audio.Media.MIME_TYPE)
            add(MediaStore.Audio.Media.SIZE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                add(MediaStore.Audio.Media.GENRE)
            }
        }.toTypedArray()

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"

        context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            "${MediaStore.Audio.Media.TITLE} ASC",
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumArtistCol = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ARTIST)
            val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val composerCol = cursor.getColumnIndex(MediaStore.Audio.Media.COMPOSER)
            val yearCol = cursor.getColumnIndex(MediaStore.Audio.Media.YEAR)
            val trackCol = cursor.getColumnIndex(MediaStore.Audio.Media.TRACK)
            val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dateAddedCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val dateModifiedCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED)
            val dataCol = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
            val mimeCol = cursor.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val genreCol = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                cursor.getColumnIndex(MediaStore.Audio.Media.GENRE)
            } else {
                -1
            }

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val albumId = if (cursor.isNull(albumIdCol)) null else cursor.getLong(albumIdCol)
                val filePath = if (dataCol >= 0) cursor.getStringOrNull(dataCol) else null
                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id
                ).toString()

                val rawArtist = cursor.getStringOrNull(artistCol)
                val rawGenre = if (genreCol >= 0) cursor.getStringOrNull(genreCol) else null

                tracks += Track(
                    id = id,
                    title = cursor.getStringOrNull(titleCol) ?: "Unknown Title",
                    artists = splitMultiValue(rawArtist),
                    albumArtist = if (albumArtistCol >= 0) cursor.getStringOrNull(albumArtistCol) else null,
                    album = cursor.getStringOrNull(albumCol),
                    albumId = albumId,
                    genres = splitMultiValue(rawGenre),
                    composer = if (composerCol >= 0) cursor.getStringOrNull(composerCol) else null,
                    year = if (yearCol >= 0 && !cursor.isNull(yearCol)) cursor.getInt(yearCol) else null,
                    trackNumber = if (trackCol >= 0 && !cursor.isNull(trackCol)) {
                        // MediaStore packs disc*1000 + track into this column.
                        cursor.getInt(trackCol) % 1000
                    } else null,
                    trackTotal = null, // Not exposed by MediaStore; filled in by the Tag Editor's direct file read.
                    discNumber = if (trackCol >= 0 && !cursor.isNull(trackCol)) {
                        cursor.getInt(trackCol) / 1000
                    } else null,
                    discTotal = null,
                    durationMs = cursor.getLong(durationCol),
                    dateAdded = cursor.getLong(dateAddedCol),
                    dateModified = cursor.getLong(dateModifiedCol),
                    filePath = filePath ?: "",
                    folderPath = filePath?.substringBeforeLast('/', missingDelimiterValue = "") ?: "",
                    contentUri = contentUri,
                    albumArtUri = albumId?.let { buildAlbumArtUri(it) },
                    mimeType = if (mimeCol >= 0) cursor.getStringOrNull(mimeCol) else null,
                    size = cursor.getLong(sizeCol),
                )
            }
        }

        tracks
    }

    private fun buildAlbumArtUri(albumId: Long): String {
        return ContentUris.withAppendedId(
            android.net.Uri.parse("content://media/external/audio/albumart"), albumId
        ).toString()
    }

    /**
     * Splits a raw MediaStore tag value into multiple entries. Handles the
     * common multi-artist/genre separators seen in real-world tags. Falls
     * back to a single-element list (or empty list) when there's nothing to
     * split, so callers never need to null-check.
     */
    private fun splitMultiValue(raw: String?): List<String> {
        if (raw.isNullOrBlank() || raw == "<unknown>") return emptyList()
        return raw
            .split(Regex(";|/| feat\\. | ft\\. | featuring |&"))
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }

    private fun Cursor.getStringOrNull(columnIndex: Int): String? {
        if (columnIndex < 0 || isNull(columnIndex)) return null
        return getString(columnIndex)
    }
}
