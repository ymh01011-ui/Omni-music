package com.omnimusic.app.data.localdb

import androidx.room.Database
import androidx.room.RoomDatabase
import com.omnimusic.app.data.localdb.daos.PlaylistDao
import com.omnimusic.app.data.localdb.entities.PlaylistEntity
import com.omnimusic.app.data.localdb.entities.PlaylistSongCrossRef
import com.omnimusic.app.data.localdb.entities.PlaylistSongEntity

@Database(
    entities = [
        PlaylistEntity::class,
        PlaylistSongEntity::class,
        PlaylistSongCrossRef::class // هذا السطر هو الحل الجذري للـ MissingType
    ],
    version = 1,
    exportSchema = false
)
abstract class OmniMusicDatabase : RoomDatabase() {
    abstract fun playlistDao(): PlaylistDao
}
