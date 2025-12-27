// File: data/service/DummyDataGenerator.kt
package com.readboost.id.data.service

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object DummyDataGenerator {

    private val firestore = FirebaseFirestore.getInstance()

    // Dummy users untuk ALL-TIME leaderboard (XP tinggi - ribuan)
    private val allTimeDummyUsers = listOf(
        Triple("dokja_reader", "Dokja", 8540),              // Kim Dokja - Protagonist ORV
        Triple("sooyoung_reader", "Sooyoung", 7230),         // Yoo Joonghyuk's companion
        Triple("joonghyuk_reader", "Joonghyuk", 6890),       // Yoo Joonghyuk - Regressor
        Triple("hakhyun_reader", "Hakhyun", 6120),           // Lee Hakhyun - Disaster of Questions
        Triple("uriel_reader", "Uriel", 5980),               // Uriel - Archangel
        Triple("ricardo_reader", "Ricardo", 5430),           // Ricardo - King of Beauty
        Triple("ryan_reader", "Ryan", 4980),                 // Ryan - Spear that Pierces the Side
        Triple("chalista_reader", "Chalista", 4520),         // Disaster of Floods
        Triple("jeje_reader", "Jeje", 4210),                 // Jeje - Prisoner of the Golden Headband
        Triple("arthur_reader", "Arthur", 3890)              // Arthur - King of the Last World
    )

    // Dummy users untuk WEEKLY leaderboard (XP maksimal 3000)
    private val weeklyDummyUsers = listOf(
        Triple("faiz_reader", "Faiz Galen", 2850),           // Nama Indonesia
        Triple("anindya_reader", "Anindya", 2420),           // Nama Indonesia
        Triple("najla_reader", "Najla", 2300),               // Nama Indonesia
        Triple("naylannajwa_reader", "Naylannajwa", 2150),   // Nama Indonesia
        Triple("erfina_reader", "Erfina", 1980),             // Nama Indonesia
        Triple("chandra_reader", "Chandra", 1850),           // Nama Indonesia
        Triple("nadia_reader", "Nadia", 1720),               // Nama Indonesia
        Triple("raisya_reader", "Raisya", 1650),             // Nama Indonesia
        Triple("aofy_reader", "Aofy", 1520),                 // Nama Indonesia
        Triple("nurjanah_reader", "Nurjanah", 1410)         // Nama Indonesia
    )

    suspend fun generateDummyLeaderboardData() {
        try {
            println("DummyDataGenerator: Generating dummy leaderboard data...")
            val batch = firestore.batch()

            // Generate ALL-TIME leaderboard data (static dummy data)
            allTimeDummyUsers.forEachIndexed { index, (userId, username, xp) ->
                println("DummyDataGenerator: Adding ALL-TIME #${index + 1} - $username (ID: $userId) with $xp XP")
                val userDocRef = firestore.collection("leaderboard_alltime").document(userId)
                val userData = mapOf(
                    "userId" to userId,
                    "username" to username,
                    "totalXP" to xp,
                    "lastUpdated" to System.currentTimeMillis()
                )
                batch.set(userDocRef, userData)
            }

            // Generate initial WEEKLY leaderboard data for current week (as fallback)
            val currentWeekKey = getCurrentWeekKey()
            weeklyDummyUsers.forEachIndexed { index, (userId, username, xp) ->
                println("DummyDataGenerator: Adding WEEKLY #${index + 1} - $username (ID: $userId) with $xp XP for week $currentWeekKey")
                val userDocRef = firestore.collection("leaderboard_weekly_$currentWeekKey").document(userId)
                val userData = mapOf(
                    "userId" to userId,
                    "username" to username,
                    "totalXP" to xp,
                    "weekKey" to currentWeekKey,
                    "lastUpdated" to System.currentTimeMillis()
                )
                batch.set(userDocRef, userData)
            }

            batch.commit().await()
            println("DummyDataGenerator: Dummy leaderboard data generated successfully! Added ${allTimeDummyUsers.size} all-time and ${weeklyDummyUsers.size} weekly users for week $currentWeekKey.")
        } catch (e: Exception) {
            println("DummyDataGenerator: Error generating dummy data: ${e.message}")
            e.printStackTrace()
        }
    }

    // Helper function to get current week key
    private fun getCurrentWeekKey(): String {
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        val year = calendar.get(java.util.Calendar.YEAR)
        val week = calendar.get(java.util.Calendar.WEEK_OF_YEAR)
        return String.format("%04d_%02d", year, week)
    }

    suspend fun generateDummyUserProgressData() {
        try {
            val batch = firestore.batch()

            // Generate progress data untuk beberapa dummy users
            val progressUsers = allTimeDummyUsers.take(5) // Ambil 5 user pertama dari all-time

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
            println("DummyDataGenerator: Starting to clear all dummy data...")

            // Clear in batches to avoid Firestore limits
            val staticCollectionsToClear = listOf(
                "leaderboard",           // Old collection
                "leaderboard_alltime",   // Static all-time collection
                "user_progress",         // User progress data
                "reading_sessions"       // Reading sessions
            )

            // Clear static collections
            for (collectionName in staticCollectionsToClear) {
                try {
                    val snapshot = firestore.collection(collectionName).get().await()
                    if (!snapshot.isEmpty) {
                        println("DummyDataGenerator: Clearing ${snapshot.size()} documents from '$collectionName'")
                        val batch = firestore.batch()
                        snapshot.documents.forEach { doc ->
                            batch.delete(doc.reference)
                        }
                        batch.commit().await()
                        println("DummyDataGenerator: Successfully cleared '$collectionName'")
                    } else {
                        println("DummyDataGenerator: Collection '$collectionName' is already empty")
                    }
                } catch (e: Exception) {
                    println("DummyDataGenerator: Error clearing collection '$collectionName': ${e.message}")
                }
            }

            // Clear weekly collections (find all weekly collections and clear them)
            try {
                // Get all collection IDs that start with "leaderboard_weekly_"
                val allCollections = firestore.collectionGroup("dummy").limit(1).get().await()
                // Since we can't easily list collections, we'll try to clear common weekly collections
                val currentWeekKey = getCurrentWeekKey()

                val weeklyCollectionsToClear = listOf(
                    "leaderboard_weekly",  // Old static weekly
                    "leaderboard_weekly_$currentWeekKey"  // Current week
                )

                for (collectionName in weeklyCollectionsToClear) {
                    try {
                        val snapshot = firestore.collection(collectionName).get().await()
                        if (!snapshot.isEmpty) {
                            println("DummyDataGenerator: Clearing ${snapshot.size()} documents from '$collectionName'")
                            val batch = firestore.batch()
                            snapshot.documents.forEach { doc ->
                                batch.delete(doc.reference)
                            }
                            batch.commit().await()
                            println("DummyDataGenerator: Successfully cleared '$collectionName'")
                        }
                    } catch (e: Exception) {
                        // Ignore errors for collections that don't exist
                        println("DummyDataGenerator: Collection '$collectionName' may not exist or already cleared")
                    }
                }
            } catch (e: Exception) {
                println("DummyDataGenerator: Error clearing weekly collections: ${e.message}")
            }

            println("DummyDataGenerator: All dummy data clearing completed!")
        } catch (e: Exception) {
            println("DummyDataGenerator: Error in clearAllDummyData: ${e.message}")
            e.printStackTrace()
        }
    }

    // Generate data untuk testing - dipanggil saat app start
    suspend fun initializeDummyDataIfNeeded() {
        try {
            println("DummyDataGenerator: Checking leaderboard data...")

            // Check all-time leaderboard data
            val allTimeSnapshot = firestore.collection("leaderboard_alltime").get().await()
            println("DummyDataGenerator: Found ${allTimeSnapshot.size()} all-time leaderboard documents")

            // Check weekly leaderboard data
            val weeklySnapshot = firestore.collection("leaderboard_weekly").get().await()
            println("DummyDataGenerator: Found ${weeklySnapshot.size()} weekly leaderboard documents")

            if (allTimeSnapshot.isEmpty || weeklySnapshot.isEmpty) {
                println("DummyDataGenerator: Leaderboard data incomplete, generating dummy data...")
                generateDummyLeaderboardData()
                generateDummyUserProgressData()
                println("DummyDataGenerator: Dummy data initialization completed!")
            } else {
                println("DummyDataGenerator: All leaderboard data exists (All-time: ${allTimeSnapshot.size()}, Weekly: ${weeklySnapshot.size()} documents), skipping dummy data generation")
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
