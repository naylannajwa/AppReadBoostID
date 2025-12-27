// File: presentation/screens/article/ArticleDetailScreen.kt (REVISED FOR IMMERSIVE READING)
package com.readboost.id.presentation.screens.article

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.readboost.id.ReadBoostApplication
import com.readboost.id.data.model.Article
import com.readboost.id.data.model.Notes
import com.readboost.id.presentation.viewmodel.ArticleDetailViewModelFactory
import com.readboost.id.ui.theme.ReadBoostTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    val coroutineScope = rememberCoroutineScope()

    // Reading state
    var timerSeconds by remember { mutableStateOf(0) }
    var isReading by remember { mutableStateOf(false) }
    var showNotesPanel by remember { mutableStateOf(false) }
    var showControls by remember { mutableStateOf(true) }
    var controlsVisible by remember { mutableStateOf(true) }

    val listState = rememberLazyListState()

    // Auto-hide controls after 3 seconds of inactivity
    LaunchedEffect(controlsVisible) {
        if (controlsVisible) {
            delay(3000)
            controlsVisible = false
        }
    }

    // Reading timer
    LaunchedEffect(isReading, uiState.readingSession?.isPaused) {
        if (isReading && uiState.readingSession?.isPaused == false) {
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

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.screenWidthDp > 600

    // Immersive reading layout with proper layering
    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState.isLoading || uiState.article == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val article = uiState.article!!

            // Main reading area with proper content padding
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                controlsVisible = !controlsVisible
                            }
                        )
                        }
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = if (controlsVisible) 100.dp else 24.dp, // Adaptive top padding
                        bottom = if (controlsVisible) 72.dp else 24.dp, // Adaptive bottom padding
                        start = 20.dp,
                        end = 20.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Article title with elegant spacing
                    item {
                        Text(
                            text = article.title,
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontSize = if (isLandscape) 32.sp else 28.sp,
                                lineHeight = if (isLandscape) 38.sp else 34.sp
                            ),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    // Article metadata
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AssistChip(
                                onClick = { },
                                label = {
                                    Text(
                                        article.category,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Schedule,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                                Text(
                                    "${article.duration} min",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    "+${article.xp} XP",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    }

                    // Article content with optimal typography
                    item {
                        Text(
                            text = article.content,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = if (isLandscape) 20.sp else 18.sp,
                                lineHeight = if (isLandscape) 32.sp else 28.sp,
                                letterSpacing = 0.2.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                            textAlign = TextAlign.Justify,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    // Reading completion status
                    item {
                        if (uiState.readingSession?.isCompleted == true) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                        Text(
                                            "Artikel selesai dibaca!",
                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                            Text(
                                            "+${article.xp} XP earned",
                                style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Overlay layers with proper z-index management

            // Top progress overlay
            if (controlsVisible) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .zIndex(10f)
                ) {
                    ReadingProgressOverlay(
                        elapsedSeconds = timerSeconds,
                        targetMinutes = article.duration,
                        isReading = isReading,
                        onToggleReading = {
                            isReading = !isReading
                            if (isReading) {
                                viewModel.startReading()
                            } else {
                                viewModel.pauseReading()
                            }
                        }
                    )
                }
            }

            // Bottom controls overlay
            if (controlsVisible) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .zIndex(10f)
                ) {
                    ReadingControlsOverlay(
                        onBack = onNavigateBack,
                        onToggleNotes = { showNotesPanel = !showNotesPanel },
                        notesCount = uiState.notes.size
                    )
                }
            }

            // Notes panel overlay (slides from right)
            if (showNotesPanel) {
                val panelWidth = if (isLandscape) 400.dp else configuration.screenWidthDp.dp * 0.85f
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .zIndex(20f)
                ) {
                    NotesPanel(
                        notes = uiState.notes,
                        onAddNote = { viewModel.showNoteDialog() },
                        onEditNote = { viewModel.showNoteDialog(it) },
                        onDeleteNote = { viewModel.deleteNote(it) },
                        onClose = { showNotesPanel = false },
                        modifier = Modifier
                            .width(panelWidth)
                            .fillMaxHeight()
                    )
                }
            }

            // Note dialog overlay (highest z-index)
            if (uiState.showNoteDialog) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(30f),
                    contentAlignment = Alignment.Center
                ) {
                NoteDialog(
                    initialContent = uiState.editingNote?.content ?: "",
                    onDismiss = { viewModel.hideNoteDialog() },
                    onSave = { content -> viewModel.saveNote(content) }
                )
                }
            }
        }
    }
}

@Composable
fun ReadingProgressOverlay(
    elapsedSeconds: Int,
    targetMinutes: Int,
    isReading: Boolean,
    onToggleReading: () -> Unit
) {
    val minutes = elapsedSeconds / 60
    val seconds = elapsedSeconds % 60
    val progress = (elapsedSeconds.toFloat() / (targetMinutes * 60)).coerceIn(0f, 1f)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Reading progress section
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Timer display
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = String.format("%02d:%02d", minutes, seconds),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${targetMinutes} min target",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                // Progress bar
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                        .width(100.dp)
                        .height(4.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            // XP indicator
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.height(32.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
            Text(
                        text = "+XP",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Play/Pause button
            FilledIconButton(
                onClick = onToggleReading,
                modifier = Modifier.size(44.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = if (isReading)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    if (isReading) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isReading) "Pause reading" else "Start reading",
                    tint = if (isReading)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun ReadingControlsOverlay(
    onBack: () -> Unit,
    onToggleNotes: () -> Unit,
    notesCount: Int
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back to articles",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Notes button with badge
            Box(modifier = Modifier.size(44.dp)) {
                IconButton(
                    onClick = onToggleNotes,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        Icons.Default.Note,
                        contentDescription = "Toggle notes panel",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                }

                if (notesCount > 0) {
                    Badge(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 6.dp, y = (-6).dp),
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            text = notesCount.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NotesPanel(
    notes: List<Notes>,
    onAddNote: () -> Unit,
    onEditNote: (Notes) -> Unit,
    onDeleteNote: (Notes) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxHeight(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Catatan",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(onClick = onClose) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close notes panel",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            // Add note button
            FilledTonalButton(
                onClick = onAddNote,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Tambah Catatan")
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Notes content
            if (notes.isEmpty()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        modifier = Modifier.size(80.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                Icons.Default.Note,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Belum ada catatan",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Tekan tombol \"Tambah Catatan\" untuk mulai membuat catatan",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(notes) { note ->
                        NoteCard(
                            note = note,
                            onEdit = { onEditNote(note) },
                            onDelete = { onDeleteNote(note) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NoteCard(
    note: Notes,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tap untuk edit",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.weight(1f))

                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(36.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit note",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete note",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
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
    showSystemUi = false,
    name = "Article Detail - Clean Reading"
)
@Composable
fun ArticleDetailScreenPreview() {
    ReadBoostTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            // Main content
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = 24.dp,
                    bottom = 24.dp,
                    start = 20.dp,
                    end = 20.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "Memahami Kecerdasan Buatan (AI)",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 28.sp,
                            lineHeight = 34.sp
                        ),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AssistChip(
                            onClick = { },
                            label = {
                        Text(
                                    "Teknologi",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("5 min", style = MaterialTheme.typography.labelMedium)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Star,
                                null,
                                Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(4.dp))
                        Text(
                                "+15 XP",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        }
                    }
                }

                item {
                    Text(
                        text = "Kecerdasan Buatan atau Artificial Intelligence (AI) adalah simulasi kecerdasan manusia yang diprogram dalam mesin. AI memungkinkan komputer untuk belajar dari pengalaman, menyesuaikan dengan input baru, dan melakukan tugas-tugas yang biasanya memerlukan kecerdasan manusia.\n\nJenis-jenis AI meliputi Machine Learning, Deep Learning, dan Neural Networks. Machine Learning adalah subset dari AI yang memungkinkan sistem untuk belajar dari data tanpa diprogram secara eksplisit.",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 18.sp,
                            lineHeight = 28.sp,
                            letterSpacing = 0.2.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                        textAlign = TextAlign.Justify,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }

            // Top progress overlay (visible)
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .zIndex(10f)
            ) {
                ReadingProgressOverlay(
                    elapsedSeconds = 180,
                    targetMinutes = 5,
                    isReading = true,
                    onToggleReading = {}
                )
            }

            // Bottom controls overlay (visible)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .zIndex(10f)
            ) {
                ReadingControlsOverlay(
                    onBack = {},
                    onToggleNotes = {},
                    notesCount = 3
                )
            }
        }
    }
}

@Preview(
    showBackground = true,
    name = "Notes Panel - With Notes",
    widthDp = 360,
    heightDp = 640
)
@Composable
fun NotesPanelPreview() {
    ReadBoostTheme {
        val sampleNotes = listOf(
            Notes(
                articleId = 1,
                content = "AI adalah simulasi kecerdasan manusia dalam mesin yang dapat belajar dari pengalaman"
            ),
            Notes(
                articleId = 1,
                content = "Machine Learning memungkinkan sistem untuk belajar dari data tanpa diprogram secara eksplisit"
            ),
            Notes(
                articleId = 1,
                content = "Deep Learning menggunakan neural networks untuk memproses data yang kompleks"
            )
        )

        NotesPanel(
            notes = sampleNotes,
            onAddNote = {},
            onEditNote = {},
            onDeleteNote = {},
            onClose = {},
            modifier = Modifier.fillMaxHeight()
        )
    }
}

@Preview(showBackground = true, name = "Notes Panel - Empty", widthDp = 360, heightDp = 640)
@Composable
fun NotesPanelEmptyPreview() {
    ReadBoostTheme {
        NotesPanel(
            notes = emptyList(),
            onAddNote = {},
            onEditNote = {},
            onDeleteNote = {},
            onClose = {},
            modifier = Modifier.fillMaxHeight()
        )
    }
}

@Preview(showBackground = true, name = "Note Dialog")
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
