package com.massa.livecenter.data.mock

import com.google.gson.Gson
import com.massa.livecenter.data.remote.sse.CommentaryEventDto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.net.ServerSocket
import java.net.SocketException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A raw-TCP SSE server that streams match commentary events dynamically.
 *
 * Unlike [MockMatchRestServer] (which uses OkHttp's MockWebServer and sends the full body at
 * once), this class uses a plain [ServerSocket] so it can delay between events — giving the UI
 * a realistic "live commentary rolling in" experience.
 *
 * Event delivery:
 * - First event after [INITIAL_DELAY_MS]
 * - Each subsequent event after [BETWEEN_EVENTS_MS]
 *
 * Each client connection is served in its own child coroutine.  When the client disconnects
 * (e.g. the user dismisses the bottom sheet), the socket write fails and the coroutine exits
 * cleanly.
 */
@Singleton
class MockCommentarySseServer @Inject constructor(private val gson: Gson) {

    companion object {
        private const val INITIAL_DELAY_MS     = 1_000L
        private const val BETWEEN_EVENTS_MS    = 3_000L
    }

    private var serverSocket: ServerSocket? = null
    private val serverScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    val port: Int get() = serverSocket?.localPort ?: 0

    /** Returns the HTTP base URL for this server, e.g. `http://localhost:PORT/` */
    fun baseUrl(): String = "http://localhost:$port/"

    /**
     * Binds the [ServerSocket] on the IO thread (avoids StrictMode) and starts
     * the accept-loop coroutine.
     */
    fun start() {
        runBlocking(Dispatchers.IO) {
            serverSocket = ServerSocket(0) // OS assigns a free port
        }
        serverScope.launch { acceptLoop() }
    }

    fun shutdown() {
        serverScope.cancel()
        serverSocket?.runCatching { close() }
    }

    // ------------------------------------------------------------------ //
    //  Accept loop                                                         //
    // ------------------------------------------------------------------ //

    private suspend fun acceptLoop() {
        while (true) {
            val client = try {
                withContext(Dispatchers.IO) { serverSocket?.accept() } ?: break
            } catch (e: SocketException) {
                break // Server socket was closed — exit gracefully
            }
            // Handle each connection in a separate child coroutine so one slow
            // client doesn't block new connections.
            serverScope.launch { handleClient(client) }
        }
    }

    private suspend fun handleClient(socket: java.net.Socket) {
        try {
            val reader = socket.getInputStream().bufferedReader()
            val writer = socket.getOutputStream().bufferedWriter()

            // Parse only the request line — drain the rest of the headers
            val requestLine = reader.readLine() ?: return
            while (reader.readLine()?.isNotEmpty() == true) { /* drain HTTP headers */ }

            val matchId = extractMatchId(requestLine)

            // ---- HTTP response headers ----
            writer.write("HTTP/1.1 200 OK\r\n")
            writer.write("Content-Type: text/event-stream; charset=utf-8\r\n")
            writer.write("Cache-Control: no-cache\r\n")
            writer.write("Connection: keep-alive\r\n")
            writer.write("Access-Control-Allow-Origin: *\r\n")
            writer.write("\r\n")
            writer.flush()

            // ---- Stream events one by one ----
            // delay() is cancellation-aware — if the coroutine is cancelled (client dismissed),
            // delay() throws CancellationException which bubbles up to the catch block.
            val events = commentaryForMatch(matchId)
            for ((index, event) in events.withIndex()) {
                val pauseMs = if (index == 0) INITIAL_DELAY_MS else BETWEEN_EVENTS_MS
                delay(pauseMs) // cooperative cancellation point

                if (socket.isClosed) return
                writer.write("data: ${gson.toJson(event)}\n\n")
                writer.flush()
            }

            // All events sent — keep connection alive until the client closes the socket
            // (e.g. user dismisses the bottom sheet → OkHttp closes the connection).
            while (!socket.isClosed) {
                delay(1_000) // cooperative cancellation point
            }
        } catch (_: Exception) {
            // Client disconnected mid-stream or coroutine was cancelled — both are normal lifecycle
        } finally {
            socket.runCatching { close() }
        }
    }


    // ------------------------------------------------------------------ //
    //  Helpers                                                             //
    // ------------------------------------------------------------------ //

    /**
     * Extracts the matchId from an HTTP request line.
     * e.g. `"GET /matches/match_001/commentary HTTP/1.1"` → `"match_001"`
     */
    private fun extractMatchId(requestLine: String): String = try {
        requestLine.split(" ")
            .getOrElse(1) { "/" }
            .split("/")
            .getOrElse(2) { "" }
            .substringBefore("?")
    } catch (_: Exception) { "" }

    // ------------------------------------------------------------------ //
    //  Commentary fixture data                                             //
    // ------------------------------------------------------------------ //

    private fun commentaryForMatch(matchId: String): List<CommentaryEventDto> = when (matchId) {
        "match_001" -> listOf(
            CommentaryEventDto(28, "Arsenal building pressure through midfield", "GENERAL"),
            CommentaryEventDto(31, "Saka's cross is just out of reach of Havertz", "GENERAL"),
            CommentaryEventDto(33, "GOAL! Martinelli slots it home! Arsenal 1-0 Chelsea", "GOAL"),
            CommentaryEventDto(35, "Yellow card shown to Colwill after a late foul", "CARD"),
            CommentaryEventDto(37, "Substitution for Arsenal: White off, Timber on", "SUBSTITUTION"),
            CommentaryEventDto(40, "Chelsea looking for a quick equaliser", "GENERAL")
        )
        "match_002" -> listOf(
            CommentaryEventDto(60, "Barcelona dominating possession in the final third", "GENERAL"),
            CommentaryEventDto(62, "GOAL! Lewandowski breaks the deadlock! Barça 2-1", "GOAL"),
            CommentaryEventDto(64, "Red card! Rüdiger receives his second yellow", "CARD"),
            CommentaryEventDto(66, "Substitution: Yamal on for Raphinha", "SUBSTITUTION"),
            CommentaryEventDto(68, "Real Madrid down to 10 men — enormous advantage for Barça", "GENERAL")
        )
        "match_003" -> listOf(
            CommentaryEventDto(8,  "Liverpool pressing high from the first whistle", "GENERAL"),
            CommentaryEventDto(10, "Salah's shot rattles the post — so close!", "GENERAL"),
            CommentaryEventDto(12, "Ederson makes a brilliant save to keep it level", "GENERAL"),
            CommentaryEventDto(14, "Haaland muscled off the ball by van Dijk", "GENERAL")
        )
        "match_004" -> listOf(
            CommentaryEventDto(48, "Both sides look for the winner after a gripping first half", "GENERAL"),
            CommentaryEventDto(51, "Mbappé surges forward — great run but the flag is up", "GENERAL"),
            CommentaryEventDto(54, "GOAL! Kane rises to head Bayern level — 1-1!", "GOAL"),
            CommentaryEventDto(56, "Penalty! Kimmich trips Dembélé inside the box", "GENERAL"),
            CommentaryEventDto(57, "GOAL! Mbappé converts from the spot — PSG take the lead! 2-1", "GOAL")
        )
        "match_005" -> listOf(
            CommentaryEventDto(70, "Juventus controlling this match, AC Milan struggling to create", "GENERAL"),
            CommentaryEventDto(73, "GOAL! Vlahović's hat-trick! Juve 3-0 Milan", "GOAL"),
            CommentaryEventDto(75, "Substitution: Theo Hernández off, Okafor on for AC Milan", "SUBSTITUTION"),
            CommentaryEventDto(78, "Juventus see out possession as Milan search for a consolation", "GENERAL")
        )
        "match_006" -> listOf(
            CommentaryEventDto(38, "Breathless match! Already four goals in the first half", "GENERAL"),
            CommentaryEventDto(40, "GOAL! Füllkrug makes it 2-2 for Dortmund — penalty!", "GOAL"),
            CommentaryEventDto(43, "Yellow card for Schlotterbeck after time-wasting", "CARD"),
            CommentaryEventDto(45, "Two minutes of added time signalled", "GENERAL")
        )
        else -> listOf(
            CommentaryEventDto(1,  "Match underway!", "GENERAL"),
            CommentaryEventDto(15, "End-to-end action in the opening exchanges", "GENERAL"),
            CommentaryEventDto(30, "Half time approaching — level so far", "GENERAL")
        )
    }
}
