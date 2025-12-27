// File: presentation/screens/article/ArticleListScreen.kt (UPDATED)
package com.readboost.id.presentation.screens.article

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.readboost.id.ReadBoostApplication
import com.readboost.id.data.model.Article
import com.readboost.id.presentation.screens.home.ArticleCard
import com.readboost.id.presentation.viewmodel.ViewModelFactory
import com.readboost.id.ui.theme.ReadBoostTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleListScreen(
    onNavigateBack: () -> Unit,
    onArticleClick: (Int) -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as ReadBoostApplication
    val viewModel: ArticleViewModel = viewModel(
        factory = ViewModelFactory(app.appContainer)
    )

    val uiState by viewModel.uiState.collectAsState()

    val categories = listOf("All", "Teknologi", "Sains", "Psikologi", "Sejarah", "Motivasi", "Sosial & Budaya")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Semua Artikel") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    FilterChip(
                        selected = uiState.selectedCategory == category,
                        onClick = { viewModel.filterByCategory(category) },
                        label = { Text(category) }
                    )
                }
            }

            Divider()

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.articles) { article ->
                        ArticleCard(
                            article = article,
                            onClick = { onArticleClick(article.id) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(
    showBackground = true,
    device = Devices.PIXEL_4,
    showSystemUi = true,
    name = "Article List Screen - Full"
)
@Composable
fun ArticleListScreenPreview() {
    ReadBoostTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Semua Artikel") },
                    navigationIcon = {
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(listOf("All", "Teknologi", "Sains", "Psikologi", "Sejarah", "Motivasi", "Sosial & Budaya")) { category ->
                        FilterChip(
                            selected = category == "All",
                            onClick = {},
                            label = { Text(category) }
                        )
                    }
                }
                Divider()
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(listOf(
                        Article(id = 1, title = "Memahami Kecerdasan Buatan (AI)", content = "Kecerdasan Buatan atau Artificial Intelligence (AI) adalah simulasi kecerdasan manusia yang diprogram dalam mesin. AI memungkinkan komputer untuk belajar dari pengalaman.", duration = 5, category = "Teknologi", difficulty = "Dasar", xp = 15),
                        Article(id = 2, title = "Psikologi Kebahagiaan", content = "Kebahagiaan adalah kondisi emosional yang ditandai dengan perasaan positif seperti kepuasan, kegembiraan, dan makna hidup.", duration = 4, category = "Psikologi", difficulty = "Dasar", xp = 12),
                        Article(id = 3, title = "Sejarah Internet", content = "Internet dimulai sebagai proyek militer AS pada tahun 1960-an yang disebut ARPANET.", duration = 6, category = "Sejarah", difficulty = "Menengah", xp = 18)
                    )) { article ->
                        ArticleCard(article = article, onClick = {})
                    }
                }
            }
        }
    }
}

