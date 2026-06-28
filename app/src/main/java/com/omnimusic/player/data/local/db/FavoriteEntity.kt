package com.omnimusic.player.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A favorited track. Modeled as its own table rather than a flag on
 * [TrackEntity] so favoriting survives the destructive MediaStore
 * cache rebuild ([TrackDao.replaceAll]) - tracks come and go from the cache
 * as the library is rescanned, but a favorite, once set, should persist by
 * track ID independent of that cache lifecycle.
 */
@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val trackId: Long,
    val addedAt: Long,
)
