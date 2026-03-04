package com.massa.livecenter.data.remote.websocket

import com.google.gson.annotations.SerializedName

data class OddsUpdateDto(
    @SerializedName("matchId") val matchId: String,
    @SerializedName("odds") val odds: OddsDto
)

data class OddsDto(
    @SerializedName("home") val home: Double,
    @SerializedName("draw") val draw: Double,
    @SerializedName("away") val away: Double
)
