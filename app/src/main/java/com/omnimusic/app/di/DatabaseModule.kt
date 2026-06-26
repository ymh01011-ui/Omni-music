package com.omnimusic.app.data.localdb

import android.content.Context
import androidx.room.Room
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
            "omni_music_database"
        ).fallbackToDestructiveMigration() // تضمن عدم حدوث Crash للمشروع أثناء التطوير عند تغيير الهيكل
         .build()
    }

    @Provides
    @Singleton
    fun providePlaylistDao(database: OmniMusicDatabase): PlaylistDao {
        return database.playlistDao()
    }
}
