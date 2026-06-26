package com.omnimusic.app.di

import android.content.Context
import androidx.room.Room
import com.omnimusic.app.data.localdb.OmniMusicDatabase
import com.omnimusic.app.data.localdb.daos.PlaylistDao
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
    fun provideOmniMusicDatabase(
        @ApplicationContext context: Context
    ): OmniMusicDatabase {
        return Room.databaseBuilder(
            context,
            OmniMusicDatabase::class.java,
            "omni_music_database.db"
        ).fallbackToDestructiveMigration()
         .build()
    }

    @Provides
    @Singleton
    fun providePlaylistDao(database: OmniMusicDatabase): PlaylistDao {
        return database.playlistDao()
    }
}
