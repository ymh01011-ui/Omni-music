package com.omnimusic.player.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ArtistImageDao {

    @Query("SELECT * FROM artist_images WHERE artistName = :artistName")
    suspend fun getCachedImage(artistName: String): ArtistImageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ArtistImageEntity)
}
