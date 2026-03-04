package com.massa.livecenter.di

import android.content.Context
import androidx.room.Room
import com.massa.livecenter.data.local.db.LiveCenterDatabase
import com.massa.livecenter.data.local.db.MatchDao
import com.massa.livecenter.data.local.db.OddsDao
import com.massa.livecenter.data.local.db.RemoteKeyDao
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
    fun provideDatabase(@ApplicationContext context: Context): LiveCenterDatabase =
        Room.databaseBuilder(
            context,
            LiveCenterDatabase::class.java,
            "live_center.db"
        ).build()

    @Provides
    fun provideMatchDao(database: LiveCenterDatabase): MatchDao = database.matchDao()

    @Provides
    fun provideOddsDao(database: LiveCenterDatabase): OddsDao = database.oddsDao()

    @Provides
    fun provideRemoteKeyDao(database: LiveCenterDatabase): RemoteKeyDao = database.remoteKeyDao()
}
