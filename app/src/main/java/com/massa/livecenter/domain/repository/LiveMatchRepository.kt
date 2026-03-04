package com.massa.livecenter.domain.repository

import androidx.paging.PagingData
import com.massa.livecenter.data.remote.websocket.WebSocketConnectionState
import com.massa.livecenter.domain.model.Commentary
import com.massa.livecenter.domain.model.Match
import com.massa.livecenter.domain.model.Odds
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface LiveMatchRepository {

    /**
     * Returns a [Flow] of [PagingData] containing cached live matches, pagination
     * handled by [MatchRemoteMediator].
     */
    fun getLiveMatchesPager(): Flow<PagingData<Match>>

    /**
     * Subscribes to real-time odds updates for the given [matchId] via WebSocket.
     */
    fun observeOddsForMatch(matchId: String): Flow<Odds>

    /**
     * Subscribes to live commentary events for the given [matchId] via SSE.
     */
    fun observeCommentary(matchId: String): Flow<Commentary>

    /**
     * Emits the current WebSocket connection state and any subsequent changes.
     */
    fun observeConnectionState(): StateFlow<WebSocketConnectionState>

    /**
     * Triggers a full data refresh — equivalent to a remote REFRESH load in the mediator.
     */
    suspend fun refreshMatches()
}
