package com.massa.livecenter.data.remote.sse

import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import okhttp3.OkHttpClient
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommentarySseClient @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val gson: Gson
) {
    companion object {
        private const val BASE_URL = "https://api.superbet.dev/v1/matches/%s/commentary"
    }

    /**
     * Returns a [Flow] that emits [CommentaryEventDto] items streamed via Server-Sent Events
     * for the given [matchId].
     *
     * The collector is responsible for cancelling via coroutine cancellation — the implementation
     * must cancel the underlying OkHttp [Call] in the flow's [kotlinx.coroutines.flow.callbackFlow]
     * or [kotlinx.coroutines.flow.flow] `finally` block.
     */
    fun observeCommentary(matchId: String): Flow<CommentaryEventDto> {
        // TODO: Build an OkHttp Request for the URL with matchId substituted.
        //   Use callbackFlow { } or flow { } with an OkHttp streaming call:
        //   1. Execute the request synchronously inside the flow block.
        //   2. Read the ResponseBody line by line (bufferedSource or bufferedReader).
        //   3. For each line starting with "data:", strip the prefix, parse JSON into
        //      CommentaryEventDto using gson, and emit it.
        //   4. In the finally block, cancel the OkHttp Call to stop streaming when the
        //      collector is cancelled.
        TODO("Implement SSE streaming logic")
    }
}
