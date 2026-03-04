package com.massa.livecenter.di

import com.massa.livecenter.data.repository.LiveMatchRepositoryImpl
import com.massa.livecenter.domain.repository.LiveMatchRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for binding repository interfaces to their implementations.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindLiveMatchRepository(
        impl: LiveMatchRepositoryImpl
    ): LiveMatchRepository
}
