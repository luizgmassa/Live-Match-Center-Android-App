package com.massa.livecenter.domain.model

data class Match(
    val id: String,
    val homeTeam: String,
    val awayTeam: String,
    val score: String,
    val minute: Int
)

data class Odds(
    val matchId: String,
    val home: Double,
    val draw: Double,
    val away: Double
)

data class Commentary(
    val minute: Int,
    val text: String,
    val type: CommentaryType
)

enum class CommentaryType {
    GOAL, CARD, SUBSTITUTION, GENERAL
}
