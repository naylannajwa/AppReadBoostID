package com.example.readboostid.presentation.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import com.readboost.id.presentation.screens.admin.AddArticleUiState
import com.readboost.id.presentation.screens.admin.AddArticleViewModel
import com.readboost.id.presentation.viewmodel.ViewModelFactory
import com.readboost.id.ui.theme.ReadBoostTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddArticleScreen(
    onNavigateBack: () -> Unit,
    onArticleAdded: () -> Unit = {}
) {
    val context = LocalContext.current
    val app = context.applicationContext as? ReadBoostApplication

    val addArticleViewModel = viewModel<AddArticleViewModel>(
        factory = if (app?.isAppContainerInitialized == true) {
            ViewModelFactory(app.appContainer)
        } else {
            null
        }
    )

    val uiState by addArticleViewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    // State for add confirmation dialog
    var showAddDialog by remember { mutableStateOf(false) }

    // Handle successful article addition
    LaunchedEffect(uiState.articleAdded) {
        if (uiState.articleAdded) {
            scope.launch {
                delay(1000) // Show success message briefly
                onArticleAdded()
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    text = "Tambah Artikel",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Apakah Anda yakin ingin menambahkan artikel ini?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showAddDialog = false
                        addArticleViewModel.addArticle()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50) // Green color
                    )
                ) {
                    Text("Tambah", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Tambah Artikel Baru",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                AddArticleForm(
                    uiState = uiState,
                    onTitleChange = addArticleViewModel::onTitleChange,
                    onContentChange = addArticleViewModel::onContentChange,
                    onCategoryChange = addArticleViewModel::onCategoryChange,
                    onDifficultyChange = addArticleViewModel::onDifficultyChange,
                    onDurationChange = addArticleViewModel::onDurationChange,
                    onImageUrlChange = addArticleViewModel::onImageUrlChange,
                    onAddArticleClick = { showAddDialog = true }
                )
            }

            // Loading overlay
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color.White
                    )
                }
            }

            // Success message
            if (uiState.articleAdded) {
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
                                contentDescription = "Sukses",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Artikel Berhasil Ditambahkan!",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4CAF50)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Kembali ke Panel Admin...",
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
fun AddArticleForm(
    uiState: AddArticleUiState,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onDifficultyChange: (String) -> Unit,
    onDurationChange: (String) -> Unit,
    onImageUrlChange: (String) -> Unit,
    onAddArticleClick: () -> Unit
) {
    val categories = listOf("Teknologi", "Sains", "Psikologi", "Sejarah", "Motivasi", "Sosial Budaya")
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
                text = uiState.titleError.orEmpty(),
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
                text = uiState.categoryError.orEmpty(),
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
                text = uiState.difficultyError.orEmpty(),
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
                onValueChange = {}, // Disabled - XP calculated automatically
                modifier = Modifier.weight(1f),
                label = { Text("XP (Otomatis)") },
                placeholder = { Text("500") },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                enabled = false, // Make it read-only
                isError = uiState.xpError != null
            )
        }

        if (uiState.durationError != null) {
            Text(
                text = uiState.durationError.orEmpty(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp)
            )
        }

        if (uiState.xpError != null) {
            Text(
                text = uiState.xpError.orEmpty(),
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
                text = uiState.contentError.orEmpty(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Add Article button
        Button(
            onClick = onAddArticleClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1976D2) // Blue color
            ),
            enabled = !uiState.isLoading
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(
                    text = "Tambah Artikel",
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
                        text = uiState.errorMessage.orEmpty(),
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
fun AddArticleScreenPreview() {
    ReadBoostTheme {
        AddArticleScreen(
            onNavigateBack = {},
            onArticleAdded = {}
        )
    }
}
