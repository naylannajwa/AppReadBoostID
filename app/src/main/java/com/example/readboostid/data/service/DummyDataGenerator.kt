// File: data/service/DummyDataGenerator.kt
package com.readboost.id.data.service

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object DummyDataGenerator {

    private val firestore = FirebaseFirestore.getInstance()

    // Dummy users dengan XP rendah untuk testing
    private val dummyUsers = listOf(
        Triple("alex_reader", "Alex Johnson", 45),      // XP rendah
        Triple("sarah_bookworm", "Sarah Chen", 32),     // XP rendah
        Triple("mike_learner", "Mike Wilson", 28),      // XP rendah
        Triple("lisa_student", "Lisa Park", 51),        // XP sedang
        Triple("david_researcher", "David Kim", 39),    // XP sedang
        Triple("emma_writer", "Emma Taylor", 67),       // XP tinggi
        Triple("ryan_developer", "Ryan Garcia", 23),    // XP rendah
        Triple("anna_designer", "Anna Brown", 41)       // XP sedang
    )

    suspend fun generateDummyLeaderboardData() {
        try {
            println("DummyDataGenerator: Generating dummy leaderboard data...")
            val batch = firestore.batch()

            dummyUsers.forEach { (userId, username, xp) ->
                println("DummyDataGenerator: Adding user $username with $xp XP")
                val userDocRef = firestore.collection("leaderboard").document(userId)
                val userData = mapOf(
                    "userId" to userId,
                    "username" to username,
                    "totalXP" to xp,
                    "lastUpdated" to System.currentTimeMillis()
                )
                batch.set(userDocRef, userData)
            }

            batch.commit().await()
            println("DummyDataGenerator: Dummy leaderboard data generated successfully! Added ${dummyUsers.size} users.")
        } catch (e: Exception) {
            println("DummyDataGenerator: Error generating dummy data: ${e.message}")
            e.printStackTrace()
        }
    }

    suspend fun generateDummyUserProgressData() {
        try {
            val batch = firestore.batch()

            // Generate progress data untuk beberapa dummy users
            val progressUsers = dummyUsers.take(5) // Ambil 5 user pertama

            progressUsers.forEach { (userId, username, xp) ->
                val progressDocRef = firestore.collection("user_progress").document(userId)
                val progressData = mapOf(
                    "userId" to userId,
                    "totalXP" to xp,
                    "streakDays" to (2..7).random(), // Random streak 2-7 hari
                    "dailyTarget" to 5, // 5 menit default
                    "lastReadDate" to (System.currentTimeMillis() - (1..3).random() * 24 * 60 * 60 * 1000), // 1-3 hari yang lalu
                    "dailyXPEarned" to xp, // XP yang didapat hari ini
                    "dailyReadingMinutes" to (xp * 2), // Asumsi 1 XP = 2 menit membaca
                    "lastStreakDate" to System.currentTimeMillis(), // Hari terakhir streak
                    "lastUpdated" to System.currentTimeMillis()
                )
                batch.set(progressDocRef, progressData)
            }

            batch.commit().await()
            println("Dummy user progress data generated successfully!")
        } catch (e: Exception) {
            println("Error generating dummy progress data: ${e.message}")
        }
    }

    suspend fun clearAllDummyData() {
        try {
            // Clear leaderboard data
            val leaderboardSnapshot = firestore.collection("leaderboard").get().await()
            val batch = firestore.batch()

            leaderboardSnapshot.documents.forEach { doc ->
                batch.delete(doc.reference)
            }

            // Clear user progress data
            val progressSnapshot = firestore.collection("user_progress").get().await()
            progressSnapshot.documents.forEach { doc ->
                batch.delete(doc.reference)
            }

            // Clear reading sessions
            val sessionsSnapshot = firestore.collection("reading_sessions").get().await()
            sessionsSnapshot.documents.forEach { doc ->
                batch.delete(doc.reference)
            }

            batch.commit().await()
            println("All dummy data cleared successfully!")
        } catch (e: Exception) {
            println("Error clearing dummy data: ${e.message}")
        }
    }

    // Generate data untuk testing - dipanggil saat app start
    suspend fun initializeDummyDataIfNeeded() {
        try {
            println("DummyDataGenerator: Checking leaderboard data...")

            val leaderboardSnapshot = firestore.collection("leaderboard").get().await()
            println("DummyDataGenerator: Found ${leaderboardSnapshot.size()} leaderboard documents")

            if (leaderboardSnapshot.isEmpty) {
                println("DummyDataGenerator: No leaderboard data found, generating dummy data...")
                generateDummyLeaderboardData()
                generateDummyUserProgressData()
                println("DummyDataGenerator: Dummy data initialization completed!")
            } else {
                println("DummyDataGenerator: Leaderboard data already exists (${leaderboardSnapshot.size()} documents), skipping dummy data generation")
                // Uncomment the line below to force regenerate dummy data for testing
                // forceRegenerateDummyData()
            }
        } catch (e: Exception) {
            println("DummyDataGenerator: Error checking/initializing dummy data: ${e.message}")
            e.printStackTrace()
        }
    }

    // Force regenerate dummy data for testing
    suspend fun forceRegenerateDummyData() {
        try {
            println("DummyDataGenerator: Force regenerating dummy data...")

            // Test Firestore connection first
            try {
                val testDoc = firestore.collection("test").document("connection_test").get().await()
                println("DummyDataGenerator: Firestore connection test successful")
            } catch (e: Exception) {
                println("DummyDataGenerator: Firestore connection test failed: ${e.message}")
                throw e
            }

            clearAllDummyData()
            generateDummyLeaderboardData()
            generateDummyUserProgressData()
            println("DummyDataGenerator: Force regeneration completed!")
        } catch (e: Exception) {
            println("DummyDataGenerator: Error force regenerating data: ${e.message}")
            e.printStackTrace()
        }
    }
}
