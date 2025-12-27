// File: presentation/screens/admin/EditArticleScreen.kt
package com.readboost.id.presentation.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.readboost.id.ReadBoostApplication
import com.readboost.id.data.model.Article
import com.readboost.id.presentation.viewmodel.ViewModelFactory
import com.readboost.id.ui.theme.ReadBoostTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditArticleScreen(
    articleId: Int,
    onNavigateBack: () -> Unit,
    onArticleUpdated: () -> Unit = {}
) {
    val context = LocalContext.current
    val app = context.applicationContext as? ReadBoostApplication

    val editArticleViewModel = viewModel<EditArticleViewModel>(
        factory = if (app?.isAppContainerInitialized == true) {
            ViewModelFactory(app.appContainer)
        } else {
            null
        }
    )

    val uiState by editArticleViewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    // State for update confirmation dialog
    var showUpdateDialog by remember { mutableStateOf(false) }

    // Load article data when screen opens
    LaunchedEffect(articleId) {
        editArticleViewModel.loadArticle(articleId)
    }

    // Handle successful article update
    LaunchedEffect(uiState.articleUpdated) {
        if (uiState.articleUpdated) {
            scope.launch {
                delay(1000) // Show success message briefly
                onArticleUpdated()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Edit Article",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF1976D2) // Blue color
                    )
                }
            } else if (uiState.article != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    EditArticleForm(
                        uiState = uiState,
                        onTitleChange = editArticleViewModel::onTitleChange,
                        onContentChange = editArticleViewModel::onContentChange,
                        onCategoryChange = editArticleViewModel::onCategoryChange,
                        onDifficultyChange = editArticleViewModel::onDifficultyChange,
                        onDurationChange = editArticleViewModel::onDurationChange,
                        onXpChange = editArticleViewModel::onXpChange,
                        onImageUrlChange = editArticleViewModel::onImageUrlChange,
                        onUpdateArticleClick = { showUpdateDialog = true }
                    )
                }
            } else {
                // Article not found
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Artikel tidak ditemukan",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onNavigateBack) {
                            Text("Kembali")
                        }
                    }
                }
            }

            // Update confirmation dialog
            if (showUpdateDialog) {
                AlertDialog(
                    onDismissRequest = { showUpdateDialog = false },
                    title = {
                        Text(
                            text = "Perbarui Artikel",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Text(
                            text = "Apakah Anda yakin ingin memperbarui artikel ini?",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showUpdateDialog = false
                                editArticleViewModel.updateArticle()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1976D2) // Blue color
                            )
                        ) {
                            Text("Perbarui", color = Color.Black)
                        }
                    },
                    dismissButton = {
                        OutlinedButton(onClick = { showUpdateDialog = false }) {
                            Text("Batal")
                        }
                    }
                )
            }

            // Loading overlay for update
            if (uiState.isUpdating) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF1976D2) // Blue color
                    )
                }
            }

            // Success message
            if (uiState.articleUpdated) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.padding(32.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Success",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Artikel Berhasil Diperbarui!",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4CAF50)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Kembali ke Admin Panel...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
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
fun EditArticleForm(
    uiState: EditArticleUiState,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onDifficultyChange: (String) -> Unit,
    onDurationChange: (String) -> Unit,
    onXpChange: (String) -> Unit,
    onImageUrlChange: (String) -> Unit,
    onUpdateArticleClick: () -> Unit
) {
    val categories = listOf("Teknologi", "Sains", "Bisnis", "Seni", "Ilmiah", "Fantasi", "Sosial & Budaya", "Motivasi", "Sejarah", "Psikologi")
    val difficulties = listOf("Dasar", "Menengah", "Lanjut")

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title field
        OutlinedTextField(
            value = uiState.title,
            onValueChange = onTitleChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Judul Artikel") },
            placeholder = { Text("Masukkan judul artikel") },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            isError = uiState.titleError != null
        )

        if (uiState.titleError != null) {
            Text(
                text = uiState.titleError!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp)
            )
        }

        // Category dropdown
        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = uiState.category,
                onValueChange = { },
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                label = { Text("Kategori") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                shape = RoundedCornerShape(12.dp),
                isError = uiState.categoryError != null
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category) },
                        onClick = {
                            onCategoryChange(category)
                            expanded = false
                        }
                    )
                }
            }
        }

        if (uiState.categoryError != null) {
            Text(
                text = uiState.categoryError!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp)
            )
        }

        // Difficulty dropdown
        var difficultyExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = difficultyExpanded,
            onExpandedChange = { difficultyExpanded = !difficultyExpanded }
        ) {
            OutlinedTextField(
                value = uiState.difficulty,
                onValueChange = { },
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                label = { Text("Tingkat Kesulitan") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = difficultyExpanded)
                },
                shape = RoundedCornerShape(12.dp),
                isError = uiState.difficultyError != null
            )

            ExposedDropdownMenu(
                expanded = difficultyExpanded,
                onDismissRequest = { difficultyExpanded = false }
            ) {
                difficulties.forEach { difficulty ->
                    DropdownMenuItem(
                        text = { Text(difficulty) },
                        onClick = {
                            onDifficultyChange(difficulty)
                            difficultyExpanded = false
                        }
                    )
                }
            }
        }

        if (uiState.difficultyError != null) {
            Text(
                text = uiState.difficultyError!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp)
            )
        }

        // Duration and XP fields
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = uiState.duration,
                onValueChange = onDurationChange,
                modifier = Modifier.weight(1f),
                label = { Text("Durasi (menit)") },
                placeholder = { Text("5") },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                isError = uiState.durationError != null
            )

            OutlinedTextField(
                value = uiState.xp,
                onValueChange = onXpChange,
                modifier = Modifier.weight(1f),
                label = { Text("XP") },
                placeholder = { Text("15") },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                isError = uiState.xpError != null
            )
        }

        if (uiState.durationError != null) {
            Text(
                text = uiState.durationError!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp)
            )
        }

        if (uiState.xpError != null) {
            Text(
                text = uiState.xpError!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp)
            )
        }

        // Image URL field
        OutlinedTextField(
            value = uiState.imageUrl,
            onValueChange = onImageUrlChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("URL Gambar (Opsional)") },
            placeholder = { Text("https://example.com/image.jpg") },
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        // Content field
        OutlinedTextField(
            value = uiState.content,
            onValueChange = onContentChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            label = { Text("Konten Artikel") },
            placeholder = { Text("Masukkan isi artikel...") },
            shape = RoundedCornerShape(12.dp),
            maxLines = 10,
            isError = uiState.contentError != null
        )

        if (uiState.contentError != null) {
            Text(
                text = uiState.contentError!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Update Article button
        Button(
            onClick = onUpdateArticleClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFC107) // Gold color
            ),
            enabled = !uiState.isUpdating
        ) {
            if (uiState.isUpdating) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(
                    text = "Perbarui Artikel",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // General error message
        if (uiState.errorMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = uiState.errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun EditArticleScreenPreview() {
    ReadBoostTheme {
        EditArticleScreen(
            articleId = 1,
            onNavigateBack = {},
            onArticleUpdated = {}
        )
    }
}
