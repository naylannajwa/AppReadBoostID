// File: data/model/UserProgress.kt
package com.readboost.id.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_progress")
data class UserProgress(
    @PrimaryKey
    val id: Int = 1, // Selalu 1 karena hanya 1 user lokal
    val totalXP: Int = 0,
    val streakDays: Int = 0,
    val dailyTarget: Int = 2, // 2, 5, atau 10 menit
    val lastReadDate: Long = 0L,
    val dailyXPEarned: Int = 0, // XP yang sudah didapat hari ini
    val dailyReadingMinutes: Int = 0 // Menit membaca yang sudah dilakukan hari ini
)