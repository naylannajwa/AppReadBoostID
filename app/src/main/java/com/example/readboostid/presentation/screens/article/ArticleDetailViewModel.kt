// File: presentation/screens/article/ArticleDetailViewModel.kt
// Updated for Firestore integration
package com.readboost.id.presentation.screens.article

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.readboost.id.data.model.Article
import com.readboost.id.data.model.Notes
import com.readboost.id.data.model.ReadingSession
import com.readboost.id.data.service.HybridDataManager
import com.readboost.id.domain.repository.ArticleRepository
import com.readboost.id.domain.repository.UserDataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ArticleDetailUiState(
    val article: Article? = null,
    val notes: List<Notes> = emptyList(),
    val readingSession: ReadingSession? = null,
    val isLoading: Boolean = true,
    val showNoteDialog: Boolean = false,
    val editingNote: Notes? = null
)

class ArticleDetailViewModel(
    private val articleRepository: ArticleRepository,
    private val userDataRepository: UserDataRepository,
    private val hybridDataManager: HybridDataManager,
    private val articleId: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArticleDetailUiState())
    val uiState: StateFlow<ArticleDetailUiState> = _uiState.asStateFlow()

    // Firestore session tracking
    private var currentSessionId: String? = null
    private var activeReadingTime: Long = 0L
    private var lastActiveTime: Long = 0L

    init {
        loadArticle()
        loadNotes()
    }

    private fun loadArticle() {
        viewModelScope.launch {
            val article = articleRepository.getArticleById(articleId)
            _uiState.update {
                it.copy(
                    article = article,
                    isLoading = false,
                    readingSession = article?.let { art -> ReadingSession(art.id) }
                )
            }
        }
    }

    private fun loadNotes() {
        viewModelScope.launch {
            userDataRepository.getNotesByArticle(articleId)
                .collect { notes ->
                    _uiState.update { it.copy(notes = notes) }
                }
        }
    }

    fun startReading() {
        println("ArticleDetailViewModel: startReading() called for articleId: $articleId")
        viewModelScope.launch {
            try {
                // Start Firestore reading session through hybrid manager
                println("ArticleDetailViewModel: Starting reading session for article: $articleId")
                currentSessionId = hybridDataManager.startReadingSession(articleId)
                lastActiveTime = System.currentTimeMillis()
                println("ArticleDetailViewModel: Session started with ID: $currentSessionId")

                _uiState.update { state ->
                    val session = state.readingSession
                    if (session != null) {
                        session.isPaused = false
                        println("ArticleDetailViewModel: Reading session unpaused")
                    }
                    state.copy(readingSession = session)
                }
            } catch (e: Exception) {
                println("ArticleDetailViewModel: Error starting reading session: ${e.message}")
                e.printStackTrace()
                // Fallback to local session
                _uiState.update { state ->
                    val session = state.readingSession
                    if (session != null) {
                        session.isPaused = false
                    }
                    state.copy(readingSession = session)
                }
            }
        }
    }

    fun updateReadingTime(activeTimeIncrement: Long) {
        if (activeTimeIncrement > 0) {
            activeReadingTime += activeTimeIncrement
            lastActiveTime = System.currentTimeMillis()

            println("ArticleDetailViewModel: Updated reading time by ${activeTimeIncrement}ms, total active time: ${activeReadingTime}ms")

            // Update Firestore session if active
            currentSessionId?.let { sessionId ->
                viewModelScope.launch {
                    try {
                        hybridDataManager.updateReadingSession(sessionId, activeTimeIncrement)
                        println("ArticleDetailViewModel: Updated Firestore session: $sessionId")
                    } catch (e: Exception) {
                        println("ArticleDetailViewModel: Error updating Firestore session: ${e.message}")
                    }
                }
            }

            _uiState.update { state ->
                val session = state.readingSession
                if (session != null) {
                    session.elapsedTime = activeReadingTime.toInt()
                }
                state.copy(readingSession = session)
            }
        }
    }

    fun pauseReading() {
        _uiState.update { state ->
            val session = state.readingSession
            if (session != null) {
                session.isPaused = true
            }
            state.copy(readingSession = session)
        }
    }

    fun completeReading() {
        println("ArticleDetailViewModel: completeReading() called - activeTime: ${activeReadingTime}ms")
        viewModelScope.launch {
            val article = _uiState.value.article ?: return@launch
            val session = _uiState.value.readingSession ?: return@launch

            try {
                // Calculate XP earned based on reading time vs target
                val userProgress = hybridDataManager.getUserProgress()
                val targetMinutes = userProgress?.dailyTarget ?: 5
                val targetMs = targetMinutes * 60 * 1000L
                val readingMinutes = activeReadingTime / (1000.0 * 60.0)

                println("ArticleDetailViewModel: Article completion calculation:")
                println("  - Article XP: ${article.xp}")
                println("  - Target time: ${targetMinutes} min (${targetMs}ms)")
                println("  - Actual reading time: ${readingMinutes} min (${activeReadingTime}ms)")
                println("  - Current streak: ${userProgress?.streakDays ?: 0}")

                // Award XP based on reading time and article complexity (Fair system)
                val baseXP = article.xp

                // Time bonus: more XP for longer reading
                val timeBonus = when {
                    readingMinutes >= 10 -> 20  // Long article bonus
                    readingMinutes >= 5 -> 10   // Medium article bonus
                    readingMinutes >= 2 -> 5    // Short article bonus
                    else -> 0
                }

                // Complexity bonus based on article category
                val complexityBonus = when (article.category.lowercase()) {
                    "teknologi", "sains", "programmer", "matematika" -> 15  // Complex topics
                    "bisnis", "ekonomi", "politik", "kesehatan" -> 10       // Moderate complexity
                    else -> 5                                                // General topics
                }

                // Total XP with cap at 500 (admin limit)
                val totalXPEarned = (baseXP + timeBonus + complexityBonus).coerceAtMost(500)
                val xpEarned = maxOf(totalXPEarned, 5) // Minimum 5 XP

                println("  - Article XP: $baseXP, Time bonus: $timeBonus (${readingMinutes}min), Complexity bonus: $complexityBonus (${article.category})")
                println("  - Total XP earned: $xpEarned (capped at 500)")

                println("ArticleDetailViewModel: Final XP earned: $xpEarned")

                // Use hybrid manager to handle both Room and Firestore updates
                hybridDataManager.onArticleCompleted(articleId, xpEarned, activeReadingTime.toInt())
                println("ArticleDetailViewModel: Article completion processed through hybrid manager")

                // End reading session in Firestore
                currentSessionId?.let { sessionId ->
                    try {
                        hybridDataManager.endReadingSession(sessionId, xpEarned)
                        println("ArticleDetailViewModel: Firestore session ended: $sessionId")
                    } catch (e: Exception) {
                        println("ArticleDetailViewModel: Error ending Firestore session: ${e.message}")
                    }
                }

                session.isCompleted = true
                _uiState.update { state ->
                    state.copy(readingSession = session)
                }

            } catch (e: Exception) {
                println("ArticleDetailViewModel: Error completing reading: ${e.message}")
                e.printStackTrace()
                // Fallback to local database only
                try {
                    val progress = hybridDataManager.getUserProgress()
                    if (progress != null && activeReadingTime >= progress.dailyTarget * 60 * 1000) {
                        // Award XP locally through hybrid manager
                        hybridDataManager.onArticleCompleted(articleId, article.xp, activeReadingTime.toInt())
                        println("ArticleDetailViewModel: Fallback - XP awarded locally")
                    }
                } catch (localException: Exception) {
                    println("ArticleDetailViewModel: Error in fallback: ${localException.message}")
                }

                session.isCompleted = true
                _uiState.update { state ->
                    state.copy(readingSession = session)
                }
            }
        }
    }

    fun showNoteDialog(note: Notes? = null) {
        _uiState.update { it.copy(showNoteDialog = true, editingNote = note) }
    }

    fun hideNoteDialog() {
        _uiState.update { it.copy(showNoteDialog = false, editingNote = null) }
    }

    fun saveNote(content: String) {
        viewModelScope.launch {
            val editingNote = _uiState.value.editingNote

            if (editingNote != null) {
                // Update existing note
                userDataRepository.updateNote(editingNote.copy(content = content))
            } else {
                // Create new note
                val newNote = Notes(
                    articleId = articleId,
                    content = content
                )
                userDataRepository.insertNote(newNote)
                // Add XP for creating note (through hybrid manager)
                try {
                    hybridDataManager.onArticleCompleted(articleId, 5, 0) // 5 XP for note
                } catch (e: Exception) {
                    // If hybrid fails, add locally
                    // Note: This could be improved
                }
            }

            hideNoteDialog()
        }
    }

    fun deleteNote(note: Notes) {
        viewModelScope.launch {
            userDataRepository.deleteNote(note)
        }
    }
}
