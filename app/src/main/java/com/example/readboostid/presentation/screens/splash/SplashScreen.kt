// File: presentation/screens/splash/SplashScreen.kt
package com.readboost.id.presentation.screens.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import com.readboost.id.ReadBoostApplication
import com.readboost.id.data.service.DummyDataGenerator
import com.readboost.id.ui.theme.ReadBoostTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.util.Log

@Composable
fun SplashScreen(
    onNavigateToWelcome: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToAdmin: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                val app = context.applicationContext as ReadBoostApplication
                // Initialize dummy data in background
                DummyDataGenerator.initializeDummyDataIfNeeded()

                // Check login status
                val currentUser = app.appContainer.userPreferences.getCurrentUser()

                delay(1500)

                when (currentUser?.role) {
                    "admin", "superadmin" -> onNavigateToAdmin()
                    "user" -> onNavigateToHome()
                    else -> onNavigateToWelcome()
                }
            } catch (e: Exception) {
                Log.e("SplashScreen", "Error during splash screen startup", e)
                onNavigateToWelcome() // Fallback to welcome screen
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "📚",
                fontSize = 80.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "ReadBoost ID",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tingkatkan Literasi Digital Indonesia",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(
    showBackground = true,
    device = Devices.PIXEL_4,
    showSystemUi = true,
    name = "Splash Screen - Full"
)
@Composable
fun SplashScreenPreview() {
    ReadBoostTheme {
        SplashScreen(
            onNavigateToWelcome = {},
            onNavigateToHome = {},
            onNavigateToAdmin = {}
        )
    }
}
