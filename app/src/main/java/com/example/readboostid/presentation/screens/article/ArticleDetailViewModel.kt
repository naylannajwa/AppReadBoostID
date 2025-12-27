// File: presentation/screens/article/ArticleDetailViewModel.kt
package com.readboost.id.presentation.screens.article

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.readboost.id.data.model.Article
import com.readboost.id.data.model.Notes
import com.readboost.id.data.model.ReadingSession
import com.readboost.id.domain.repository.ArticleRepository
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
    private val articleId: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArticleDetailUiState())
    val uiState: StateFlow<ArticleDetailUiState> = _uiState.asStateFlow()

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
        _uiState.update { state ->
            state.copy(
                readingSession = state.readingSession?.copy(
                    isPaused = false,
                    startTime = System.currentTimeMillis()
                )
            )
        }
    }

    fun pauseReading() {
        _uiState.update { state ->
            state.copy(
                readingSession = state.readingSession?.copy(isPaused = true)
            )
        }
    }

    fun updateReadingTime(seconds: Int) {
        _uiState.update { state ->
            state.copy(
                readingSession = state.readingSession?.copy(elapsedTime = seconds)
            )
        }
    }

    fun completeReading() {
        viewModelScope.launch {
            val article = _uiState.value.article ?: return@launch
            val session = _uiState.value.readingSession ?: return@launch

            // Check if daily target is met
            val progress = userDataRepository.getUserProgressOnce()
            if (progress != null && session.elapsedTime >= progress.dailyTarget * 60) {
                userDataRepository.completeReadingSession(article.xp)
            }

            _uiState.update { state ->
                state.copy(
                    readingSession = session.copy(isCompleted = true)
                )
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
