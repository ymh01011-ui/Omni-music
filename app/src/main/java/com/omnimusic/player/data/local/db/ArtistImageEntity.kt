package com.omnimusic.player.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Caches the resolved artist image URL (or the fact that none was found) so
 * we don't re-query Deezer/iTunes/Last.fm on every app launch or list scroll.
 * [imageUrl] is null when no usable image was found anywhere - the UI then
 * falls back to the initial-letter placeholder (spec section 6) and we
 * don't retry until [resolvedAt] is old enough (see ArtistImageRepository).
 */
@Entity(tableName = "artist_images")
data class ArtistImageEntity(
    @PrimaryKey val artistName: String,
    val imageUrl: String?,
    val resolvedAt: Long,
)
