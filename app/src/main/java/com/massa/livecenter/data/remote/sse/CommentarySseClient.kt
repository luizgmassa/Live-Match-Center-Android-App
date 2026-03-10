package com.massa.livecenter.data.remote.sse

import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.SocketException
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * OkHttp SSE client that streams live match commentary.
 *
 * The base URL is injected via [@Named("sseBaseUrl")] so it works with both the
 * mock server during development and the real production endpoint.
 *
 * The SSE format expected from the server:
 * ```
 * data: {"minute":34,"text":"GOAL!","type":"GOAL"}
 *
 * data: {"minute":36,"text":"Yellow card","type":"CARD"}
 * ```
 */
@Singleton
class CommentarySseClient @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val gson: Gson,
    @Named("sseBaseUrl") private val sseBaseUrl: String
) {
    /**
     * Returns a cold [Flow] that emits [CommentaryEventDto] items streamed via SSE
     * for the given [matchId].
     *
     * The Flow:
     * - Executes the HTTP request on [Dispatchers.IO]
     * - Reads response lines until the server closes the connection (or the flow is cancelled)
     * - Cancels the OkHttp call in the `finally` block for cooperative cancellation
     */
    fun observeCommentary(matchId: String): Flow<CommentaryEventDto> = callbackFlow {
        val url = "${sseBaseUrl}matches/$matchId/commentary"
        val request = Request.Builder()
            .url(url)
            .addHeader("Accept", "text/event-stream")
            .build()

        val call = okHttpClient.newCall(request)
        try {
            val response = call.execute()
            if (!response.isSuccessful) {
                error("SSE request failed: HTTP ${response.code}")
            }

            val reader = response.body.byteStream().bufferedReader()

            reader.use { br ->
                br.forEachLine { line ->
                    if (line.startsWith("data:")) {
                        val json = line.removePrefix("data:").trim()
                        if (json.isNotEmpty()) {
                            runCatching {
                                gson.fromJson(json, CommentaryEventDto::class.java)
                            }.onSuccess {
                                trySend(it)
                            }
                            // Skip malformed events silently
                        }
                    }
                }
            }

            awaitClose {
                reader.close()
                response.body.close()
            }
        } catch(e: SocketException) {
            // Does nothing
        } finally {
            // Cancel the in-flight OkHttp call when the coroutine is cancelled.
            call.cancel()
        }
    }.flowOn(Dispatchers.IO)
}
