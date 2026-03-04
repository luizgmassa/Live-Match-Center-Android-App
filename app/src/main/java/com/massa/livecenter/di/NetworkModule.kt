package com.massa.livecenter.di

import com.google.gson.Gson
import com.massa.livecenter.data.remote.rest.MatchApiService
import com.massa.livecenter.data.remote.sse.CommentarySseClient
import com.massa.livecenter.data.remote.websocket.OddsWebSocketClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://api.superbet.dev/v1/"

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

    @Provides
    @Singleton
    fun provideOkHttpClient(loggingInterceptor: HttpLoggingInterceptor): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    @Provides
    @Singleton
    fun provideMatchApiService(retrofit: Retrofit): MatchApiService =
        retrofit.create(MatchApiService::class.java)

    @Provides
    @Singleton
    fun provideOddsWebSocketClient(
        okHttpClient: OkHttpClient,
        gson: Gson
    ): OddsWebSocketClient = OddsWebSocketClient(okHttpClient, gson)

    @Provides
    @Singleton
    fun provideCommentarySseClient(
        okHttpClient: OkHttpClient,
        gson: Gson
    ): CommentarySseClient = CommentarySseClient(okHttpClient, gson)
}
