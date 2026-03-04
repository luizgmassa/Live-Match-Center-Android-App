package com.massa.livecenter.data.remote.sse

import com.google.gson.annotations.SerializedName

data class CommentaryEventDto(
    @SerializedName("minute") val minute: Int,
    @SerializedName("text") val text: String,
    @SerializedName("type") val type: String
)
