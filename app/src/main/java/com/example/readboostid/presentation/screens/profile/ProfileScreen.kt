// File: presentation/screens/profile/ProfileScreen.kt (UPDATED)
package com.readboost.id.presentation.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.readboost.id.ReadBoostApplication
import com.readboost.id.data.model.UserProgress
import com.readboost.id.presentation.screens.home.BottomNavigationBar
import com.readboost.id.presentation.viewmodel.ViewModelFactory
import com.readboost.id.ui.theme.ReadBoostTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToArticleList: () -> Unit,
    onNavigateToLeaderboard: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToNotes: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as ReadBoostApplication
    val viewModel: ProfileViewModel = viewModel(
        factory = ViewModelFactory(app.appContainer)
    )

    val uiState by viewModel.uiState.collectAsState()
    var showTargetDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profil & Statistik") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(
                selectedRoute = "profile",
                onNavigateToHome = onNavigateToHome,
                onNavigateToArticleList = onNavigateToArticleList,
                onNavigateToLeaderboard = onNavigateToLeaderboard,
                onNavigateToProfile = {}
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "👤",
                                style = MaterialTheme.typography.displayLarge
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Pembaca Aktif",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Statistik Membaca",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            StatRow(
                                icon = "⭐",
                                label = "Total XP",
                                value = "${uiState.userProgress?.totalXP ?: 0}"
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            StatRow(
                                icon = "🔥",
                                label = "Streak Hari",
                                value = "${uiState.userProgress?.streakDays ?: 0}"
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            StatRow(
                                icon = "🎯",
                                label = "Target Harian",
                                value = "${uiState.userProgress?.dailyTarget ?: 0} menit"
                            )
                        }
                    }
                }

                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            MenuItemRow(
                                icon = Icons.Default.Edit,
                                title = "Ubah Target Harian",
                                onClick = { showTargetDialog = true }
                            )
                            Divider()
                            MenuItemRow(
                                icon = Icons.Default.Note,
                                title = "Catatan Saya",
                                onClick = onNavigateToNotes
                            )
                            Divider()
                            MenuItemRow(
                                icon = Icons.Default.Info,
                                title = "Tentang Aplikasi",
                                onClick = { }
                            )
                        }
                    }
                }
            }

            if (showTargetDialog) {
                TargetDialog(
                    currentTarget = uiState.userProgress?.dailyTarget ?: 5,
                    onDismiss = { showTargetDialog = false },
                    onConfirm = { newTarget ->
                        viewModel.updateDailyTarget(newTarget)
                        showTargetDialog = false
                    }
                )
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun StatRow(icon: String, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = icon, style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun MenuItemRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null)
                Spacer(modifier = Modifier.width(12.dp))
                Text(title)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TargetDialog(
    currentTarget: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var selectedTarget by remember { mutableStateOf(currentTarget) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ubah Target Harian") },
        text = {
            Column {
                Text("Pilih target membaca harian:")
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf(2, 5, 10).forEach { target ->
                        FilterChip(
                            selected = selectedTarget == target,
                            onClick = { selectedTarget = target },
                            label = { Text("$target min") }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedTarget) }) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(
    showBackground = true,
    device = Devices.PIXEL_4,
    showSystemUi = true,
    name = "Profile Screen - Full"
)
@Composable
fun ProfileScreenPreview() {
    ReadBoostTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Profil & Statistik") },
                    navigationIcon = {
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    }
                )
            },
            bottomBar = {
                BottomNavigationBar(
                    selectedRoute = "profile",
                    onNavigateToHome = {},
                    onNavigateToArticleList = {},
                    onNavigateToLeaderboard = {},
                    onNavigateToProfile = {}
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    StatCard(
                        title = "Total XP",
                        value = "250 XP",
                        icon = Icons.Default.Star
                    )
                }
                item {
                    StatCard(
                        title = "Total Membaca",
                        value = "25 artikel",
                        icon = Icons.Default.Book
                    )
                }
                item {
                    StatCard(
                        title = "Streak Hari",
                        value = "7 hari",
                        icon = Icons.Default.TrendingUp
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StatCardPreview() {
    ReadBoostTheme {
        StatCard(
            title = "Total Membaca",
            value = "25 artikel",
            icon = Icons.Default.Book
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TargetDialogPreview() {
    ReadBoostTheme {
        TargetDialog(
            currentTarget = 5,
            onDismiss = {},
            onConfirm = {}
        )
    }
}