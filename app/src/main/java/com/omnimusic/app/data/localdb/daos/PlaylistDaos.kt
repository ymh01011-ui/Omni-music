package com.omnimusic.app.data.localdb.daos

import androidx.room.*
import com.omnimusic.app.data.localdb.entities.PlaylistEntity
import com.omnimusic.app.data.localdb.entities.PlaylistSongCrossRef
import com.omnimusic.app.data.localdb.entities.PlaylistSongEntity
import com.omnimusic.app.data.localdb.entities.PlaylistWithSongs
import kotlinx.flow.Flow

@Dao
interface PlaylistDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Delete
    suspend fun deletePlaylist(playlist: PlaylistEntity)

    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSong(song: PlaylistSongEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCrossRef(crossRef: PlaylistSongCrossRef)

    @Transaction
    suspend fun addSongToPlaylist(playlistId: Long, song: PlaylistSongEntity) {
        insertSong(song)
        insertCrossRef(PlaylistSongCrossRef(playlistId, song.audioId))
    }

    @Query("DELETE FROM playlist_song_cross_ref WHERE playlistId = :playlistId AND audioId = :audioId")
    suspend fun removeSongFromPlaylist(playlistId: Long, audioId: Long)

    @Transaction
    @Query("SELECT * FROM playlists WHERE playlistId = :playlistId") // تم التعديل هنا ليتوافق مع المعرف الجديد
    fun getPlaylistWithSongs(playlistId: Long): Flow<PlaylistWithSongs?>

    @Transaction
    @Query("SELECT * FROM playlists")
    fun getAllPlaylistsWithSongs(): Flow<List<PlaylistWithSongs>>
}
