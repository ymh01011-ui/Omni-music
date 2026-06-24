package com.omnimusic.player.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {

    @Query("SELECT * FROM tracks ORDER BY title ASC")
    fun observeAllTracks(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE id = :id")
    suspend fun getTrackById(id: Long): TrackEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tracks: List<TrackEntity>)

    @Query("DELETE FROM tracks")
    suspend fun deleteAll()

    @Query("DELETE FROM tracks WHERE id NOT IN (:keepIds)")
    suspend fun deleteMissing(keepIds: List<Long>)

    /**
     * Replaces the entire cache with a fresh MediaStore scan result in one
     * transaction: removes rows for tracks that no longer exist on disk,
     * then upserts everything currently found. Keeps favorites/history
     * (stored in separate tables, see Phase 3) unaffected since we only
     * touch the tracks table here.
     */
    @Transaction
    suspend fun replaceAll(tracks: List<TrackEntity>) {
        if (tracks.isEmpty()) {
            deleteAll()
            return
        }
        deleteMissing(tracks.map { it.id })
        insertAll(tracks)
    }
}
