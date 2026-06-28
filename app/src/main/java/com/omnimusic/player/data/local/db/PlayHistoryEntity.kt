package com.omnimusic.player.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A single playback event, used to drive the History tab toggle (spec
 * section 3) and, later, "Most played" / "Recently played" sections on
 * Home. One row per play (not one row per track), so play counts and
 * recency can both be derived from this table.
 */
@Entity(tableName = "play_history")
data class PlayHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val trackId: Long,
    val playedAt: Long,
)
