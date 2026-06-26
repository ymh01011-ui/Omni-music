package com.omnimusic.app.data.localdb.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val playlistId: Long = 0, // تم تغيير الاسم هنا ليتطابق تلقائياً
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "playlist_songs")
data class PlaylistSongEntity(
    @PrimaryKey val audioId: Long, 
    val title: String,
    val artist: String,
    val albumId: Long,
    val duration: Long,
    val albumArtUri: String? = null
)

@Entity(
    tableName = "playlist_song_cross_ref",
    primaryKeys = ["playlistId", "audioId"]
)
data class PlaylistSongCrossRef(
    val playlistId: Long,
    val audioId: Long
)

// الآن بما أن الأسماء متطابقة تماماً، الـ KSP سيتعرف عليها فوراً بدون أي مشاكل
data class PlaylistWithSongs(
    @Embedded val playlist: PlaylistEntity,
    @Relation(
        parentColumn = "playlistId",
        entityColumn = "audioId",
        associateBy = Junction(PlaylistSongCrossRef::class)
    )
    val songs: List<PlaylistSongEntity>
)
