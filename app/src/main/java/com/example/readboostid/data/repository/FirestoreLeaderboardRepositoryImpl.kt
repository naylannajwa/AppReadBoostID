// File: data/repository/FirestoreLeaderboardRepositoryImpl.kt
package com.readboost.id.data.repository

import com.readboost.id.data.model.Leaderboard
import com.readboost.id.data.model.UserProgress
import com.readboost.id.data.service.FirestoreLeaderboardService
import com.readboost.id.domain.repository.FirestoreLeaderboardRepository
import kotlinx.coroutines.flow.Flow

class FirestoreLeaderboardRepositoryImpl(
    private val firestoreService: FirestoreLeaderboardService
) : FirestoreLeaderboardRepository {

    override suspend fun getLeaderboard(limit: Int): List<Leaderboard> {
        return firestoreService.getLeaderboard(limit)
    }

    override suspend fun getAllTimeLeaderboard(limit: Int): List<Leaderboard> {
        return firestoreService.getAllTimeLeaderboard(limit)
    }

    override suspend fun getWeeklyLeaderboard(limit: Int): List<Leaderboard> {
        return firestoreService.getWeeklyLeaderboard(limit)
    }

    override fun getLeaderboardFlow(limit: Int): Flow<List<Leaderboard>> {
        return firestoreService.getLeaderboardFlow(limit)
    }

    override suspend fun updateUserXP(userId: String, username: String, xpToAdd: Int) {
        firestoreService.updateUserXP(userId, username, xpToAdd)
    }

    override suspend fun getUserProgress(userId: String): UserProgress? {
        return firestoreService.getUserProgress(userId)
    }

    override fun getUserProgressFlow(userId: String): Flow<UserProgress?> {
        return firestoreService.getUserProgressFlow(userId)
    }

    override suspend fun saveUserProgress(userId: String, userProgress: UserProgress) {
        firestoreService.saveUserProgress(userId, userProgress)
    }

    override suspend fun updateUserProgress(userId: String, updates: Map<String, Any>) {
        firestoreService.updateUserProgress(userId, updates)
    }

    override suspend fun startReadingSession(userId: String, articleId: Int): String {
        val sessionId = "${userId}_${articleId}_${System.currentTimeMillis()}"
        firestoreService.startReadingSession(userId, articleId)
        return sessionId
    }

    override suspend fun updateReadingSession(sessionId: String, activeTimeIncrement: Long) {
        firestoreService.updateReadingSession(sessionId, activeTimeIncrement)
    }

    override suspend fun endReadingSession(sessionId: String, totalXP: Int) {
        firestoreService.endReadingSession(sessionId, totalXP)
    }
}
