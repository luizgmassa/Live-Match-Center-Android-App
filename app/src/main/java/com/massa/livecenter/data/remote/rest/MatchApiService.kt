package com.massa.livecenter.data.remote.rest

import retrofit2.http.GET
import retrofit2.http.Query

interface MatchApiService {

    @GET("matches/live")
    suspend fun getLiveMatches(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): MatchPageDto
}
