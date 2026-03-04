package com.massa.livecenter.data.remote.websocket

sealed class WebSocketConnectionState {
    object Connected : WebSocketConnectionState()
    object Reconnecting : WebSocketConnectionState()
    object Disconnected : WebSocketConnectionState()
}
