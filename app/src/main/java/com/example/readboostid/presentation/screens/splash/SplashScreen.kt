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
fun SplashScreen(onNavigateToLogin: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try {
            // Initialize dummy data in background - force regenerate for updated data
            coroutineScope.launch {
                try {
                    val app = context.applicationContext as? ReadBoostApplication
                    if (app != null) {
                        // Force regenerate dummy data to ensure latest names are used
                        DummyDataGenerator.forceRegenerateDummyData()
                        Log.d("SplashScreen", "Dummy data force regeneration completed with updated names")
                    }
                } catch (e: Exception) {
                    Log.e("SplashScreen", "Failed to regenerate dummy data", e)
                    // Fallback to normal initialization
                    try {
                        DummyDataGenerator.initializeDummyDataIfNeeded()
                        Log.d("SplashScreen", "Fallback to normal dummy data initialization")
                    } catch (fallbackException: Exception) {
                        Log.e("SplashScreen", "Fallback initialization also failed", fallbackException)
                    }
                }
            }

            delay(2000)
            onNavigateToLogin()
        } catch (e: Exception) {
            // If navigation fails, show error
            Log.e("SplashScreen", "Navigation failed", e)
            // Still try to navigate even if dummy data fails
            onNavigateToLogin()
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
        SplashScreen(onNavigateToLogin = {})
    }
}