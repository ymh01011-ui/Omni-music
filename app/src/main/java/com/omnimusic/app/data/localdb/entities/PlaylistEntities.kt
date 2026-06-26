package com.omnimusic.app.data.localdb.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "playlist_songs")
data class PlaylistSongEntity(
    @PrimaryKey val audioId: Long, // معرف الأغنية من الـ MediaStore
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

// الحل هنا: تحديد الـ parentColumn والـ entityColumn داخل الـ Junction صراحة لـ KSP
data class PlaylistWithSongs(
    @Embedded val playlist: PlaylistEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "audioId",
        associateBy = Junction(
            value = PlaylistSongCrossRef::class,
            parentColumn = "playlistId",
            entityColumn = "audioId"
        )
    )
    val songs: List<PlaylistSongEntity>
)
