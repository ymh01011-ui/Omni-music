package com.omnimusic.player.data.repository

import com.omnimusic.player.data.local.db.TrackDao
import com.omnimusic.player.data.local.db.toEntity
import com.omnimusic.player.data.local.db.toModel
import com.omnimusic.player.data.mediastore.MediaStoreScanner
import com.omnimusic.player.data.model.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single source of truth for the music library used by the UI layer.
 *
 * [observeTracks] exposes the Room cache reactively (instant on screen open).
 * [refreshFromMediaStore] re-scans the device and writes the result back
 * into the cache, which automatically re-emits to any active observer.
 */
@Singleton
class MusicLibraryRepository @Inject constructor(
    private val scanner: MediaStoreScanner,
    private val trackDao: TrackDao,
) {

    fun observeTracks(): Flow<List<Track>> =
        trackDao.observeAllTracks().map { entities -> entities.map { it.toModel() } }

    suspend fun refreshFromMediaStore() {
        val scanned = scanner.scanAllTracks()
        trackDao.replaceAll(scanned.map { it.toEntity() })
    }
}
