package com.omnimusic.app.data.repository

import com.omnimusic.app.data.localdb.daos.PlaylistDao
import com.omnimusic.app.data.localdb.entities.PlaylistEntity
import com.omnimusic.app.data.localdb.entities.PlaylistSongEntity
import com.omnimusic.app.data.localdb.entities.PlaylistWithSongs
import kotlinx.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaylistRepository @Inject constructor(
    private val playlistDao: PlaylistDao
) {
    val allPlaylists: Flow<List<PlaylistEntity>> = playlistDao.getAllPlaylists()
    val playlistsWithSongs: Flow<List<PlaylistWithSongs>> = playlistDao.getAllPlaylistsWithSongs()

    suspend fun createPlaylist(name: String): Long {
        return playlistDao.insertPlaylist(PlaylistEntity(name = name))
    }

    suspend fun deletePlaylist(playlist: PlaylistEntity) {
        playlistDao.deletePlaylist(playlist)
    }

    suspend fun addTrackToPlaylist(playlistId: Long, song: PlaylistSongEntity) {
        playlistDao.addSongToPlaylist(playlistId, song)
    }

    suspend fun removeTrackFromPlaylist(playlistId: Long, audioId: Long) {
        playlistDao.removeSongFromPlaylist(playlistId, audioId)
    }

    fun getPlaylistDetails(playlistId: Long): Flow<PlaylistWithSongs?> {
        return playlistDao.getPlaylistWithSongs(playlistId)
    }
}
