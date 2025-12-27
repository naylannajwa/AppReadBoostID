// File: presentation/screens/leaderboard/LeaderboardScreen.kt - PROFESSIONAL VERSION
package com.readboost.id.presentation.screens.leaderboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.readboost.id.ReadBoostApplication
import com.readboost.id.data.model.Leaderboard
import com.readboost.id.presentation.screens.home.BottomNavigationBar
import com.readboost.id.presentation.viewmodel.ViewModelFactory
import com.readboost.id.ui.theme.ReadBoostTheme
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToArticleList: () -> Unit,
    onNavigateToProfile: () -> Unit
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

    // Background gradient
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                )
            )
    ) {
        Scaffold(
            topBar = {
                LeaderboardHeader(onNavigateBack = onNavigateBack)
            },
            bottomBar = {
                BottomNavigationBar(
                    selectedRoute = "leaderboard",
                    onNavigateToHome = onNavigateToHome,
                    onNavigateToArticleList = onNavigateToArticleList,
                    onNavigateToLeaderboard = {},
                    onNavigateToProfile = onNavigateToProfile
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            if (uiState.isLoading) {
                LeaderboardLoadingSkeleton(paddingValues)
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(bottom = 24.dp, top = 16.dp)
                ) {
                    // Welcome section with trophy
                    item {
                        LeaderboardWelcomeSection()
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    // Time Filter Toggle
                    item {
                        TimeFilterToggle(
                            selectedFilter = uiState.selectedFilter,
                            onFilterSelected = { filter ->
                                viewModel.setTimeFilter(filter)
                            }
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                    }

                    // Top 3 Podium
                    if (uiState.leaderboard.size >= 3) {
                        item {
                            TopThreePodiumSimple(
                                topThree = uiState.leaderboard.take(3)
                            )
                        }
                    }

                    // Other Rankings section (only show if there are more than 3)
                    if (uiState.leaderboard.size > 3) {
                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "Other Rankings",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 20.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }

                    // Rest of the leaderboard (Rank 4+)
                    if (uiState.leaderboard.size > 3) {
                        itemsIndexed(uiState.leaderboard.drop(3)) { index, entry ->
                            LeaderboardItemSimple(
                                rank = entry.rank,
                                username = entry.username,
                                xp = entry.totalXP,
                                rankChange = if (index % 3 == 0) -1 else if (index % 3 == 1) 1 else 0,
                                isLastItem = index == uiState.leaderboard.size - 4
                            )
                        }
                    } else if (uiState.leaderboard.isEmpty()) {
                        item {
                            LeaderboardEmptyState()
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
                .height(44.dp),
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Weekly Button
                FilterChip(
                    selected = selectedFilter == TimeFilter.Weekly,
                    onClick = { onFilterSelected(TimeFilter.Weekly) },
                    label = {
                        Text(
                            "Weekly",
                            style = MaterialTheme.typography.labelLarge
                        )
                    },
                    modifier = Modifier.height(36.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = Color.Transparent,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    border = null
                )

                // All Time Button
                FilterChip(
                    selected = selectedFilter == TimeFilter.AllTime,
                    onClick = { onFilterSelected(TimeFilter.AllTime) },
                    label = {
                        Text(
                            "All Time",
                            style = MaterialTheme.typography.labelLarge
                        )
                    },
                    modifier = Modifier.height(36.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = Color.Transparent,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    border = null
                )
            }
        }
    }
}

@Composable
fun TopThreePodiumSimple(topThree: List<Leaderboard>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        // Rank 2 (Left)
        if (topThree.size > 1) {
            PodiumItemSimple(
                entry = topThree[1],
                rank = 2,
                height = 140.dp,
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }

        // Rank 1 (Center - Tallest)
        if (topThree.isNotEmpty()) {
            PodiumItemSimple(
                entry = topThree[0],
                rank = 1,
                height = 180.dp,
                backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                isChampion = true
            )
        }

        // Rank 3 (Right)
        if (topThree.size > 2) {
            PodiumItemSimple(
                entry = topThree[2],
                rank = 3,
                height = 120.dp,
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@Composable
fun PodiumItemSimple(
    entry: Leaderboard,
    rank: Int,
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp,
    backgroundColor: Color,
    isChampion: Boolean = false
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar
        InitialAvatar(
            username = entry.username,
            size = if (isChampion) 56.dp else 52.dp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Name
        Text(
            text = entry.username,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        // XP
        Text(
            text = "${entry.totalXP} XP",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = if (isChampion)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Podium base
        Surface(
            modifier = Modifier
                .width(64.dp)
                .height(height),
            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
            color = backgroundColor
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = rank.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 20.sp
                )
            }
        }
    }
}


@Composable
fun InitialAvatar(
    username: String,
    size: androidx.compose.ui.unit.Dp,
    borderColor: Color? = null
) {
    val initial = username.firstOrNull()?.uppercaseChar() ?: '?'
    val backgroundColor = getAvatarColor(username)

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor)
            .then(
                if (borderColor != null) {
                    Modifier.border(
                        width = 2.dp,
                        color = borderColor,
                        shape = CircleShape
                    )
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontSize = (size.value * 0.4f).sp
        )
    }
}

@Composable
fun LeaderboardItemSimple(
    rank: Int,
    username: String,
    xp: Int,
    rankChange: Int = 0,
    isLastItem: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Rank
        Text(
            text = rank.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.width(32.dp),
            textAlign = TextAlign.Center
        )

        // Avatar
        InitialAvatar(
            username = username,
            size = 44.dp
        )

        // Name and XP
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = username,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "$xp XP",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }

        // Rank change (if any)
        if (rankChange != 0) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Icon(
                    if (rankChange < 0) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    contentDescription = null,
                    tint = if (rankChange < 0) Color(0xFF4CAF50) else Color(0xFFF44336),
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "${abs(rankChange)}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = if (rankChange < 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                )
            }
        }
    }

    // Divider
    if (!isLastItem) {
        Divider(
            modifier = Modifier.padding(horizontal = 20.dp),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
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
// New Components for Enhanced Leaderboard

@Composable
fun LeaderboardWelcomeSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Trophy icon with professional styling
        Surface(
            modifier = Modifier
                .size(72.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            shadowElevation = 4.dp
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = Icons.Filled.EmojiEvents,
                    contentDescription = "Leaderboard Trophy",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Leaderboard",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Top readers this week",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun LeaderboardLoadingSkeleton(paddingValues: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        // Welcome section skeleton
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray.copy(alpha = 0.3f))
            )
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(24.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.LightGray.copy(alpha = 0.3f))
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .width(160.dp)
                    .height(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.LightGray.copy(alpha = 0.3f))
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Filter skeleton
        Box(
            modifier = Modifier
                .width(220.dp)
                .height(44.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(Color.LightGray.copy(alpha = 0.3f))
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Title skeleton
        Box(
            modifier = Modifier
                .width(140.dp)
                .height(28.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.LightGray.copy(alpha = 0.3f))
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Podium skeleton
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            repeat(3) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray.copy(alpha = 0.3f))
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(20.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.LightGray.copy(alpha = 0.3f))
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(50.dp)
                            .height(16.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.LightGray.copy(alpha = 0.3f))
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .width(88.dp)
                            .height(120.dp + (it * 40).dp)
                            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                            .background(Color.LightGray.copy(alpha = 0.3f))
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Section header skeleton
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray.copy(alpha = 0.3f))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.LightGray.copy(alpha = 0.3f))
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // List items skeleton
        repeat(4) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(horizontal = 20.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.LightGray.copy(alpha = 0.3f))
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun LeaderboardEmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Empty state illustration
        Surface(
            modifier = Modifier
                .size(120.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant,
            shadowElevation = 2.dp
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = Icons.Outlined.Leaderboard,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "No Data Available",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Start reading articles to appear on the leaderboard",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        FilledTonalButton(
            onClick = { /* Navigate to articles */ },
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Outlined.MenuBook, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Start Reading")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(
    showBackground = true,
    device = Devices.PIXEL_4,
    showSystemUi = false,
    name = "Leaderboard Screen - Enhanced"
)
@Composable
fun LeaderboardScreenPreview() {
    ReadBoostTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                topBar = {
                    LeaderboardHeader(onNavigateBack = {})
                },
                bottomBar = {
                    BottomNavigationBar(
                        selectedRoute = "leaderboard",
                        onNavigateToHome = {},
                        onNavigateToArticleList = {},
                        onNavigateToLeaderboard = {},
                        onNavigateToProfile = {}
                    )
                },
                containerColor = Color.Transparent
            ) { paddingValues ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(bottom = 24.dp, top = 16.dp)
                ) {
                    // Welcome section
                    item {
                        LeaderboardWelcomeSection()
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    // Filter
                    item {
                        TimeFilterToggle(
                            selectedFilter = TimeFilter.Weekly,
                            onFilterSelected = {}
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                    }

                    // Top 3 Podium
                    item {
                        TopThreePodiumSimple(
                            topThree = listOf(
                                Leaderboard(userId = 1, username = "Lydia Price", totalXP = 413, rank = 1),
                                Leaderboard(userId = 2, username = "Lois Parker", totalXP = 311, rank = 2),
                                Leaderboard(userId = 3, username = "Mary Clark", totalXP = 227, rank = 3)
                            )
                        )
                    }

                    // Other rankings section
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Other Rankings",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Leaderboard items
                    itemsIndexed(listOf(
                        Leaderboard(userId = 4, username = "Yvonne Brown", totalXP = 174, rank = 4),
                        Leaderboard(userId = 5, username = "Paul King", totalXP = 172, rank = 5),
                        Leaderboard(userId = 6, username = "Robert Hernandez", totalXP = 141, rank = 6),
                        Leaderboard(userId = 7, username = "Shirley Morgan", totalXP = 136, rank = 7)
                    )) { index, entry ->
                        LeaderboardItemSimple(
                            rank = entry.rank,
                            username = entry.username,
                            xp = entry.totalXP,
                            rankChange = when (index % 4) {
                                0 -> -2  // Up
                                1 -> 1   // Down
                                2 -> -1  // Up
                                else -> 0 // No change
                            },
                            isLastItem = index == 3
                        )
                    }
                }
            }
        }
    }
}
