package com.massa.livecenter.data.mock

import com.google.gson.Gson
import com.massa.livecenter.data.remote.rest.MatchDto
import com.massa.livecenter.data.remote.rest.MatchPageDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A local mock server that simulates the `GET /matches/live` REST endpoint.
 *
 * Match IDs align with [MockOddsWebSocketServer.MATCH_IDS] so WebSocket odds updates
 * land on the correct cards.
 *
 * SSE commentary is handled separately by [MockCommentarySseServer].
 *
 * Pagination: 6 fixture matches split 4 + 2 across two pages to exercise RemoteMediator APPEND.
 */
@Singleton
class MockMatchRestServer @Inject constructor(private val gson: Gson) {

    private val mockWebServer = MockWebServer()

    init {
        mockWebServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                val path = request.path ?: return MockResponse().setResponseCode(404)
                return when {
                    path.startsWith("/matches/live") -> handleMatchesLive(request)
                    else                             -> MockResponse().setResponseCode(404)
                }
            }
        }
    }

    fun start() {
        runBlocking(Dispatchers.IO) { mockWebServer.start() }
    }

    fun baseUrl(): String = mockWebServer.url("/").toString()

    fun shutdown() { mockWebServer.shutdown() }

    // ------------------------------------------------------------------ //
    //  /matches/live                                                       //
    // ------------------------------------------------------------------ //

    private fun handleMatchesLive(request: RecordedRequest): MockResponse {
        val requestUrl = request.requestUrl
        val page = requestUrl?.queryParameter("page")?.toIntOrNull() ?: 1
        val size = requestUrl?.queryParameter("size")?.toIntOrNull() ?: 20

        val allMatches = fixtureMatches()
        val fromIndex  = (page - 1) * size
        val toIndex    = minOf(fromIndex + size, allMatches.size)

        val pageMatches = if (fromIndex < allMatches.size) allMatches.subList(fromIndex, toIndex)
                          else emptyList()
        val nextPage   = if (toIndex < allMatches.size) page + 1 else null
        val totalPages = maxOf(1, (allMatches.size + size - 1) / size)

        val body = gson.toJson(MatchPageDto(pageMatches, nextPage, totalPages))
        return MockResponse()
            .setResponseCode(200)
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .setBody(body)
    }

    // ------------------------------------------------------------------ //
    //  Fixture data                                                        //
    // ------------------------------------------------------------------ //

    private data class MatchMeta(val homeTeam: String, val awayTeam: String,
                                  val score: String, val minute: Int)

    private val FIXTURE_META = listOf(
        MatchMeta("Arsenal",   "Chelsea",      "1:0", 34),
        MatchMeta("Barcelona", "Real Madrid",  "2:1", 67),
        MatchMeta("Liverpool", "Man City",     "0:0", 12),
        MatchMeta("PSG",       "Bayern",       "1:1", 55),
        MatchMeta("Juventus",  "AC Milan",     "3:0", 78),
        MatchMeta("Dortmund",  "RB Leipzig",   "2:2", 45)
    )

    private fun fixtureMatches(): List<MatchDto> =
        MockOddsWebSocketServer.MATCH_IDS.zip(FIXTURE_META).map { (id, meta) ->
            MatchDto(id, meta.homeTeam, meta.awayTeam, meta.score, meta.minute)
        }
}
