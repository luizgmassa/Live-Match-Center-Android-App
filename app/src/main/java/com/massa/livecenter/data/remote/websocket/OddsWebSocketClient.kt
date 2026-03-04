package com.massa.livecenter.data.remote.websocket

import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.OkHttpClient
import okhttp3.WebSocket
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OddsWebSocketClient @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val gson: Gson
) {
    companion object {
        private const val WS_URL = "wss://live.superbet.dev/odds"
    }

    private val _connectionState = MutableStateFlow<WebSocketConnectionState>(
        WebSocketConnectionState.Disconnected
    )
    val connectionState: StateFlow<WebSocketConnectionState> = _connectionState

    private val _oddsFlow = MutableSharedFlow<OddsUpdateDto>(replay = 0)
    val oddsFlow: SharedFlow<OddsUpdateDto> = _oddsFlow

    private var webSocket: WebSocket? = null

    fun connect() {
        // TODO: Build an OkHttp Request for WS_URL, create a WebSocketListener that:
        //   - In onOpen()       → set _connectionState to Connected
        //   - In onMessage()    → parse JSON with gson into OddsUpdateDto and emit to _oddsFlow
        //                          (use a CoroutineScope to emit from a non-suspend callback)
        //   - In onFailure()    → set _connectionState to Reconnecting, schedule a reconnect
        //   - In onClosing()    → set _connectionState to Disconnected
        // Then call okHttpClient.newWebSocket(request, listener) and store the result in webSocket
        TODO("Implement WebSocket connection logic")
    }

    fun disconnect() {
        // TODO: Call webSocket?.close(1000, "Client disconnect"), set _connectionState to Disconnected
        TODO("Implement WebSocket disconnect logic")
    }
}
