package com.massa.livecenter.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.massa.livecenter.data.local.db.LiveCenterDatabase
import com.massa.livecenter.data.local.db.MatchEntity
import com.massa.livecenter.data.remote.rest.MatchApiService
import javax.inject.Inject

@OptIn(ExperimentalPagingApi::class)
class MatchRemoteMediator @Inject constructor(
    private val apiService: MatchApiService,
    private val database: LiveCenterDatabase
) : RemoteMediator<Int, MatchEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, MatchEntity>
    ): MediatorResult {
        // TODO: Implement mediation logic:
        //   REFRESH:
        //     - Fetch page 1 from apiService.getLiveMatches(page = 1, size = state.config.pageSize)
        //     - In a single Room transaction: clearAll matches & remote keys, then upsertAll new items
        //       and insertAll remote keys (prevPage = null, nextPage = response.nextPage)
        //     - Return MediatorResult.Success(endOfPaginationReached = response.nextPage == null)
        //
        //   PREPEND:
        //     - Return MediatorResult.Success(endOfPaginationReached = true)  (no prepend needed)
        //
        //   APPEND:
        //     - Get the last MatchEntity from state (state.lastItemOrNull())
        //     - Look up its remote key via database.remoteKeyDao().remoteKeyByMatchId(lastItem.id)
        //     - If nextPage == null → return MediatorResult.Success(endOfPaginationReached = true)
        //     - Otherwise fetch that page and upsert; return Success with endOfPagination flag
        //
        //   Wrap everything in try/catch and return MediatorResult.Error(e) on failure.
        return MediatorResult.Error(NotImplementedError("MatchRemoteMediator.load() is not yet implemented"))
    }
}
