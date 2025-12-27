// File: data/model/FirestoreModels.kt
package com.readboost.id.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

// Model untuk Firestore User Progress
data class FirestoreUserProgress(
    @DocumentId
    val userId: String = "",
    val email: String = "",
    val displayName: String = "",
    val totalXP: Int = 0,
    val streakDays: Int = 0,
    val dailyTarget: Int = 5, // dalam menit
    val dailyXPEarned: Int = 0, // XP yang didapat hari ini
    val dailyReadingMinutes: Int = 0, // Menit membaca yang sudah dilakukan hari ini
    val articlesRead: Int = 0,
    val lastReadDate: Long = 0L,
    val currentStreakStartDate: Long = 0L,
    @ServerTimestamp
    val lastUpdated: Date? = null
)

// Model untuk Leaderboard Entry (untuk tampilan)
data class LeaderboardEntry(
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val totalXP: Int = 0,
    val streakDays: Int = 0,
    val dailyXPEarned: Int = 0, // XP yang didapat hari ini
    val dailyReadingMinutes: Int = 0, // Menit membaca yang sudah dilakukan hari ini
    val rank: Int = 0,
    val avatarUrl: String? = null
)

// Model untuk reading session tracking (Firestore)
data class FirestoreReadingSession(
    @DocumentId
    val sessionId: String = "",
    val userId: String = "",
    val articleId: Int = 0,
    val startTime: Long = 0L,
    val endTime: Long = 0L,
    val totalScrollDistance: Float = 0f,
    val lastScrollPosition: Float = 0f,
    val xpEarned: Int = 0,
    @ServerTimestamp
    val createdAt: Date? = null
)
