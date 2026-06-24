package com.omnimusic.player.di

import android.content.Context
import androidx.room.Room
import com.omnimusic.player.data.local.db.OmniMusicDatabase
import com.omnimusic.player.data.local.db.TrackDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideOmniMusicDatabase(@ApplicationContext context: Context): OmniMusicDatabase {
        return Room.databaseBuilder(
            context,
            OmniMusicDatabase::class.java,
            "omni_music.db",
        ).build()
    }

    @Provides
    @Singleton
    fun provideTrackDao(database: OmniMusicDatabase): TrackDao = database.trackDao()
}
