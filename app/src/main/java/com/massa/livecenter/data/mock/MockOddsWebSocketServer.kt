package com.massa.livecenter.data.mock

import com.google.gson.Gson
import com.massa.livecenter.data.remote.websocket.OddsDto
import com.massa.livecenter.data.remote.websocket.OddsUpdateDto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * A local mock WebSocket server that simulates the real `wss://live.superbet.dev/odds` endpoint.
 *
 * Uses [MockWebServer] from OkHttp to accept WebSocket upgrade requests, then broadcasts
 * randomly-varying odds updates every [BROADCAST_INTERVAL_MS] milliseconds.
 *
 * Lifecycle: call [start] once at app startup; [shutdown] in `onCleared` or app exit.
 */
@Singleton
class MockOddsWebSocketServer @Inject constructor(private val gson: Gson) {

    companion object {
        /** How often to push an odds update to every connected client (ms). */
        private const val BROADCAST_INTERVAL_MS = 2_000L

        /**
         * Fixed set of match IDs that the mock server cycles through.
         * These should match what the mock REST endpoint (or Room) provides so that
         * odds updates associate correctly with cards in the list.
         */
        val MATCH_IDS = listOf(
            "match_001", "match_002", "match_003",
            "match_004", "match_005", "match_006"
        )
    }

    private val mockWebServer = MockWebServer()

    /** SupervisorJob so individual broadcast coroutines don't cancel the whole scope. */
    private val serverScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /** Tracks per-connection broadcast jobs so they can be cancelled on disconnect. */
    private val broadcastJobs = mutableMapOf<WebSocket, Job>()

    init {
        mockWebServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return MockResponse().withWebSocketUpgrade(ServerWebSocketListener())
            }
        }
    }

    /** Starts the MockWebServer on an available local port.
     * Uses [runBlocking] + [Dispatchers.IO] so the socket/DNS work happens off the main thread
     * while keeping the call synchronous (so [wsUrl] is ready immediately after). */
    fun start() {
        runBlocking(Dispatchers.IO) {
            mockWebServer.start()
        }
    }

    /**
     * Returns the WebSocket URL for this mock server.
     * e.g. `ws://localhost:PORT/`
     */
    fun wsUrl(): String = mockWebServer.url("/").toString()
        .replaceFirst("http", "ws")

    /** Stops the server and all active broadcast coroutines. */
    fun shutdown() {
        serverScope.cancel()
        mockWebServer.shutdown()
    }

    // ------------------------------------------------------------------ //
    //  Server-side WebSocket listener                                      //
    // ------------------------------------------------------------------ //

    private inner class ServerWebSocketListener : WebSocketListener() {

        override fun onOpen(webSocket: WebSocket, response: Response) {
            val job = serverScope.launch {
                while (isActive) {
                    delay(BROADCAST_INTERVAL_MS)
                    val payload = buildRandomOddsUpdate()
                    webSocket.send(gson.toJson(payload))
                }
            }
            broadcastJobs[webSocket] = job
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            broadcastJobs.remove(webSocket)?.cancel()
            webSocket.close(1000, null)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            broadcastJobs.remove(webSocket)?.cancel()
        }
    }

    // ------------------------------------------------------------------ //
    //  Mock data generation                                                //
    // ------------------------------------------------------------------ //

    /** Returns a [OddsUpdateDto] for a random match with slightly jittered odds values. */
    private fun buildRandomOddsUpdate(): OddsUpdateDto {
        val matchId = MATCH_IDS.random()
        return OddsUpdateDto(
            matchId = matchId,
            odds = OddsDto(
                home = (1.20 + Random.nextDouble() * 3.80).roundTo(2),
                draw = (2.50 + Random.nextDouble() * 2.00).roundTo(2),
                away = (1.20 + Random.nextDouble() * 3.80).roundTo(2)
            )
        )
    }

    private fun Double.roundTo(decimals: Int): Double {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10 }
        return kotlin.math.round(this * multiplier) / multiplier
    }
}
