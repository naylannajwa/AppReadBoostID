// File: presentation/screens/leaderboard/LeaderboardScreen.kt
package com.readboost.id.presentation.screens.leaderboard

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
import com.readboost.id.data.model.Leaderboard
import com.readboost.id.presentation.viewmodel.ViewModelFactory
import com.readboost.id.ui.theme.ReadBoostTheme
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as? ReadBoostApplication

    val viewModel: LeaderboardViewModel = viewModel(
        factory = if (app?.isAppContainerInitialized == true) {
            ViewModelFactory(app.appContainer)
        } else {
            null
        }
    )

    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            LeaderboardHeader(onNavigateBack = onNavigateBack)
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
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                // Time Filter Toggle
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    TimeFilterToggle(
                        selectedFilter = uiState.selectedFilter,
                        onFilterSelected = { filter ->
                            viewModel.setTimeFilter(filter)
                        }
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Top 3 Podium
                if (uiState.leaderboard.size >= 3) {
                    item {
                        TopThreePodium(
                            topThree = uiState.leaderboard.take(3)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                // Rest of the leaderboard (Rank 4+)
                if (uiState.leaderboard.size > 3) {
                    itemsIndexed(uiState.leaderboard.drop(3)) { index, entry ->
                        LeaderboardItem(
                            rank = entry.rank,
                            username = entry.username,
                            xp = entry.totalXP,
                            rankChange = 0 // TODO: Calculate from previous ranks
                        )
                        if (index < uiState.leaderboard.size - 4) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                } else if (uiState.leaderboard.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Belum ada data leaderboard",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardHeader(onNavigateBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1976D2), // Primary blue
                        Color(0xFF1565C0)  // Slightly darker blue
                    )
                )
            )
    ) {
        Column {
            // Top App Bar
            TopAppBar(
                title = {
                            Text(
                        text = "Leaderboard",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeFilterToggle(
    selectedFilter: TimeFilter,
    onFilterSelected: (TimeFilter) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier
                .weight(1f)
                .height(40.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFFFFA726).copy(alpha = 0.2f) // Light orange background
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Weekly Button
                FilterChip(
                    selected = selectedFilter == TimeFilter.Weekly,
                    onClick = { onFilterSelected(TimeFilter.Weekly) },
                    label = { Text("Weekly") },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color.White,
                        selectedLabelColor = Color(0xFFFFA726),
                        containerColor = Color.Transparent,
                        labelColor = Color(0xFFFFA726).copy(alpha = 0.7f)
                    )
                )

                // All Time Button
                FilterChip(
                    selected = selectedFilter == TimeFilter.AllTime,
                    onClick = { onFilterSelected(TimeFilter.AllTime) },
                    label = { Text("All Time") },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color.White,
                        selectedLabelColor = Color(0xFFFFA726),
                        containerColor = Color.Transparent,
                        labelColor = Color(0xFFFFA726).copy(alpha = 0.7f)
                    )
                )
            }
        }
    }
}

@Composable
fun TopThreePodium(topThree: List<Leaderboard>) {
    val infiniteTransition = rememberInfiniteTransition()
    val scale1 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val scale2 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val scale3 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Podium Container
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            // Rank 2 (Left)
            if (topThree.size > 1) {
                PodiumItem(
                    entry = topThree[1],
                    rank = 2,
                    modifier = Modifier
                        .weight(1f)
                        .scale(scale2),
                    height = 180.dp
                    )
                }

            // Rank 1 (Center - Tallest)
            PodiumItem(
                entry = topThree[0],
                rank = 1,
                modifier = Modifier
                    .weight(1f)
                    .scale(scale1),
                height = 220.dp
            )

            // Rank 3 (Right)
            if (topThree.size > 2) {
                PodiumItem(
                    entry = topThree[2],
                    rank = 3,
                    modifier = Modifier
                        .weight(1f)
                        .scale(scale3),
                    height = 160.dp
                )
            }
        }
    }
}

@Composable
fun PodiumItem(
    entry: Leaderboard,
    rank: Int,
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp
) {
    val medalColor = when (rank) {
        1 -> Color(0xFFFFD700) // Gold
        2 -> Color(0xFFC0C0C0) // Silver
        3 -> Color(0xFFCD7F32) // Bronze
        else -> Color.Gray
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar and Info
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            InitialAvatar(
                username = entry.username,
                size = 64.dp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = entry.username,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = "${entry.totalXP} pts",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Podium Base
        Card(
            modifier = Modifier
                .width(80.dp)
                .height(height),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = medalColor.copy(alpha = 0.3f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "#$rank",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = medalColor
                )
            }
        }
    }
}

@Composable
fun LeaderboardItem(
    rank: Int,
    username: String,
    xp: Int,
    rankChange: Int = 0 // Positive = down, Negative = up
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank and Avatar
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Rank Number
                Text(
                    text = String.format("%02d", rank),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.width(32.dp)
                )

                // Avatar
                InitialAvatar(
                    username = username,
                    size = 48.dp
                )

                // Username
                Column {
                Text(
                    text = username,
                    style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "$xp pts",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            // Rank Change Indicator
            if (rankChange != 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (rankChange < 0) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                        contentDescription = null,
                        tint = if (rankChange < 0) Color(0xFF4CAF50) else Color(0xFFF44336),
                        modifier = Modifier.size(16.dp)
                    )
            Text(
                        text = "${abs(rankChange)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (rankChange < 0) Color(0xFF4CAF50) else Color(0xFFF44336),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun InitialAvatar(
    username: String,
    size: androidx.compose.ui.unit.Dp
) {
    val initial = username.firstOrNull()?.uppercaseChar() ?: '?'
    val backgroundColor = getAvatarColor(username)

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

// Generate consistent color based on username
fun getAvatarColor(username: String): Color {
    val colors = listOf(
        Color(0xFF1976D2), // Blue
        Color(0xFF388E3C), // Green
        Color(0xFFFFA726), // Orange
        Color(0xFF7B1FA2), // Purple
        Color(0xFFE91E63), // Pink
        Color(0xFF0097A7), // Cyan
        Color(0xFFFF5722), // Deep Orange
        Color(0xFF795548), // Brown
        Color(0xFF607D8B), // Blue Grey
        Color(0xFF9C27B0)  // Purple
    )
    val index = abs(username.hashCode()) % colors.size
    return colors[index]
}

// Preview Functions
@OptIn(ExperimentalMaterial3Api::class)
@Preview(
    showBackground = true,
    device = Devices.PIXEL_4,
    showSystemUi = true,
    name = "Leaderboard Screen - Full"
)
@Composable
fun LeaderboardScreenPreview() {
    ReadBoostTheme {
        Scaffold(
            topBar = {
                LeaderboardHeader(onNavigateBack = {})
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    TimeFilterToggle(
                        selectedFilter = TimeFilter.Weekly,
                        onFilterSelected = {}
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
                    TopThreePodium(
                        topThree = listOf(
                            Leaderboard(userId = 1, username = "Lydia Price", totalXP = 413, rank = 1),
                            Leaderboard(userId = 2, username = "Lois Parker", totalXP = 311, rank = 2),
                            Leaderboard(userId = 3, username = "Mary Clark", totalXP = 227, rank = 3)
                        )
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                        }

                itemsIndexed(listOf(
                    Leaderboard(userId = 4, username = "Yvonne Brown", totalXP = 174, rank = 4),
                    Leaderboard(userId = 5, username = "Paul King", totalXP = 172, rank = 5),
                    Leaderboard(userId = 6, username = "Robert Hernandez", totalXP = 141, rank = 6),
                    Leaderboard(userId = 7, username = "Shirley Morgan", totalXP = 136, rank = 7)
                )) { index, entry ->
                    LeaderboardItem(
                        rank = entry.rank,
                        username = entry.username,
                        xp = entry.totalXP,
                        rankChange = if (index % 2 == 0) -1 else 1
                    )
                    if (index < 3) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}
