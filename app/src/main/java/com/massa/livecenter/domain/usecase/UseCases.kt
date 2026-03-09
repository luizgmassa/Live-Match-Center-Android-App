package com.massa.livecenter.domain.usecase

import androidx.paging.PagingData
import com.massa.livecenter.domain.model.Commentary
import com.massa.livecenter.domain.model.Match
import com.massa.livecenter.domain.model.Odds
import com.massa.livecenter.data.remote.websocket.WebSocketConnectionState
import com.massa.livecenter.domain.repository.LiveMatchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class GetLiveMatchesPagerUseCase @Inject constructor(
    private val repository: LiveMatchRepository
) {
    operator fun invoke(): Flow<PagingData<Match>> = repository.getLiveMatchesPager()
}

class ObserveOddsUseCase @Inject constructor(
    private val repository: LiveMatchRepository
) {
    /** Observe real-time odds for a single [matchId] (e.g. a detail view). */
    operator fun invoke(matchId: String): Flow<Odds> = repository.observeOddsForMatch(matchId)
}

class ObserveAllOddsUseCase @Inject constructor(
    private val repository: LiveMatchRepository
) {
    /** Observe ALL real-time odds updates — use this to populate the full match list odds map. */
    operator fun invoke(): Flow<Odds> = repository.observeAllOdds()
}

class ObserveCommentaryUseCase @Inject constructor(
    private val repository: LiveMatchRepository
) {
    operator fun invoke(matchId: String): Flow<Commentary> {
        // TODO: delegate to repository.observeCommentary(matchId)
        TODO("Implement ObserveCommentaryUseCase")
    }
}

class ObserveConnectionStateUseCase @Inject constructor(
    private val repository: LiveMatchRepository
) {
    operator fun invoke(): StateFlow<WebSocketConnectionState> = repository.observeConnectionState()
}

class RefreshMatchesUseCase @Inject constructor(
    private val repository: LiveMatchRepository
) {
    suspend operator fun invoke() = repository.refreshMatches()
}

class ConnectWebSocketUseCase @Inject constructor(
    private val repository: LiveMatchRepository
) {
    operator fun invoke() = repository.connectWebSocket()
}

class DisconnectWebSocketUseCase @Inject constructor(
    private val repository: LiveMatchRepository
) {
    operator fun invoke() = repository.disconnectWebSocket()
}
