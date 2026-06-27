package com.omnimusic.player.di

import com.omnimusic.player.data.remote.deezer.DeezerApiService
import com.omnimusic.player.data.remote.itunes.ITunesApiService
import com.omnimusic.player.data.remote.lastfm.LastFmApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder().build()

    @Provides
    @Singleton
    @Named("deezer")
    fun provideDeezerRetrofit(client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl("https://api.deezer.com/")
        .client(client)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    @Provides
    @Singleton
    fun provideDeezerApiService(@Named("deezer") retrofit: Retrofit): DeezerApiService =
        retrofit.create(DeezerApiService::class.java)

    @Provides
    @Singleton
    @Named("itunes")
    fun provideITunesRetrofit(client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl("https://itunes.apple.com/")
        .client(client)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    @Provides
    @Singleton
    fun provideITunesApiService(@Named("itunes") retrofit: Retrofit): ITunesApiService =
        retrofit.create(ITunesApiService::class.java)

    @Provides
    @Singleton
    @Named("lastfm")
    fun provideLastFmRetrofit(client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl("https://ws.audioscrobbler.com/")
        .client(client)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    @Provides
    @Singleton
    fun provideLastFmApiService(@Named("lastfm") retrofit: Retrofit): LastFmApiService =
        retrofit.create(LastFmApiService::class.java)
}
