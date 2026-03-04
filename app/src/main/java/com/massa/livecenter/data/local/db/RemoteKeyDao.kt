package com.massa.livecenter.data.local.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface RemoteKeyDao {

    @Upsert
    suspend fun insertAll(keys: List<RemoteKeyEntity>)

    @Query("SELECT * FROM remote_keys WHERE matchId = :matchId")
    suspend fun remoteKeyByMatchId(matchId: String): RemoteKeyEntity?

    @Query("DELETE FROM remote_keys")
    suspend fun clearAll()
}
