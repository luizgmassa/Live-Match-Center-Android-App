package com.massa.livecenter.data.remote.rest

import com.google.gson.annotations.SerializedName

data class MatchDto(
    @SerializedName("id") val id: String,
    @SerializedName("homeTeam") val homeTeam: String,
    @SerializedName("awayTeam") val awayTeam: String,
    @SerializedName("score") val score: String,
    @SerializedName("minute") val minute: Int
)

data class MatchPageDto(
    @SerializedName("matches") val matches: List<MatchDto>,
    @SerializedName("nextPage") val nextPage: Int?,
    @SerializedName("totalPages") val totalPages: Int
)
