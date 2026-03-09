package com.massa.livecenter.presentation.contract

import com.massa.livecenter.data.remote.websocket.WebSocketConnectionState
import com.massa.livecenter.domain.model.Commentary
import com.massa.livecenter.domain.model.Odds

data class LiveMatchUiState(
    val selectedMatchId: String? = null,
    val commentary: List<Commentary> = emptyList(),
    val oddsMap: Map<String, Odds> = emptyMap(),
    val connectionState: WebSocketConnectionState = WebSocketConnectionState.Disconnected,
    val isRefreshing: Boolean = false
)

sealed class LiveMatchUiEvent {
    data class SelectMatch(val matchId: String) : LiveMatchUiEvent()
    object Refresh : LiveMatchUiEvent()
    object DismissDetail : LiveMatchUiEvent()
}

sealed class LiveMatchUiEffect {
    object ScrollToTop : LiveMatchUiEffect()
    data class ShowError(val message: String) : LiveMatchUiEffect()
}
