// File: data/service/HybridDataManager.kt
package com.readboost.id.data.service

import com.google.firebase.auth.FirebaseAuth
import com.readboost.id.data.model.UserProgress
import com.readboost.id.domain.repository.FirestoreLeaderboardRepository
import com.readboost.id.domain.repository.UserDataRepository
import kotlinx.coroutines.flow.firstOrNull

/**
 * Hybrid Data Manager - Mengelola integrasi antara Room (artikel) dan Firestore (leaderboard/progress)
 */
class HybridDataManager(
    private val userDataRepository: UserDataRepository,
    private val firestoreRepository: FirestoreLeaderboardRepository
) {

    companion object {
        // Get current user ID dynamically based on authentication type
        fun getCurrentUserId(): String {
            val firebaseUser = FirebaseAuth.getInstance().currentUser
            return if (firebaseUser != null) {
                // Firebase authenticated user - use Firebase UID
                firebaseUser.uid
            } else {
                // Local/Admin user - use a fixed ID for local users
                // Note: In a real app, you'd want to support multiple local users
                "local_user_default"
            }
        }

        fun getCurrentUsername(): String {
            val firebaseUser = FirebaseAuth.getInstance().currentUser
            return if (firebaseUser != null) {
                // Firebase authenticated user - use display name or email
                firebaseUser.displayName ?: firebaseUser.email ?: "Firebase User"
            } else {
                // Local/Admin user - use a default name
                "Local User"
            }
        }

        // Helper function to determine if current user is Firebase authenticated
        fun isFirebaseUser(): Boolean {
            return FirebaseAuth.getInstance().currentUser != null
        }
    }

    /**
     * Sinkronisasi progress user dari Firestore ke Room (untuk backup)
     */
    suspend fun syncUserProgressFromFirestore(): UserProgress? {
        return try {
            val userId = getCurrentUserId()
            val firestoreProgress = firestoreRepository.getUserProgress(userId)

            if (firestoreProgress != null) {
                // Update Room database dengan data dari Firestore
                userDataRepository.updateUserProgress(firestoreProgress)
                firestoreProgress
            } else {
                // Jika tidak ada di Firestore, ambil dari Room
                userDataRepository.getUserProgress().firstOrNull()
            }
        } catch (e: Exception) {
            // Fallback ke Room database
            userDataRepository.getUserProgress().firstOrNull()
        }
    }

    /**
     * Sinkronisasi progress user dari Room ke Firestore
     */
    suspend fun syncUserProgressToFirestore() {
        try {
            val roomProgress = userDataRepository.getUserProgressOnce()

            if (roomProgress != null) {
                val userId = getCurrentUserId()
                val username = getCurrentUsername()
                firestoreRepository.saveUserProgress(userId, roomProgress)
                firestoreRepository.updateUserXP(userId, username, roomProgress.totalXP)
            }
        } catch (e: Exception) {
            // Handle error silently
        }
    }

    /**
     * Update XP dan progress saat user menyelesaikan artikel
     */
    suspend fun onArticleCompleted(articleId: Int, xpEarned: Int, readingTimeSeconds: Int) {
        println("HybridDataManager: onArticleCompleted called - articleId: $articleId, xpEarned: $xpEarned, readingTime: ${readingTimeSeconds}s")
        try {
            val userId = getCurrentUserId()
            val username = getCurrentUsername()
            val isFirebaseUser = isFirebaseUser()
            println("HybridDataManager: User type - Firebase: $isFirebaseUser, ID: $userId, Name: $username")

            // Get current progress
            val currentProgress = getUserProgress()
            if (currentProgress == null) {
                println("HybridDataManager: No current progress found, creating default")
                return
            }

            // Calculate new progress
            val today = System.currentTimeMillis()
            val lastReadDate = currentProgress.lastReadDate
            val lastStreakDate = currentProgress.lastStreakDate

            // Get current date (start of day in milliseconds)
            val todayStart = today / (1000 * 60 * 60 * 24) * (1000 * 60 * 60 * 24)
            val lastStreakDayStart = lastStreakDate / (1000 * 60 * 60 * 24) * (1000 * 60 * 60 * 24)
            val lastReadDayStart = lastReadDate / (1000 * 60 * 60 * 24) * (1000 * 60 * 60 * 24)

            // Check if streak should increase (only once per day)
            val shouldIncreaseStreak = todayStart > lastStreakDayStart

            // Calculate streak reset (if more than 1 day gap from last read)
            val daysDifference = ((today - lastReadDate) / (1000 * 60 * 60 * 24)).toInt()
            val shouldResetStreak = daysDifference > 1

            val newStreak = when {
                shouldResetStreak -> 1 // Reset streak if gap > 1 day
                shouldIncreaseStreak -> currentProgress.streakDays + 1 // Increase streak if new day
                else -> currentProgress.streakDays // Keep current streak
            }

            val newLastStreakDate = if (shouldIncreaseStreak) todayStart else lastStreakDate

            // Check if it's a new day (reset daily XP and minutes if so)
            val isNewDay = daysDifference >= 1
            val newDailyXPEarned = if (isNewDay) xpEarned else (currentProgress.dailyXPEarned + xpEarned)
            val readingTimeInSeconds = readingTimeSeconds.toInt() / 1000 // Convert milliseconds to seconds
            val readingTimeInMinutes = readingTimeInSeconds / 60 // Convert seconds to minutes
            val newDailyReadingMinutes = if (isNewDay) readingTimeInMinutes else (currentProgress.dailyReadingMinutes + readingTimeInMinutes)

            val updatedProgress = currentProgress.copy(
                totalXP = currentProgress.totalXP + xpEarned,
                dailyXPEarned = newDailyXPEarned,
                dailyReadingMinutes = newDailyReadingMinutes,
                streakDays = newStreak,
                lastStreakDate = newLastStreakDate,
                lastReadDate = today
            )

            println("HybridDataManager: Updated progress - Total XP: ${updatedProgress.totalXP}, Daily XP: ${updatedProgress.dailyXPEarned}, Daily Minutes: ${updatedProgress.dailyReadingMinutes}/${updatedProgress.dailyTarget}, Streak: ${updatedProgress.streakDays}")

            // Update Firestore for ALL users (both Firebase and Local) to enable leaderboard
            println("HybridDataManager: Updating user data in Firestore (for leaderboard)")
            try {
                firestoreRepository.updateUserXP(userId, username, xpEarned)
                firestoreRepository.saveUserProgress(userId, updatedProgress)

                // Also update reading session for all users
                try {
                    val sessionId = firestoreRepository.startReadingSession(userId, articleId)
                    firestoreRepository.endReadingSession(sessionId, xpEarned)
                    println("HybridDataManager: Reading session completed - sessionId: $sessionId")
                } catch (e: Exception) {
                    println("HybridDataManager: Failed to save reading session: ${e.message}")
                }
            } catch (e: Exception) {
                println("HybridDataManager: Failed to update Firestore for leaderboard: ${e.message}")
            }

            // Update Room database (for local offline access)
            println("HybridDataManager: Updating local Room database")
            userDataRepository.completeReadingSession(xpEarned)
            userDataRepository.updateUserProgress(updatedProgress)

        } catch (e: Exception) {
            println("HybridDataManager: Error in onArticleCompleted: ${e.message}")
            e.printStackTrace()
            // Fallback: at least update Room
            try {
                userDataRepository.completeReadingSession(xpEarned)
            } catch (e2: Exception) {
                println("HybridDataManager: Even Room fallback failed: ${e2.message}")
            }
        }
    }

    /**
     * Update target harian
     */
    suspend fun updateDailyTarget(newTarget: Int) {
        try {
            val userId = getCurrentUserId()
            println("HybridDataManager: Updating daily target for $userId to $newTarget")

            // Update Firestore for all users (for leaderboard consistency)
            try {
                firestoreRepository.updateUserProgress(userId, mapOf("dailyTarget" to newTarget))
                println("HybridDataManager: Updated target in Firestore")
            } catch (e: Exception) {
                println("HybridDataManager: Failed to update Firestore target: ${e.message}")
            }

            // Also update Room for offline access
            userDataRepository.updateDailyTarget(newTarget)
            println("HybridDataManager: Updated target in Room")
        } catch (e: Exception) {
            println("HybridDataManager: Error updating daily target: ${e.message}")
            // Fallback ke Room
            try {
                userDataRepository.updateDailyTarget(newTarget)
                println("HybridDataManager: Fallback update in Room only")
            } catch (e2: Exception) {
                println("HybridDataManager: Even Room fallback failed: ${e2.message}")
            }
        }
    }

    /**
     * Get user progress dengan fallback hybrid
     */
    suspend fun getUserProgress(): UserProgress? {
        return try {
            val userId = getCurrentUserId()
            val isFirebaseUser = isFirebaseUser()
            println("HybridDataManager: Getting user progress for $userId (Firebase: $isFirebaseUser)")

            // Try to get from Firestore first (for all users now)
            val firestoreProgress = try {
                firestoreRepository.getUserProgress(userId)
            } catch (e: Exception) {
                println("HybridDataManager: ERROR - Firestore access failed: ${e.message}")
                e.printStackTrace()
                null
            }

                if (firestoreProgress != null) {
                    println("HybridDataManager: SUCCESS - Using Firestore data: XP: ${firestoreProgress.totalXP}, Daily XP: ${firestoreProgress.dailyXPEarned}, Daily Minutes: ${firestoreProgress.dailyReadingMinutes}/${firestoreProgress.dailyTarget}, Streak: ${firestoreProgress.streakDays}")
                    // Sync to Room for offline access
                    try {
                        userDataRepository.updateUserProgress(firestoreProgress)
                        println("HybridDataManager: Synced Firestore to Room successfully")
                    } catch (e: Exception) {
                        println("HybridDataManager: Failed to sync to Room: ${e.message}")
                    }
                    firestoreProgress
            } else {
                // Fallback to Room, then sync to Firestore
                val roomProgress = userDataRepository.getUserProgressOnce()

                if (roomProgress != null) {
                    println("HybridDataManager: Using Room data: XP: ${roomProgress.totalXP}, Daily XP: ${roomProgress.dailyXPEarned}, Daily Minutes: ${roomProgress.dailyReadingMinutes}/${roomProgress.dailyTarget}, Streak: ${roomProgress.streakDays}")
                    // Sync to Firestore for future access
                    try {
                        firestoreRepository.saveUserProgress(userId, roomProgress)
                        firestoreRepository.updateUserXP(userId, getCurrentUsername(), roomProgress.totalXP)
                        println("HybridDataManager: Synced Room data to Firestore")
                    } catch (e: Exception) {
                        println("HybridDataManager: Failed to sync to Firestore: ${e.message}")
                    }
                    roomProgress
                } else {
                    // Create default progress
                    val defaultProgress = UserProgress()
                    userDataRepository.updateUserProgress(defaultProgress)
                    // Also save to Firestore
                    try {
                        firestoreRepository.saveUserProgress(userId, defaultProgress)
                        firestoreRepository.updateUserXP(userId, getCurrentUsername(), 0)
                        println("HybridDataManager: Created and synced default progress")
                    } catch (e: Exception) {
                        println("HybridDataManager: Failed to save default progress to Firestore: ${e.message}")
                    }
                    println("HybridDataManager: Created default progress")
                    defaultProgress
                }
            }
        } catch (e: Exception) {
            println("HybridDataManager: Error getting user progress: ${e.message}")
            // Return default progress if everything fails
            UserProgress()
        }
    }

    /**
     * Update streak days (dipanggil setiap hari)
     */
    suspend fun updateStreakDays() {
        try {
            val currentProgress = getUserProgress()

            if (currentProgress != null) {
                val today = System.currentTimeMillis()
                val lastReadDate = currentProgress.lastReadDate
                val daysDifference = ((today - lastReadDate) / (1000 * 60 * 60 * 24)).toInt()

                val newStreak = when {
                    daysDifference <= 1 -> currentProgress.streakDays + 1 // Continue streak
                    daysDifference > 1 -> 1 // Reset streak
                    else -> currentProgress.streakDays
                }

                val updatedProgress = currentProgress.copy(
                    streakDays = newStreak,
                    lastReadDate = today
                )

                // Update kedua database
                userDataRepository.updateUserProgress(updatedProgress)
                val userId = getCurrentUserId()
                firestoreRepository.saveUserProgress(userId, updatedProgress)
            }
        } catch (e: Exception) {
            // Handle error silently
        }
    }

    /**
     * Start reading session
     */
    suspend fun startReadingSession(articleId: Int): String {
        val userId = getCurrentUserId()
        return firestoreRepository.startReadingSession(userId, articleId)
    }

    /**
     * Update reading session with active time
     */
    suspend fun updateReadingSession(sessionId: String, activeTimeIncrement: Long) {
        firestoreRepository.updateReadingSession(sessionId, activeTimeIncrement)
    }

    /**
     * End reading session
     */
    suspend fun endReadingSession(sessionId: String, xpEarned: Int) {
        firestoreRepository.endReadingSession(sessionId, xpEarned)
    }

    /**
     * Admin function: Add XP to specific user (max 500 XP)
     */
    suspend fun adminAddXPToUser(userId: String, xpToAdd: Int): Result<Unit> {
        return try {
            if (xpToAdd <= 0 || xpToAdd > 500) {
                return Result.failure(Exception("XP harus antara 1-500"))
            }

            // Get current user progress
            val currentProgress = getUserProgress()
            if (currentProgress == null) {
                return Result.failure(Exception("User tidak ditemukan"))
            }

            // Calculate new progress
            val updatedProgress = currentProgress.copy(
                totalXP = currentProgress.totalXP + xpToAdd
            )

            // Update Firestore for all users (to appear in leaderboard)
            try {
                firestoreRepository.saveUserProgress(userId, updatedProgress)
                firestoreRepository.updateUserXP(userId, getCurrentUsername(), xpToAdd)
                println("HybridDataManager: Updated Firestore for admin XP addition")
            } catch (e: Exception) {
                println("HybridDataManager: Failed to update Firestore for admin XP: ${e.message}")
            }

            // Also update Room for consistency
            userDataRepository.updateUserProgress(updatedProgress)
            println("HybridDataManager: Updated Room for admin XP addition")

            println("HybridDataManager: Admin added $xpToAdd XP to user $userId")
            Result.success(Unit)
        } catch (e: Exception) {
            println("HybridDataManager: Error adding XP to user: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Reset data untuk testing
     */
    suspend fun resetUserData() {
        try {
            val userId = getCurrentUserId()
            val username = getCurrentUsername()
            println("HybridDataManager: Resetting data for user $userId ($username)")

            val defaultProgress = UserProgress()

            // Reset Firestore data for all users (for leaderboard)
            try {
                firestoreRepository.saveUserProgress(userId, defaultProgress)
                firestoreRepository.updateUserXP(userId, username, 0)
                println("HybridDataManager: Reset Firestore data")
            } catch (e: Exception) {
                println("HybridDataManager: Failed to reset Firestore data: ${e.message}")
            }

            // Reset Room data for offline access
            userDataRepository.updateUserProgress(defaultProgress)
            println("HybridDataManager: Reset Room data")

            println("HybridDataManager: User data reset completed")
        } catch (e: Exception) {
            println("HybridDataManager: Error resetting user data: ${e.message}")
        }
    }

}
