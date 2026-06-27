package com.omnimusic.player.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * App-wide Room database. Phase 3 will add Favorites, History, Most Played,
 * and Playlist tables here as additional entities + further version bumps
 * (kept in one DB so playlist/favorite rows can reference track IDs with
 * simple foreign keys later).
 */
@Database(
    entities = [TrackEntity::class, ArtistImageEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class OmniMusicDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
    abstract fun artistImageDao(): ArtistImageDao
}
