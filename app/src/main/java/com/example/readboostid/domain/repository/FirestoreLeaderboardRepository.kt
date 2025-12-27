// File: domain/repository/FirestoreLeaderboardRepository.kt
package com.readboost.id.domain.repository

import com.readboost.id.data.model.Leaderboard
import com.readboost.id.data.model.UserProgress
import kotlinx.coroutines.flow.Flow

interface FirestoreLeaderboardRepository {
    // Leaderboard operations
    suspend fun getLeaderboard(limit: Int): List<Leaderboard>
    suspend fun getAllTimeLeaderboard(limit: Int): List<Leaderboard>
    suspend fun getWeeklyLeaderboard(limit: Int): List<Leaderboard>
    fun getLeaderboardFlow(limit: Int): Flow<List<Leaderboard>>
    suspend fun updateUserXP(userId: String, username: String, xpToAdd: Int)

    // User progress operations
    suspend fun getUserProgress(userId: String): UserProgress?
    fun getUserProgressFlow(userId: String): Flow<UserProgress?>
    suspend fun saveUserProgress(userId: String, userProgress: UserProgress)
    suspend fun updateUserProgress(userId: String, updates: Map<String, Any>)

    // Reading session operations
    suspend fun startReadingSession(userId: String, articleId: Int): String
    suspend fun updateReadingSession(sessionId: String, activeTimeIncrement: Long)
    suspend fun endReadingSession(sessionId: String, totalXP: Int)
}