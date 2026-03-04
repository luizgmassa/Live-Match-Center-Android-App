package com.massa.livecenter.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.massa.livecenter.data.local.db.LiveCenterDatabase
import com.massa.livecenter.data.remote.sse.CommentarySseClient
import com.massa.livecenter.data.remote.websocket.OddsWebSocketClient
import com.massa.livecenter.data.remote.websocket.WebSocketConnectionState
import com.massa.livecenter.domain.model.Commentary
import com.massa.livecenter.domain.model.Match
import com.massa.livecenter.domain.model.Odds
import com.massa.livecenter.domain.repository.LiveMatchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LiveMatchRepositoryImpl @Inject constructor(
    private val database: LiveCenterDatabase,
    private val oddsWebSocketClient: OddsWebSocketClient,
    private val commentarySseClient: CommentarySseClient,
    private val matchRemoteMediator: MatchRemoteMediator
) : LiveMatchRepository {

    companion object {
        private const val PAGE_SIZE = 20
    }

    @OptIn(ExperimentalPagingApi::class)
    override fun getLiveMatchesPager(): Flow<PagingData<Match>> {
        return Pager(
            config = PagingConfig(pageSize = PAGE_SIZE, enablePlaceholders = false),
            // TODO: add remoteMediator = matchRemoteMediator
            pagingSourceFactory = { database.matchDao().getPagedMatches() }
        ).flow.map { pagingData ->
            // TODO: map MatchEntity → domain Match
            // pagingData.map { entity -> entity.toDomain() }
            @Suppress("UNCHECKED_CAST")
            pagingData as PagingData<Match>
        }
    }

    override fun observeOddsForMatch(matchId: String): Flow<Odds> {
        // TODO: Filter oddsWebSocketClient.oddsFlow by matchId, map OddsUpdateDto → domain Odds
        throw NotImplementedError("observeOddsForMatch is not yet implemented")
    }

    override fun observeCommentary(matchId: String): Flow<Commentary> {
        // TODO: Call commentarySseClient.observeCommentary(matchId), map CommentaryEventDto → domain Commentary
        throw NotImplementedError("observeCommentary is not yet implemented")
    }

    override fun observeConnectionState(): StateFlow<WebSocketConnectionState> {
        // TODO: Return oddsWebSocketClient.connectionState
        throw NotImplementedError("observeConnectionState is not yet implemented")
    }

    override suspend fun refreshMatches() {
        // TODO: Trigger a fresh load — e.g. invalidate the PagingSource or call the mediator REFRESH path directly
        throw NotImplementedError("refreshMatches is not yet implemented")
    }
}
