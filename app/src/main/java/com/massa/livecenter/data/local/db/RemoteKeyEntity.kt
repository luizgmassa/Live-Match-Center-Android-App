package com.massa.livecenter.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "remote_keys")
data class RemoteKeyEntity(
    @PrimaryKey val matchId: String,
    val prevPage: Int?,
    val nextPage: Int?
)
