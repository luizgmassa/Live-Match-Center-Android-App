package com.massa.livecenter.data.local.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface OddsDao {

    @Upsert
    suspend fun upsertOdds(odds: OddsEntity)

    @Query("SELECT * FROM odds WHERE matchId = :matchId")
    fun getOddsByMatchId(matchId: String): Flow<OddsEntity?>
}
