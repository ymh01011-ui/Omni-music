package com.omnimusic.player.di

import com.omnimusic.player.data.remote.deezer.DeezerApiService
import com.omnimusic.player.data.remote.itunes.ITunesApiService
import com.omnimusic.player.data.remote.lastfm.LastFmApiService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
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

    /**
     * Critical: MoshiConverterFactory.create() with NO arguments compiles
     * fine but throws "Unable to create converter for class ..." at runtime
     * for every Kotlin data class, because the resulting Moshi instance has
     * no adapter that understands Kotlin's non-null/default-value semantics.
     * We must build our own Moshi with KotlinJsonAdapterFactory registered
     * and pass THAT instance into MoshiConverterFactory.create(moshi).
     */
    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    @Provides
    @Singleton
    @Named("deezer")
    fun provideDeezerRetrofit(client: OkHttpClient, moshi: Moshi): Retrofit = Retrofit.Builder()
        .baseUrl("https://api.deezer.com/")
        .client(client)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    @Provides
    @Singleton
    fun provideDeezerApiService(@Named("deezer") retrofit: Retrofit): DeezerApiService =
        retrofit.create(DeezerApiService::class.java)

    @Provides
    @Singleton
    @Named("itunes")
    fun provideITunesRetrofit(client: OkHttpClient, moshi: Moshi): Retrofit = Retrofit.Builder()
        .baseUrl("https://itunes.apple.com/")
        .client(client)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    @Provides
    @Singleton
    fun provideITunesApiService(@Named("itunes") retrofit: Retrofit): ITunesApiService =
        retrofit.create(ITunesApiService::class.java)

    @Provides
    @Singleton
    @Named("lastfm")
    fun provideLastFmRetrofit(client: OkHttpClient, moshi: Moshi): Retrofit = Retrofit.Builder()
        .baseUrl("https://ws.audioscrobbler.com/")
        .client(client)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    @Provides
    @Singleton
    fun provideLastFmApiService(@Named("lastfm") retrofit: Retrofit): LastFmApiService =
        retrofit.create(LastFmApiService::class.java)
}
