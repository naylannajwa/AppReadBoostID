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
    val totalReadingTime: Int = 0 // total waktu membaca dalam detik
)