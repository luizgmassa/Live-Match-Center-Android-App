package com.massa.livecenter.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey val id: String,
    val homeTeam: String,
    val awayTeam: String,
    val score: String,
    val minute: Int
)
