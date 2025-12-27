// File: presentation/screens/article/ArticleDetailViewModel.kt
// Updated for Firestore integration
package com.readboost.id.presentation.screens.article

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.readboost.id.data.model.Article
import com.readboost.id.data.model.Notes
import com.readboost.id.data.model.ReadingSession
import com.readboost.id.domain.repository.ArticleRepository
import com.readboost.id.domain.repository.FirestoreLeaderboardRepository
import com.readboost.id.domain.repository.UserDataRepository
import kotlinx.coroutines.flow.*
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
    private val firestoreRepository: FirestoreLeaderboardRepository,
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
        viewModelScope.launch {
            try {
                // Start Firestore reading session
                val userId = "current_user_id" // TODO: Get from FirebaseAuth
                currentSessionId = "${userId}_${articleId}_${System.currentTimeMillis()}"
                firestoreRepository.startReadingSession(userId, articleId)
                lastActiveTime = System.currentTimeMillis()

                _uiState.update { state ->
                    val session = state.readingSession
                    if (session != null) {
                        session.isPaused = false
                    }
                    state.copy(readingSession = session)
                }
            } catch (e: Exception) {
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

            // Update Firestore session if active
            currentSessionId?.let { sessionId ->
                viewModelScope.launch {
                    try {
                        firestoreRepository.updateReadingSession(sessionId, activeTimeIncrement)
                    } catch (e: Exception) {
                        // Handle error silently
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
        viewModelScope.launch {
            val article = _uiState.value.article ?: return@launch
            val session = _uiState.value.readingSession ?: return@launch

            try {
                // End Firestore session and award XP if target met
                val userId = "current_user_id" // TODO: Get from FirebaseAuth
                val userProgress = firestoreRepository.getUserProgress(userId)

                val targetMinutes = userProgress?.dailyTarget ?: 5
                val xpEarned = if (activeReadingTime >= targetMinutes * 60 * 1000) article.xp else 0

                currentSessionId?.let { sessionId ->
                    firestoreRepository.endReadingSession(sessionId, xpEarned)
                }

                if (xpEarned > 0) {
                    firestoreRepository.updateUserXP(userId, "Current User", xpEarned)
                }

                session.isCompleted = true
                _uiState.update { state ->
                    state.copy(readingSession = session)
                }
            } catch (e: Exception) {
                // Fallback to local database
                val progress = userDataRepository.getUserProgressOnce()
                if (progress != null && activeReadingTime >= progress.dailyTarget * 60 * 1000) {
                    userDataRepository.completeReadingSession(article.xp)
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
                // Add XP for creating note
                userDataRepository.addXP(5)
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
