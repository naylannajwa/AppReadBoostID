// File: presentation/screens/profile/ProfileScreen.kt (UPDATED)
package com.readboost.id.presentation.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

    // Refresh data when screen becomes visible (e.g., after reading article)
    LaunchedEffect(Unit) {
        println("ProfileScreen: Screen became visible, refreshing data")
        viewModel.refreshData()
    }

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
                    IconButton(onClick = { viewModel.forceRefreshData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh Data")
                    }
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
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Header Section - Pembaca Aktif
                item {
                    ReaderHeaderSection()
                }

                // Statistics Section - Vertical Layout
                item {
                    StatisticsSection(userProgress = uiState.userProgress ?: UserProgress())
                }

                // Quick Actions Section
                item {
                    QuickActionsSection(
                        onEditTarget = { showTargetDialog = true },
                        onNavigateToNotes = onNavigateToNotes,
                        onNavigateToAbout = {}
                    )
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

// Header Section - Pembaca Aktif (Simple Design)
@Composable
fun ReaderHeaderSection() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar dengan icon besar
        Surface(
            modifier = Modifier.size(120.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            shadowElevation = 8.dp
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "User Avatar",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(60.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Title - Pembaca Aktif
        Text(
            text = "Pembaca Aktif",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Subtitle
        Text(
            text = "Selamat datang di profil Anda",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

// Statistics Section - Vertical Layout
@Composable
fun StatisticsSection(userProgress: UserProgress?) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "Statistik Membaca",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // XP Statistic
        StatisticRowItem(
            icon = Icons.Default.Star,
            title = "Total XP",
            value = "${userProgress?.totalXP ?: 0}",
            color = Color(0xFFFFD700), // Gold
            description = "Poin yang dikumpulkan"
        )

        // Streak Statistic
        StatisticRowItem(
            icon = Icons.Default.LocalFireDepartment,
            title = "Streak Hari",
            value = "${userProgress?.streakDays ?: 0}",
            color = Color(0xFFFF5722), // Orange
            description = "Hari berturut-turut"
        )

        // Target Status (berdasarkan menit membaca)
        val isTargetAchieved = (userProgress?.dailyReadingMinutes ?: 0) >= (userProgress?.dailyTarget ?: 0)
        StatisticRowItem(
            icon = if (isTargetAchieved) Icons.Default.CheckCircle else Icons.Default.Schedule,
            title = if (isTargetAchieved) "Target Harian Tercapai" else "Target Harian",
            value = if (isTargetAchieved) "✓" else "${userProgress?.dailyReadingMinutes ?: 0}/${userProgress?.dailyTarget ?: 0} min",
            color = if (isTargetAchieved) Color(0xFF4CAF50) else Color(0xFF2196F3), // Green if achieved, Blue if not
            description = if (isTargetAchieved) "Selamat! Target hari ini tercapai" else "Progress membaca hari ini"
        )
    }
}

// Statistic Row Item - Horizontal Layout
@Composable
fun StatisticRowItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    color: Color,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon dengan background berwarna
            Surface(
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = color.copy(alpha = 0.1f)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Title and Value
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Description
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Quick Actions Section
@Composable
fun QuickActionsSection(
    onEditTarget: () -> Unit,
    onNavigateToNotes: () -> Unit,
    onNavigateToAbout: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Aksi Cepat",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        ActionItemCard(
            icon = Icons.Default.Edit,
            title = "Ubah Target Harian",
            subtitle = "Sesuaikan target membaca Anda",
            onClick = onEditTarget
        )

        ActionItemCard(
            icon = Icons.Default.Note,
            title = "Catatan Saya",
            subtitle = "Kelola catatan dan highlight",
            onClick = onNavigateToNotes
        )

        ActionItemCard(
            icon = Icons.Default.Info,
            title = "Tentang Aplikasi",
            subtitle = "Informasi ReadBoost ID",
            onClick = onNavigateToAbout
        )
    }
}

// Action Item Card
@Composable
fun ActionItemCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon dengan background
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Text Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Chevron Icon
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
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
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header Section
                item {
                    ReaderHeaderSection()
                }

                // Statistics Section
                item {
                    StatisticsSection(
                        userProgress = UserProgress(
                            totalXP = 250,
                            streakDays = 7,
                            dailyTarget = 5,
                            dailyXPEarned = 3,
                            dailyReadingMinutes = 3
                        )
                    )
                }

                // Quick Actions
                item {
                    QuickActionsSection(
                        onEditTarget = {},
                        onNavigateToNotes = {},
                        onNavigateToAbout = {}
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReaderHeaderSectionPreview() {
    ReadBoostTheme {
        ReaderHeaderSection()
    }
}

@Preview(showBackground = true)
@Composable
fun StatisticsSectionPreview() {
    ReadBoostTheme {
        StatisticsSection(
            userProgress = UserProgress(
                totalXP = 250,
                streakDays = 7,
                dailyTarget = 5,
                dailyXPEarned = 3,
                dailyReadingMinutes = 3
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun QuickActionsSectionPreview() {
    ReadBoostTheme {
        QuickActionsSection(
            onEditTarget = {},
            onNavigateToNotes = {},
            onNavigateToAbout = {}
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