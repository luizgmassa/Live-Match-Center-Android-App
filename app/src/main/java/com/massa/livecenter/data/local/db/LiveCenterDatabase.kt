package com.massa.livecenter.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [MatchEntity::class, OddsEntity::class, RemoteKeyEntity::class],
    version = 1,
    exportSchema = false
)
abstract class LiveCenterDatabase : RoomDatabase() {
    abstract fun matchDao(): MatchDao
    abstract fun oddsDao(): OddsDao
    abstract fun remoteKeyDao(): RemoteKeyDao
}
