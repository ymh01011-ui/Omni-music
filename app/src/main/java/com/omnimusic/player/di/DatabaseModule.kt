package com.omnimusic.player.di

import android.content.Context
import androidx.room.Room
import com.omnimusic.player.data.local.db.ArtistImageDao
import com.omnimusic.player.data.local.db.OmniMusicDatabase
import com.omnimusic.player.data.local.db.PlaylistDao
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
        )
            // The app is still pre-release and the track/artist-image tables
            // are caches re-populated from MediaStore/network on next launch,
            // so destructive migration is safe and avoids hand-writing
            // Migration objects for every schema bump during active development.
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideTrackDao(database: OmniMusicDatabase): TrackDao = database.trackDao()

    @Provides
    @Singleton
    fun provideArtistImageDao(database: OmniMusicDatabase): ArtistImageDao = database.artistImageDao()

    @Provides
    @Singleton
    fun providePlaylistDao(database: OmniMusicDatabase): PlaylistDao = database.playlistDao()
}
