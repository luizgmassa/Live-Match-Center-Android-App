package com.massa.livecenter.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.massa.livecenter.data.local.db.LiveCenterDatabase
import com.massa.livecenter.data.local.db.MatchEntity
import com.massa.livecenter.data.local.db.RemoteKeyEntity
import com.massa.livecenter.data.remote.rest.MatchApiService
import javax.inject.Inject

@OptIn(ExperimentalPagingApi::class)
class MatchRemoteMediator @Inject constructor(
    private val apiService: MatchApiService,
    private val database: LiveCenterDatabase
) : RemoteMediator<Int, MatchEntity>() {

    /**
     * Always request a fresh network REFRESH when the pager first subscribes.
     * This guarantees the RemoteMediator is invoked even if Room already has rows
     * from a previous session — important when the mock server generates different
     * data on each launch.
     */
    override suspend fun initialize(): InitializeAction =
        InitializeAction.LAUNCH_INITIAL_REFRESH

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
        return try {
            val page = when (loadType) {
                LoadType.REFRESH -> FIRST_PAGE

                LoadType.PREPEND ->
                    // We never need to load pages before the first — scroll direction is append-only.
                    return MediatorResult.Success(endOfPaginationReached = true)

                LoadType.APPEND -> {
                    // Find the remote key for the last item currently in the paging state.
                    val lastItem = state.lastItemOrNull()
                        ?: return MediatorResult.Success(endOfPaginationReached = true)

                    val remoteKey = database.remoteKeyDao().remoteKeyByMatchId(lastItem.id)
                    remoteKey?.nextPage
                        ?: return MediatorResult.Success(endOfPaginationReached = true)
                }
            }

            val response = apiService.getLiveMatches(
                page = page,
                size = state.config.pageSize
            )

            val matches = response.matches.map { dto ->
                MatchEntity(
                    id = dto.id,
                    homeTeam = dto.homeTeam,
                    awayTeam = dto.awayTeam,
                    score = dto.score,
                    minute = dto.minute
                )
            }

            val remoteKeys = response.matches.map { dto ->
                RemoteKeyEntity(
                    matchId = dto.id,
                    prevPage = if (page == FIRST_PAGE) null else page - 1,
                    nextPage = response.nextPage
                )
            }

            val endOfPaginationReached = response.nextPage == null

            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    database.remoteKeyDao().clearAll()
                    database.matchDao().clearAll()
                }
                database.remoteKeyDao().insertAll(remoteKeys)
                database.matchDao().upsertAll(matches)
            }

            MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (e: Exception) {
            android.util.Log.e("MatchRemoteMediator", "load() failed [${loadType.name}]", e)
            MediatorResult.Error(e)
        }

    }

    companion object {
        private const val FIRST_PAGE = 1
    }
}
