package com.massa.livecenter.di

import com.google.gson.Gson
import com.massa.livecenter.data.mock.MockMatchRestServer
import com.massa.livecenter.data.mock.MockOddsWebSocketServer
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
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.HEADERS
        }

    @Provides
    @Singleton
    fun provideOkHttpClient(loggingInterceptor: HttpLoggingInterceptor): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()

    // ------------------------------------------------------------------ //
    //  Mock servers                                                        //
    // ------------------------------------------------------------------ //

    /**
     * Starts the [MockMatchRestServer] and provides it as a Singleton.
     * To point at the real REST API, remove this provider and replace
     * [provideRestBaseUrl] with: `return "https://api.superbet.dev/v1/"`
     */
    @Provides
    @Singleton
    fun provideMockMatchRestServer(gson: Gson): MockMatchRestServer {
        val server = MockMatchRestServer(gson)
        server.start()
        return server
    }

    @Provides
    @Named("restBaseUrl")
    fun provideRestBaseUrl(mockRestServer: MockMatchRestServer): String =
        mockRestServer.baseUrl()

    /**
     * Starts the [MockOddsWebSocketServer] and provides it as a Singleton.
     * To point at the real WebSocket, remove this provider and replace
     * [provideWsUrl] with: `return "wss://live.superbet.dev/odds"`
     */
    @Provides
    @Singleton
    fun provideMockOddsWebSocketServer(gson: Gson): MockOddsWebSocketServer {
        val server = MockOddsWebSocketServer(gson)
        server.start()
        return server
    }

    @Provides
    @Named("wsUrl")
    fun provideWsUrl(mockServer: MockOddsWebSocketServer): String = mockServer.wsUrl()

    // ------------------------------------------------------------------ //
    //  Real clients                                                        //
    // ------------------------------------------------------------------ //

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson,
        @Named("restBaseUrl") baseUrl: String
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
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
        gson: Gson,
        @Named("wsUrl") wsUrl: String
    ): OddsWebSocketClient = OddsWebSocketClient(okHttpClient, gson, wsUrl)

    @Provides
    @Singleton
    fun provideCommentarySseClient(
        okHttpClient: OkHttpClient,
        gson: Gson
    ): CommentarySseClient = CommentarySseClient(okHttpClient, gson)
}
