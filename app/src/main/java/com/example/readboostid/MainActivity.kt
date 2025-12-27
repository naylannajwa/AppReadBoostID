// File: MainActivity.kt
package com.readboost.id

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.readboost.id.presentation.navigation.NavGraph
import com.readboost.id.presentation.navigation.Screen
import com.readboost.id.ui.theme.ReadBoostTheme

/**
 * ReadBoost ID - Aplikasi Literasi Digital
 * Tugas Besar PAB 2025
 *
 * Anggota Kelompok:
 * 1. Nama: [NAYLANNAJWA JIHANA UMMA], NIM: [23523183]
 * 2. Nama: [ERFINA SAFITRI], NIM: [23523192]
 * 3. Nama: [PAMELA NAJLA GHASSANI], NIM: [23523249]
 * 4. Nama: [ANINDYA AYU NABILAH], NIM: [23523268]
 *
 * Username & Password:
 * - Admin: username: admin, password: admin123
 * - User: username: user, password: user123
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContent {
                ReadBoostTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        val navController = rememberNavController()

                        // Check for existing session and set appropriate start destination
                        val startDestination = getStartDestinationBasedOnSession()

                        NavGraph(navController = navController, startDestination = startDestination)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("MainActivity", "Critical error in setContent", e)
            // Show error to user if needed
            finish()
        }
    }

    private fun getStartDestinationBasedOnSession(): String {
        return try {
            val app = applicationContext as? ReadBoostApplication
            val userPreferences = app?.appContainer?.userPreferences
            val currentUser = userPreferences?.getCurrentUser()

            when {
                currentUser?.role == "admin" -> Screen.AdminDashboard.route
                currentUser != null -> Screen.Home.route
                else -> Screen.Welcome.route
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error checking session", e)
            Screen.Welcome.route
        }
    }
}