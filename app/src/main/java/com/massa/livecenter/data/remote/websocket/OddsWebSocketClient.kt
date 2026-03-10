package com.massa.livecenter.data.remote.websocket

import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * OkHttp WebSocket client that connects to the odds stream.
 *
 * The target URL is injected via [@Named("wsUrl")] so the same class works with both
 * the real production server and the local [MockOddsWebSocketServer] during development.
 */
@Singleton
class OddsWebSocketClient @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val gson: Gson,
    @Named("wsUrl") private val wsUrl: String
) {
    private val _connectionState = MutableStateFlow<WebSocketConnectionState>(
        WebSocketConnectionState.Disconnected
    )
    val connectionState: StateFlow<WebSocketConnectionState> = _connectionState

    private val _oddsFlow = MutableSharedFlow<OddsUpdateDto>(replay = 0)
    val oddsFlow: SharedFlow<OddsUpdateDto> = _oddsFlow

    private var webSocket: WebSocket? = null

    fun connect() {
        val request = Request.Builder().url(wsUrl).build()
        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                _connectionState.value = WebSocketConnectionState.Connected
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                val dto = gson.fromJson(text, OddsUpdateDto::class.java)
                CoroutineScope(Dispatchers.IO).launch {
                    _oddsFlow.emit(dto)
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                webSocket.close(1000, "Server error")
                _connectionState.value = WebSocketConnectionState.Reconnecting
                CoroutineScope(Dispatchers.IO).launch { connect() }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                _connectionState.value = WebSocketConnectionState.Disconnected
            }
        }
        webSocket = okHttpClient.newWebSocket(request, listener)
    }

    fun disconnect() {
        webSocket?.close(1000, "Client disconnect")
        _connectionState.value = WebSocketConnectionState.Disconnected
        webSocket = null
    }
}
