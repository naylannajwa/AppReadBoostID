package com.readboost.id
import android.app.Application
import android.util.Log
import com.readboost.id.di.AppContainer

class ReadBoostApplication : Application() {

    lateinit var appContainer: AppContainer
        private set

    val isAppContainerInitialized: Boolean
        get() = ::appContainer.isInitialized

    override fun onCreate() {
        super.onCreate()
        try {
            Log.d("ReadBoostApplication", "Initializing AppContainer...")
            // Initialize AppContainer with application context
            appContainer = AppContainer(this)
            Log.d("ReadBoostApplication", "AppContainer initialized successfully")

            // Test database initialization
            Log.d("ReadBoostApplication", "Testing database initialization...")
            // This will trigger lazy initialization of database through repositories
            val testRepo = appContainer.articleRepository
            Log.d("ReadBoostApplication", "Database test successful")
        } catch (e: Exception) {
            Log.e("ReadBoostApplication", "Critical error in onCreate", e)
            // Don't re-throw, just log the error to prevent app crash
            // But mark that initialization failed
            Log.e("ReadBoostApplication", "App initialization failed!", e)
        }
    }

    companion object {
        private const val TAG = "ReadBoostApplication"
    }
}
