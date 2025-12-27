// File: presentation/screens/home/HomeScreen.kt
package com.readboost.id.presentation.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.readboost.id.ReadBoostApplication
import com.readboost.id.data.model.Article
import com.readboost.id.presentation.viewmodel.ViewModelFactory
import com.readboost.id.ui.theme.ReadBoostTheme

// Mock author names untuk artikel
private fun getMockAuthor(article: Article): String {
    val authors = listOf(
        "Antonio Bonabeno",
        "Alvin Kleon",
        "John Doe",
        "Jane Smith",
        "Robert Wilson",
        "Emily Johnson",
        "Michael Brown"
    )
    return authors[article.id % authors.size]
}

// Helper function untuk mendapatkan image URL
// Jika imageUrl ada di Article, gunakan itu. Jika tidak, gunakan placeholder
private fun getArticleImageUrl(article: Article): String {
    return article.imageUrl ?: "https://picsum.photos/seed/${article.id}/300/400"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToArticleList: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToLeaderboard: () -> Unit,
    onNavigateToArticle: (Int) -> Unit,
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    val app = context.applicationContext as? ReadBoostApplication

    // Get current user
    val currentUser = remember {
        app?.appContainer?.userPreferences?.getCurrentUser()
    }
    val currentUserName = currentUser?.fullName ?: "User"
    val isLoggedIn = currentUser != null

    // State for logout confirmation dialog
    var showLogoutDialog by remember { mutableStateOf(false) }

    // ViewModel initialization
    val homeViewModel = viewModel<HomeViewModel>(
        factory = if (app?.isAppContainerInitialized == true) {
            ViewModelFactory(app.appContainer)
        } else {
            null
        }
    )
    val uiState by homeViewModel.uiState.collectAsState()

    // Logout confirmation dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(text = "Konfirmasi Logout") },
            text = { Text("Apakah Anda yakin ingin logout dari akun Anda?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        app?.appContainer?.userPreferences?.logout()
                        onLogout() // Execute the logout action
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("Logout", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showLogoutDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            // Empty top bar karena kita menggunakan custom header
            Spacer(modifier = Modifier.height(0.dp))
        },
        bottomBar = {
            BottomNavigationBar(
                selectedRoute = "home",
                onNavigateToHome = { },
                onNavigateToArticleList = onNavigateToArticleList,
                onNavigateToLeaderboard = onNavigateToLeaderboard,
                onNavigateToProfile = onNavigateToProfile
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
                    .padding(paddingValues)
            ) {
                // Header Section
                item {
                    HeaderSection(
                        searchQuery = uiState.searchQuery,
                        onSearchQueryChange = homeViewModel::onSearchQueryChange,
                        userName = currentUserName,
                        isLoggedIn = isLoggedIn,
                        onLogout = { showLogoutDialog = true } // Show dialog on click
                    )
                }

                // Tampilkan NewCollectionSection hanya jika tidak ada pencarian
                if (uiState.searchQuery.isBlank()) {
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        NewCollectionSection(
                            articles = uiState.newCollectionArticles,
                            onArticleClick = onNavigateToArticle,
                            onNavigateToArticleList = onNavigateToArticleList
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        CategoryRow(
                            selectedCategory = uiState.selectedCategory,
                            onCategorySelected = homeViewModel::filterByCategory
                        )
                    }
                }

                // Article List
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    ArticleList(
                        articles = uiState.filteredArticles,
                        onArticleClick = onNavigateToArticle
                    )
                }

                // Bottom spacing untuk navigation bar
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}


@Composable
fun HeaderSection(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    userName: String = "User",
    isLoggedIn: Boolean = false,
    onLogout: () -> Unit = {}
) {
    // Background biru dengan rounded bottom
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1976D2), // Primary blue
                        Color(0xFF1565C0)  // Slightly darker blue
                    )
                ),
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            // Logo dan Nama Aplikasi
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                // Logo icon
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = "Logo ReadBoost",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                // Nama aplikasi
                Text(
                    text = "ReadBoost ID",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                // Logout button for logged in users
                if (isLoggedIn) {
                    Spacer(modifier = Modifier.weight(1f)) // Pushes logout to the end
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Logout",
                            tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // User greeting - centered
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Halo, $userName!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Horizontal ReadBoost image
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data("https://firebasestorage.googleapis.com/v0/b/readboost-9999.appspot.com/o/readboost_banner.png?alt=media&token=12345678-1234-1234-1234-1234567890ab")
                        .crossfade(true)
                        .build(),
                    contentDescription = "ReadBoost Banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Description about ReadBoost - maximum 2 lines, centered
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Platform pembelajaran yang membantu Anda meningkatkan pengetahuan melalui artikel berkualitas dalam berbagai kategori menarik.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f),
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.2f,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Search Bar
            SearchBar(
                query = searchQuery,
                onQueryChange = onSearchQueryChange
            )

        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = {
            Text(
                text = "Apa yang ingin Anda baca?",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Cari",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = Color.White.copy(alpha = 0.95f),
            focusedContainerColor = Color.White,
            unfocusedBorderColor = Color.Transparent,
            focusedBorderColor = MaterialTheme.colorScheme.primary
        ),
        singleLine = true
    )
}

@Composable
fun NewCollectionSection(
    articles: List<Article>,
    onArticleClick: (Int) -> Unit,
    onNavigateToArticleList: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Artikel Terbaru",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            TextButton(onClick = onNavigateToArticleList) {
                Text("Lihat Semua")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(articles) { article ->
                NewCollectionCard(
                    article = article,
                    onClick = { onArticleClick(article.id) }
                )
            }
        }
    }
}

@Composable
fun NewCollectionCard(
    article: Article,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(200.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = getCategoryColor(article.category)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            // Gambar cover artikel
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(getArticleImageUrl(article))
                    .crossfade(true)
                    .build(),
                contentDescription = article.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            // Gradient overlay untuk readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    )
            )

            // Content overlay
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                // Kategori
                Text(
                    text = article.category,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Judul artikel
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun CategoryRow(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    val categories = listOf("Populer", "Teknologi", "Sains", "Psikologi", "Sejarah", "Motivasi", "Sosial Budaya")

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 20.dp)
    ) {
        items(categories) { category ->
            CategoryChip(
                category = category,
                isSelected = category == selectedCategory,
                onClick = { onCategorySelected(category) }
            )
        }
    }
}

@Composable
fun CategoryChip(
    category: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(
                text = category,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

@Composable
fun ArticleList(
    articles: List<Article>,
    onArticleClick: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        if (articles.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Tidak ada artikel yang cocok dengan pencarian Anda.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            articles.forEach { article ->
                ArticleListItem(
                    article = article,
                    author = getMockAuthor(article),
                    onClick = { onArticleClick(article.id) }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ArticleListItem(
    article: Article,
    author: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Thumbnail gambar
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(80.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(getArticleImageUrl(article))
                        .crossfade(true)
                        .build(),
                    contentDescription = article.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Kategori
                Text(
                    text = article.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Judul artikel
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Nama penulis
                Text(
                    text = author,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            // Arrow icon
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Buka Artikel",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .size(24.dp)
            )
        }
    }
}

// ArticleCard untuk digunakan di ArticleListScreen
@Composable
fun ArticleCard(
    article: Article,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = article.category,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
                Text(
                    text = "${article.duration} min",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = article.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = article.content.take(100) + "...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "+${article.xp} XP",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    selectedRoute: String,
    onNavigateToHome: () -> Unit,
    onNavigateToArticleList: () -> Unit,
    onNavigateToLeaderboard: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = selectedRoute == "home",
            onClick = onNavigateToHome,
            icon = {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Beranda"
                )
            },
            label = { Text("Beranda") }
        )
        NavigationBarItem(
            selected = selectedRoute == "article",
            onClick = onNavigateToArticleList,
            icon = {
                Icon(
                    imageVector = Icons.Default.Article,
                    contentDescription = "Artikel"
                )
            },
            label = { Text("Artikel") }
        )
        NavigationBarItem(
            selected = selectedRoute == "leaderboard",
            onClick = onNavigateToLeaderboard,
            icon = {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Leaderboard"
                )
            },
            label = { Text("Leaderboard") }
        )
        NavigationBarItem(
            selected = selectedRoute == "profile",
            onClick = onNavigateToProfile,
            icon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profil"
                )
            },
            label = { Text("Profil") }
        )
    }
}

// Helper function untuk mendapatkan warna berdasarkan kategori
fun getCategoryColor(category: String): Color {
    return when (category.lowercase()) {
        "art", "seni" -> Color(0xFFFFD54F) // Yellow
        "design", "desain" -> Color(0xFFFFA726) // Orange
        "science", "ilmiah", "sains" -> Color(0xFF42A5F5) // Blue
        "business", "bisnis" -> Color(0xFF66BB6A) // Green
        "technology", "teknologi" -> Color(0xFFAB47BC) // Purple
        "fantasy", "fantasi" -> Color(0xFFEC407A) // Pink
        else -> Color(0xFF78909C) // Default gray
    }
}

// Preview Functions
@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    ReadBoostTheme {
        Scaffold(
            bottomBar = {
                BottomNavigationBar(
                    selectedRoute = "home",
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
                    .padding(paddingValues)
            ) {
                item {
                    HeaderSection(
                        searchQuery = "",
                        onSearchQueryChange = {},
                        isLoggedIn = true
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    NewCollectionSection(
                        articles = listOf(
                            Article(id = 1, title = "Gestalt", content = "Art content", duration = 5, category = "Art", xp = 15),
                            Article(id = 2, title = "Modern Design", content = "Design content", duration = 4, category = "Design", xp = 12),
                            Article(id = 3, title = "Astronomy", content = "Science content", duration = 6, category = "Science", xp = 18)
                        ),
                        onArticleClick = {},
                        onNavigateToArticleList = {}
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    CategoryRow(
                        selectedCategory = "Populer",
                        onCategorySelected = {}
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    ArticleList(
                        articles = listOf(
                            Article(id = 1, title = "Show Your Work", content = "Management content", duration = 5, category = "Management", xp = 15),
                            Article(id = 2, title = "Steal Like Designer", content = "Science content", duration = 4, category = "Science", xp = 12)
                        ).take(5), // Limit to maximum 5 articles
                        onArticleClick = {}
                    )
                }
            }
        }
    }
}
