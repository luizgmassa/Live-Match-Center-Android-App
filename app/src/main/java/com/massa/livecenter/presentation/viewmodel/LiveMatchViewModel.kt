package com.massa.livecenter.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.massa.livecenter.domain.model.Match
import com.massa.livecenter.domain.usecase.GetLiveMatchesPagerUseCase
import com.massa.livecenter.domain.usecase.ObserveCommentaryUseCase
import com.massa.livecenter.domain.usecase.ObserveConnectionStateUseCase
import com.massa.livecenter.domain.usecase.ObserveOddsUseCase
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
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

@HiltViewModel
class LiveMatchViewModel @Inject constructor(
    private val getLiveMatchesPager: GetLiveMatchesPagerUseCase,
    private val observeOdds: ObserveOddsUseCase,
    private val observeCommentary: ObserveCommentaryUseCase,
    private val observeConnectionState: ObserveConnectionStateUseCase,
    private val refreshMatches: RefreshMatchesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LiveMatchUiState())
    val uiState: StateFlow<LiveMatchUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<LiveMatchUiEffect>()
    val uiEffect: SharedFlow<LiveMatchUiEffect> = _uiEffect.asSharedFlow()

    /**
     * Paging data flow, safe to collect with [androidx.paging.compose.collectAsLazyPagingItems]
     * in a Composable. Returns an empty flow until the candidate implements the use case.
     *
     * TODO (candidate): After implementing GetLiveMatchesPagerUseCase, replace the placeholder
     * with: getLiveMatchesPager().cachedIn(viewModelScope)
     */
    val matchesPagingFlow: Flow<PagingData<Match>> = flowOf(PagingData.empty())
        // TODO: replace with getLiveMatchesPager().cachedIn(viewModelScope)

    private var oddsJob: Job? = null
    private var commentaryJob: Job? = null

    init {
        // TODO: Start observing connection state:
        //   viewModelScope.launch {
        //       observeConnectionState().collect { state ->
        //           _uiState.update { it.copy(connectionState = state) }
        //       }
        //   }
    }

    fun onEvent(event: LiveMatchUiEvent) {
        when (event) {
            is LiveMatchUiEvent.SelectMatch -> {
                // TODO: Update _uiState with selectedMatchId = event.matchId
                // TODO: Cancel any active oddsJob and commentaryJob
                // TODO: Call observeOddsForMatch(event.matchId) and observeCommentaryForMatch(event.matchId)
            }
            is LiveMatchUiEvent.Refresh -> {
                // TODO: Set isRefreshing = true in _uiState
                // TODO: viewModelScope.launch {
                //          try { refreshMatches(); _uiEffect.emit(LiveMatchUiEffect.ScrollToTop) }
                //          catch (e: Exception) { _uiEffect.emit(LiveMatchUiEffect.ShowError(e.message ?: "Unknown error")) }
                //          finally { _uiState.update { it.copy(isRefreshing = false) } }
                //       }
            }
            is LiveMatchUiEvent.DismissDetail -> {
                // TODO: _uiState.update { it.copy(selectedMatchId = null, commentary = emptyList()) }
                // TODO: Cancel oddsJob and commentaryJob
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
