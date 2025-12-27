// File: data/service/FirestoreLeaderboardService.kt
package com.readboost.id.data.service

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.readboost.id.data.model.Leaderboard
import com.readboost.id.data.model.UserProgress
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class FirestoreLeaderboardService(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    companion object {
        private const val LEADERBOARD_COLLECTION = "leaderboard"
        private const val USER_PROGRESS_COLLECTION = "user_progress"
        private const val READING_SESSIONS_COLLECTION = "reading_sessions"
    }

    // Leaderboard Operations
    suspend fun getLeaderboard(limit: Int = 50): List<Leaderboard> {
        return getAllTimeLeaderboard(limit)
    }

    suspend fun getAllTimeLeaderboard(limit: Int = 50): List<Leaderboard> {
        return try {
            println("FirestoreLeaderboardService: Getting all-time leaderboard")
            val snapshot = firestore.collection(LEADERBOARD_COLLECTION)
                .orderBy("totalXP", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            val leaderboard = snapshot.documents.mapIndexed { index, document ->
                val userId = document.getString("userId") ?: "unknown"
                val username = document.getString("username") ?: "Unknown"
                val totalXP = document.getLong("totalXP")?.toInt() ?: 0
                val rank = index + 1

                println("FirestoreLeaderboardService: All-time - $username: $totalXP XP (Rank: $rank)")
                Leaderboard(
                    userId = userId.hashCode(),
                    username = username,
                    totalXP = totalXP,
                    rank = rank
                )
            }
            leaderboard
        } catch (e: Exception) {
            println("FirestoreLeaderboardService: Error getting all-time leaderboard: ${e.message}")
            emptyList()
        }
    }

    suspend fun getWeeklyLeaderboard(limit: Int = 50): List<Leaderboard> {
        return try {
            println("FirestoreLeaderboardService: Getting weekly leaderboard")
            // For now, return same as all-time since we don't have weekly data structure yet
            // In production, you would filter by last week's data
            val allTimeData = getAllTimeLeaderboard(limit)

            // Simulate weekly by taking top users (in real app, filter by timestamp)
            allTimeData.take(limit).mapIndexed { index, entry ->
                entry.copy(rank = index + 1)
            }
        } catch (e: Exception) {
            println("FirestoreLeaderboardService: Error getting weekly leaderboard: ${e.message}")
            emptyList()
        }
    }

    suspend fun updateUserXP(userId: String, username: String, xpToAdd: Int) {
        try {
            val userDocRef = firestore.collection(LEADERBOARD_COLLECTION).document(userId)

            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(userDocRef)
                val currentXP = snapshot.getLong("totalXP")?.toInt() ?: 0
                val newXP = currentXP + xpToAdd

                transaction.set(userDocRef, mapOf(
                    "userId" to userId,
                    "username" to username,
                    "totalXP" to newXP,
                    "lastUpdated" to System.currentTimeMillis()
                ))
            }.await()
        } catch (e: Exception) {
            // Handle error
        }
    }

    // User Progress Operations
    suspend fun getUserProgress(userId: String): UserProgress? {
        return try {
            val document = firestore.collection(USER_PROGRESS_COLLECTION)
                .document(userId)
                .get()
                .await()

            if (document.exists()) {
                UserProgress(
                    id = 1, // Keep for compatibility
                    totalXP = document.getLong("totalXP")?.toInt() ?: 0,
                    streakDays = document.getLong("streakDays")?.toInt() ?: 0,
                    dailyTarget = document.getLong("dailyTarget")?.toInt() ?: 5,
                    lastReadDate = document.getLong("lastReadDate") ?: 0L,
                    dailyXPEarned = document.getLong("dailyXPEarned")?.toInt() ?: 0,
                    dailyReadingMinutes = document.getLong("dailyReadingMinutes")?.toInt() ?: 0
                )
            } else {
                // Create default user progress if not exists
                val defaultProgress = UserProgress()
                saveUserProgress(userId, defaultProgress)
                defaultProgress
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun saveUserProgress(userId: String, userProgress: UserProgress) {
        try {
            val progressMap = mapOf(
                "userId" to userId,
                "totalXP" to userProgress.totalXP,
                "streakDays" to userProgress.streakDays,
                "dailyTarget" to userProgress.dailyTarget,
                "lastReadDate" to userProgress.lastReadDate,
                "dailyXPEarned" to userProgress.dailyXPEarned,
                "dailyReadingMinutes" to userProgress.dailyReadingMinutes,
                "lastUpdated" to System.currentTimeMillis()
            )

            firestore.collection(USER_PROGRESS_COLLECTION)
                .document(userId)
                .set(progressMap)
                .await()
        } catch (e: Exception) {
            // Handle error
        }
    }

    suspend fun updateUserProgress(userId: String, updates: Map<String, Any>) {
        try {
            firestore.collection(USER_PROGRESS_COLLECTION)
                .document(userId)
                .update(updates)
                .await()
        } catch (e: Exception) {
            // Handle error
        }
    }

    // Reading Session Tracking
    suspend fun startReadingSession(userId: String, articleId: Int) {
        try {
            val sessionId = "${userId}_${articleId}_${System.currentTimeMillis()}"
            val sessionData = mapOf(
                "userId" to userId,
                "articleId" to articleId,
                "startTime" to System.currentTimeMillis(),
                "lastActiveTime" to System.currentTimeMillis(),
                "totalActiveTime" to 0L,
                "isActive" to true
            )

            firestore.collection(READING_SESSIONS_COLLECTION)
                .document(sessionId)
                .set(sessionData)
                .await()
        } catch (e: Exception) {
            // Handle error
        }
    }

    suspend fun updateReadingSession(sessionId: String, activeTimeIncrement: Long) {
        try {
            val sessionRef = firestore.collection(READING_SESSIONS_COLLECTION).document(sessionId)

            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(sessionRef)
                val currentActiveTime = snapshot.getLong("totalActiveTime") ?: 0L
                val newActiveTime = currentActiveTime + activeTimeIncrement

                transaction.update(sessionRef, mapOf(
                    "totalActiveTime" to newActiveTime,
                    "lastActiveTime" to System.currentTimeMillis()
                ))
            }.await()
        } catch (e: Exception) {
            // Handle error
        }
    }

    suspend fun endReadingSession(sessionId: String, totalXP: Int = 0) {
        try {
            firestore.collection(READING_SESSIONS_COLLECTION)
                .document(sessionId)
                .update(mapOf(
                    "isActive" to false,
                    "endTime" to System.currentTimeMillis(),
                    "xpEarned" to totalXP
                ))
                .await()
        } catch (e: Exception) {
            // Handle error
        }
    }

    // Flow untuk real-time updates
    fun getLeaderboardFlow(limit: Int = 50): Flow<List<Leaderboard>> = flow {
        try {
            println("FirestoreLeaderboardService: Querying leaderboard collection...")
            val snapshot = firestore.collection(LEADERBOARD_COLLECTION)
                .orderBy("totalXP", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            println("FirestoreLeaderboardService: Found ${snapshot.size()} documents in leaderboard collection")

            val leaderboard = snapshot.documents.mapIndexed { index, document ->
                val userId = document.getString("userId") ?: "unknown"
                val username = document.getString("username") ?: "Unknown"
                val totalXP = document.getLong("totalXP")?.toInt() ?: 0
                val rank = index + 1

                println("FirestoreLeaderboardService: Document ${document.id} -> $username: $totalXP XP (Rank: $rank)")

                Leaderboard(
                    userId = userId.hashCode(),
                    username = username,
                    totalXP = totalXP,
                    rank = rank
                )
            }

            println("FirestoreLeaderboardService: Emitting ${leaderboard.size} leaderboard entries")
            emit(leaderboard)
        } catch (e: Exception) {
            println("FirestoreLeaderboardService: Error getting leaderboard: ${e.message}")
            e.printStackTrace()
            emit(emptyList())
        }
    }

    fun getUserProgressFlow(userId: String): Flow<UserProgress?> = flow {
        try {
            val document = firestore.collection(USER_PROGRESS_COLLECTION)
                .document(userId)
                .get()
                .await()

            val progress = if (document.exists()) {
                UserProgress(
                    id = 1,
                    totalXP = document.getLong("totalXP")?.toInt() ?: 0,
                    streakDays = document.getLong("streakDays")?.toInt() ?: 0,
                    dailyTarget = document.getLong("dailyTarget")?.toInt() ?: 5,
                    lastReadDate = document.getLong("lastReadDate") ?: 0L,
                    dailyXPEarned = document.getLong("dailyXPEarned")?.toInt() ?: 0
                )
            } else {
                UserProgress()
            }
            emit(progress)
        } catch (e: Exception) {
            emit(null)
        }
    }
}