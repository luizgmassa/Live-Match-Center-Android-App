package com.massa.livecenter.data.local.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface MatchDao {

    @Upsert
    suspend fun upsertAll(matches: List<MatchEntity>)

    @Query("SELECT * FROM matches ORDER BY minute DESC")
    fun getPagedMatches(): PagingSource<Int, MatchEntity>

    @Query("DELETE FROM matches")
    suspend fun clearAll()
}
