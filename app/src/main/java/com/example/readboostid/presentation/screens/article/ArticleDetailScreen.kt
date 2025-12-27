// File: presentation/screens/article/ArticleDetailScreen.kt (UPDATED)
package com.readboost.id.presentation.screens.article

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.readboost.id.data.model.Article
import com.readboost.id.data.model.Notes
import com.readboost.id.presentation.viewmodel.ArticleDetailViewModelFactory
import com.readboost.id.ui.theme.ReadBoostTheme
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleDetailScreen(
    articleId: Int,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as ReadBoostApplication
    val viewModel: ArticleDetailViewModel = viewModel(
        factory = ArticleDetailViewModelFactory(
            app.appContainer,
            articleId
        )
    )

    val uiState by viewModel.uiState.collectAsState()

    var timerSeconds by remember { mutableStateOf(0) }
    var isScrolling by remember { mutableStateOf(false) }

    LaunchedEffect(isScrolling, uiState.readingSession?.isPaused) {
        if (isScrolling && uiState.readingSession?.isPaused == false) {
            while (true) {
                delay(1000L)
                timerSeconds++
                viewModel.updateReadingTime(timerSeconds)

                val targetSeconds = (uiState.article?.duration ?: 0) * 60
                if (timerSeconds >= targetSeconds) {
                    viewModel.completeReading()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Baca Artikel") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showNoteDialog() }
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Add Note")
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading || uiState.article == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val article = uiState.article!!

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                ReadingTimerCard(
                    elapsedSeconds = timerSeconds,
                    targetMinutes = article.duration,
                    onStartPause = {
                        isScrolling = !isScrolling
                        if (isScrolling) {
                            viewModel.startReading()
                        } else {
                            viewModel.pauseReading()
                        }
                    },
                    isRunning = isScrolling
                )

                Divider()

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = article.title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            AssistChip(
                                onClick = { },
                                label = { Text(article.category) }
                            )
                            AssistChip(
                                onClick = { },
                                label = { Text("${article.duration} min") }
                            )
                            AssistChip(
                                onClick = { },
                                label = { Text("+${article.xp} XP") }
                            )
                        }
                    }

                    item {
                        Divider()
                    }

                    item {
                        Text(
                            text = article.content,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    item {
                        Divider()
                    }

                    item {
                        Text(
                            text = "Catatan Kamu",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (uiState.notes.isEmpty()) {
                        item {
                            Text(
                                text = "Belum ada catatan. Tap tombol + untuk menambah catatan.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    } else {
                        items(uiState.notes) { note ->
                            NoteItem(
                                note = note.content,
                                onEdit = { viewModel.showNoteDialog(note) },
                                onDelete = { viewModel.deleteNote(note) }
                            )
                        }
                    }
                }
            }

            if (uiState.showNoteDialog) {
                NoteDialog(
                    initialContent = uiState.editingNote?.content ?: "",
                    onDismiss = { viewModel.hideNoteDialog() },
                    onSave = { content -> viewModel.saveNote(content) }
                )
            }
        }
    }
}

@Composable
fun ReadingTimerCard(
    elapsedSeconds: Int,
    targetMinutes: Int,
    onStartPause: () -> Unit,
    isRunning: Boolean
) {
    val minutes = elapsedSeconds / 60
    val seconds = elapsedSeconds % 60
    val progress = (elapsedSeconds.toFloat() / (targetMinutes * 60)).coerceIn(0f, 1f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Waktu Membaca",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = String.format("%02d:%02d", minutes, seconds),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Target: $targetMinutes menit",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onStartPause) {
                Text(if (isRunning) "⏸ Pause" else "▶ Mulai Membaca")
            }
        }
    }
}

@Composable
fun NoteItem(
    note: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = note,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun NoteDialog(
    initialContent: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var noteContent by remember { mutableStateOf(initialContent) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Catatan") },
        text = {
            TextField(
                value = noteContent,
                onValueChange = { noteContent = it },
                label = { Text("Tulis catatan...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(noteContent) },
                enabled = noteContent.isNotBlank()
            ) {
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
    name = "Article Detail Screen - Full"
)
@Composable
fun ArticleDetailScreenPreview() {
    ReadBoostTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Detail Artikel") },
                    navigationIcon = {
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.Note, contentDescription = "Notes")
                        }
                    }
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
                    Text(
                        text = "Memahami Kecerdasan Buatan (AI)",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Teknologi",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text("•")
                        Text(
                            text = "5 min",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text("•")
                        Text(
                            text = "+15 XP",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                item {
                    Text(
                        text = "Kecerdasan Buatan atau Artificial Intelligence (AI) adalah simulasi kecerdasan manusia yang diprogram dalam mesin. AI memungkinkan komputer untuk belajar dari pengalaman, menyesuaikan dengan input baru, dan melakukan tugas-tugas yang biasanya memerlukan kecerdasan manusia.\n\nJenis-jenis AI meliputi Machine Learning, Deep Learning, dan Neural Networks. Machine Learning adalah subset dari AI yang memungkinkan sistem untuk belajar dari data tanpa diprogram secara eksplisit.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NoteDialogPreview() {
    ReadBoostTheme {
        NoteDialog(
            initialContent = "Contoh catatan yang sudah ditulis sebelumnya...",
            onDismiss = {},
            onSave = {}
        )
    }
}