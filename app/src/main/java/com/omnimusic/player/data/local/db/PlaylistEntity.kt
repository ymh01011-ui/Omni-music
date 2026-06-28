package com.omnimusic.player.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A user-created playlist (per spec section 3/7). Favorites is handled as
 * its own dedicated table ([FavoriteEntity]) rather than a row here, since
 * the reference design treats it as a system-level filter/tab rather than a
 * deletable user playlist - but it's still surfaced in the Playlists grid
 * via [com.omnimusic.player.data.repository.PlaylistRepository].
 */
@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long,
)
