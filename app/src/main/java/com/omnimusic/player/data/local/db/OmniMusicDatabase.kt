package com.omnimusic.player.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * App-wide Room database. Holds the MediaStore track cache, the resolved
 * artist-image cache, and now (v3) the Playlists/Favorites/History tables
 * from spec section 3/7.
 */
@Database(
    entities = [
        TrackEntity::class,
        ArtistImageEntity::class,
        PlaylistEntity::class,
        PlaylistTrackCrossRef::class,
        FavoriteEntity::class,
        PlayHistoryEntity::class,
    ],
    version = 3,
    exportSchema = false,
)
abstract class OmniMusicDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
    abstract fun artistImageDao(): ArtistImageDao
    abstract fun playlistDao(): PlaylistDao
}
