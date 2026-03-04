package com.massa.livecenter.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "odds")
data class OddsEntity(
    @PrimaryKey val matchId: String,
    val home: Double,
    val draw: Double,
    val away: Double
)
