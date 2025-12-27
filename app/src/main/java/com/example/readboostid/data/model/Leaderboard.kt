// File: data/model/Leaderboard.kt
package com.readboost.id.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "leaderboard")
data class Leaderboard(
    @PrimaryKey(autoGenerate = true)
    val userId: Int = 0,
    val username: String,
    val totalXP: Int,
    val rank: Int = 0
)

