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
        // TODO: delegate to repository.getLiveMatchesPager()
    operator fun invoke(): Flow<PagingData<Match>> = repository.getLiveMatchesPager()
}


class ObserveOddsUseCase @Inject constructor(
    private val repository: LiveMatchRepository
) {
    operator fun invoke(matchId: String): Flow<Odds> {
        // TODO: delegate to repository.observeOddsForMatch(matchId)
        TODO("Implement ObserveOddsUseCase")
    }
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
    operator fun invoke(): StateFlow<WebSocketConnectionState> {
        // TODO: delegate to repository.observeConnectionState()
        TODO("Implement ObserveConnectionStateUseCase")
    }
}

class RefreshMatchesUseCase @Inject constructor(
    private val repository: LiveMatchRepository
) {
    suspend operator fun invoke() {
        // TODO: delegate to repository.refreshMatches()
        TODO("Implement RefreshMatchesUseCase")
    }
}
