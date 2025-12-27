// File: di/AppContainer.kt
package com.readboost.id.di

import android.content.Context
import android.util.Log
import com.readboost.id.data.local.UserPreferences
import com.readboost.id.data.local.database.AppDatabase
import com.readboost.id.data.service.FirebaseAuthService
import com.readboost.id.data.service.FirestoreLeaderboardService
import com.readboost.id.data.service.HybridDataManager
import com.readboost.id.data.repository.FirestoreLeaderboardRepositoryImpl
import com.readboost.id.domain.repository.ArticleRepository
import com.readboost.id.domain.repository.FirestoreLeaderboardRepository
import com.readboost.id.domain.repository.UserDataRepository
import com.readboost.id.domain.repository.UserRepository

class AppContainer(private val context: Context) {

    // Database - simplified initialization
    private val database: AppDatabase by lazy {
        Log.d("AppContainer", "Initializing database...")
        val db = AppDatabase.getDatabase(context)
        Log.d("AppContainer", "Database initialized successfully")
        db
    }

    // Repositories - simplified initialization
    val articleRepository: ArticleRepository by lazy {
        Log.d("AppContainer", "Initializing articleRepository...")
        val repo = ArticleRepository(database.articleDao())
        Log.d("AppContainer", "ArticleRepository initialized successfully")
        repo
    }

    val userDataRepository: UserDataRepository by lazy {
        Log.d("AppContainer", "Initializing userDataRepository...")
        val repo = UserDataRepository(
            database.notesDao(),
            database.userProgressDao(),
            database.leaderboardDao()
        )
        Log.d("AppContainer", "UserDataRepository initialized successfully")
        repo
    }

    val userRepository: UserRepository by lazy {
        Log.d("AppContainer", "Initializing userRepository...")
        val repo = com.readboost.id.data.repository.UserRepositoryImpl(database.userDao())
        Log.d("AppContainer", "UserRepository initialized successfully")
        repo
    }

    val userPreferences: UserPreferences by lazy {
        Log.d("AppContainer", "Initializing userPreferences...")
        val prefs = UserPreferences(context)
        Log.d("AppContainer", "UserPreferences initialized successfully")
        prefs
    }

    val firebaseAuthService: FirebaseAuthService by lazy {
        Log.d("AppContainer", "Initializing firebaseAuthService...")
        val service = FirebaseAuthService()
        Log.d("AppContainer", "FirebaseAuthService initialized successfully")
        service
    }

    val firestoreLeaderboardService: FirestoreLeaderboardService by lazy {
        Log.d("AppContainer", "Initializing firestoreLeaderboardService...")
        val service = FirestoreLeaderboardService()
        Log.d("AppContainer", "FirestoreLeaderboardService initialized successfully")
        service
    }

    val firestoreLeaderboardRepository: FirestoreLeaderboardRepository by lazy {
        Log.d("AppContainer", "Initializing firestoreLeaderboardRepository...")
        val repo = FirestoreLeaderboardRepositoryImpl(firestoreLeaderboardService)
        Log.d("AppContainer", "FirestoreLeaderboardRepository initialized successfully")
        repo
    }

    val hybridDataManager: HybridDataManager by lazy {
        Log.d("AppContainer", "Initializing hybridDataManager...")
        val manager = HybridDataManager(userDataRepository, firestoreLeaderboardRepository)
        Log.d("AppContainer", "HybridDataManager initialized successfully")
        manager
    }

    // Initialize dummy data on first access
    init {
        // Note: Dummy data initialization will be called from MainActivity or Splash
        Log.d("AppContainer", "AppContainer initialized successfully")
    }
}
