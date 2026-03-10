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
import com.massa.livecenter.domain.model.CommentaryType
import com.massa.livecenter.domain.model.Match
import com.massa.livecenter.domain.model.Odds
import com.massa.livecenter.domain.repository.LiveMatchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
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
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                enablePlaceholders = false,
                prefetchDistance = 5
            ),
            remoteMediator = matchRemoteMediator,
            pagingSourceFactory = { database.matchDao().getPagedMatches() }
        ).flow.map { pagingData ->
            pagingData.map { entity ->
                Match(
                    id = entity.id,
                    homeTeam = entity.homeTeam,
                    awayTeam = entity.awayTeam,
                    score = entity.score,
                    minute = entity.minute
                )
            }
        }
    }

    override fun observeOddsForMatch(matchId: String): Flow<Odds> {
        return oddsWebSocketClient.oddsFlow
            .filter { it.matchId == matchId }
            .map { dto ->
                Odds(
                    matchId = dto.matchId,
                    home = dto.odds.home,
                    draw = dto.odds.draw,
                    away = dto.odds.away
                )
            }
    }

    override fun observeAllOdds(): Flow<Odds> {
        return oddsWebSocketClient.oddsFlow.map { dto ->
            Odds(
                matchId = dto.matchId,
                home = dto.odds.home,
                draw = dto.odds.draw,
                away = dto.odds.away
            )
        }
    }

    override fun observeCommentary(matchId: String): Flow<Commentary> {
        return commentarySseClient.observeCommentary(matchId).map { dto ->
            Commentary(
                minute = dto.minute,
                text   = dto.text,
                type   = parseCommentaryType(dto.type)
            )
        }
    }

    override fun observeConnectionState(): StateFlow<WebSocketConnectionState> {
        return oddsWebSocketClient.connectionState
    }

    override fun connectWebSocket() {
        oddsWebSocketClient.connect()
    }

    override fun disconnectWebSocket() {
        oddsWebSocketClient.disconnect()
    }

    override suspend fun refreshMatches() {
        // Clearing the table invalidates the active PagingSource, which triggers
        // a fresh REFRESH load through the RemoteMediator.
        database.matchDao().clearAll()
    }

    private fun parseCommentaryType(raw: String): CommentaryType =
        runCatching { CommentaryType.valueOf(raw) }.getOrDefault(CommentaryType.GENERAL)
}
