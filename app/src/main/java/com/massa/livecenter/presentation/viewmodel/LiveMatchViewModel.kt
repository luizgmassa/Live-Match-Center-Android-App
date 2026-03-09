package com.massa.livecenter.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.massa.livecenter.domain.model.Match
import com.massa.livecenter.domain.usecase.ConnectWebSocketUseCase
import com.massa.livecenter.domain.usecase.DisconnectWebSocketUseCase
import com.massa.livecenter.domain.usecase.GetLiveMatchesPagerUseCase
import com.massa.livecenter.domain.usecase.ObserveAllOddsUseCase
import com.massa.livecenter.domain.usecase.ObserveCommentaryUseCase
import com.massa.livecenter.domain.usecase.ObserveConnectionStateUseCase
import com.massa.livecenter.domain.usecase.RefreshMatchesUseCase
import com.massa.livecenter.presentation.contract.LiveMatchUiEffect
import com.massa.livecenter.presentation.contract.LiveMatchUiEvent
import com.massa.livecenter.presentation.contract.LiveMatchUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LiveMatchViewModel @Inject constructor(
    private val getLiveMatchesPager: GetLiveMatchesPagerUseCase,
    private val observeAllOdds: ObserveAllOddsUseCase,
    private val observeCommentary: ObserveCommentaryUseCase,
    private val observeConnectionState: ObserveConnectionStateUseCase,
    private val refreshMatches: RefreshMatchesUseCase,
    private val connectWebSocket: ConnectWebSocketUseCase,
    private val disconnectWebSocket: DisconnectWebSocketUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LiveMatchUiState())
    val uiState: StateFlow<LiveMatchUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<LiveMatchUiEffect>()
    val uiEffect: SharedFlow<LiveMatchUiEffect> = _uiEffect.asSharedFlow()

    /**
     * Paging data flow, safe to collect with [androidx.paging.compose.collectAsLazyPagingItems].
     * Cached in [viewModelScope] so recompositions don't restart the upstream Flow.
     */
    val matchesPagingFlow: Flow<PagingData<Match>> = getLiveMatchesPager()
        .cachedIn(viewModelScope)

    private var commentaryJob: Job? = null

    init {
        connectWebSocket()

        // Observe WebSocket connection state -> update UI
        viewModelScope.launch {
            observeConnectionState().collect { state ->
                _uiState.update { it.copy(connectionState = state) }
            }
        }

        // Collect ALL real-time odds updates from the WebSocket and accumulate into
        // a matchId -> Odds map. Each MatchCard reads its odds from this map.
        viewModelScope.launch {
            observeAllOdds().collect { odds ->
                _uiState.update { current ->
                    current.copy(oddsMap = current.oddsMap + (odds.matchId to odds))
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        disconnectWebSocket()
    }

    fun onEvent(event: LiveMatchUiEvent) {
        when (event) {
            is LiveMatchUiEvent.SelectMatch -> {
                _uiState.update { it.copy(selectedMatchId = event.matchId, commentary = emptyList()) }
                //startObservingCommentary(event.matchId)
            }

            is LiveMatchUiEvent.Refresh -> {
                viewModelScope.launch {
                    _uiState.update { it.copy(isRefreshing = true) }
                    try {
                        refreshMatches()
                        _uiEffect.emit(LiveMatchUiEffect.ScrollToTop)
                    } catch (e: Exception) {
                        _uiEffect.emit(LiveMatchUiEffect.ShowError(e.message ?: "Refresh failed"))
                    } finally {
                        _uiState.update { it.copy(isRefreshing = false) }
                    }
                }
            }

            is LiveMatchUiEvent.DismissDetail -> {
                commentaryJob?.cancel()
                commentaryJob = null
                _uiState.update { it.copy(selectedMatchId = null, commentary = emptyList()) }
            }
        }
    }

    private fun observeOddsForMatch(matchId: String) {
        // TODO: oddsJob = viewModelScope.launch {
        //   observeOdds(matchId).collect { odds ->
        //       // odds are emitted per matchId — store in a map in _uiState if needed
        //   }
        // }
    }

    private fun observeCommentaryForMatch(matchId: String) {
        // TODO: commentaryJob = viewModelScope.launch {
        //   observeCommentary(matchId).collect { commentary ->
        //       _uiState.update { it.copy(commentary = it.commentary + commentary) }
        //   }
        // }
    }
}
