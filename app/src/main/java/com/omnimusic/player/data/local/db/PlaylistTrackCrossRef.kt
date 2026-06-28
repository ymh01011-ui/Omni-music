package com.omnimusic.player.data.local.db

import androidx.room.Entity

/**
 * Many-to-many join row between a playlist and a track. [position] preserves
 * the order tracks were added/arranged in, since playlists are ordered lists
 * rather than unordered sets.
 */
@Entity(tableName = "playlist_track_cross_ref", primaryKeys = ["playlistId", "trackId"])
data class PlaylistTrackCrossRef(
    val playlistId: Long,
    val trackId: Long,
    val position: Int,
    val addedAt: Long,
)
