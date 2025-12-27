// File: presentation/screens/admin/AddArticleViewModel.kt
package com.readboost.id.presentation.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.readboost.id.data.model.Article
import com.readboost.id.domain.repository.ArticleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AddArticleUiState(
    val title: String = "",
    val content: String = "",
    val category: String = "",
    val difficulty: String = "",
    val duration: String = "",
    val xp: String = "",
    val imageUrl: String = "",
    val titleError: String? = null,
    val contentError: String? = null,
    val categoryError: String? = null,
    val difficultyError: String? = null,
    val durationError: String? = null,
    val xpError: String? = null,
    val errorMessage: String? = null,
    val isLoading: Boolean = false,
    val articleAdded: Boolean = false
)

class AddArticleViewModel(
    private val articleRepository: ArticleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddArticleUiState())
    val uiState: StateFlow<AddArticleUiState> = _uiState.asStateFlow()

    fun onTitleChange(title: String) {
        _uiState.value = _uiState.value.copy(
            title = title,
            titleError = null,
            errorMessage = null
        )
    }

    fun onContentChange(content: String) {
        _uiState.value = _uiState.value.copy(
            content = content,
            contentError = null,
            errorMessage = null
        )
    }

    fun onCategoryChange(category: String) {
        _uiState.value = _uiState.value.copy(
            category = category,
            categoryError = null,
            errorMessage = null
        )
    }

    fun onDifficultyChange(difficulty: String) {
        _uiState.value = _uiState.value.copy(
            difficulty = difficulty,
            difficultyError = null,
            errorMessage = null
        )
    }

    fun onDurationChange(duration: String) {
        val durationInt = duration.toIntOrNull()
        val calculatedXP = if (durationInt != null && durationInt > 0) durationInt * 100 else 0

        _uiState.value = _uiState.value.copy(
            duration = duration,
            xp = calculatedXP.toString(),
            durationError = null,
            xpError = null,
            errorMessage = null
        )
    }

    fun onXpChange(xp: String) {
        _uiState.value = _uiState.value.copy(
            xp = xp,
            xpError = null,
            errorMessage = null
        )
    }

    fun onImageUrlChange(imageUrl: String) {
        _uiState.value = _uiState.value.copy(
            imageUrl = imageUrl,
            errorMessage = null
        )
    }

    fun addArticle() {
        val currentState = _uiState.value

        // Validation
        var hasError = false
        var titleError: String? = null
        var contentError: String? = null
        var categoryError: String? = null
        var difficultyError: String? = null
        var durationError: String? = null
        var xpError: String? = null

        if (currentState.title.isBlank()) {
            titleError = "Judul artikel tidak boleh kosong"
            hasError = true
        }

        if (currentState.content.isBlank()) {
            contentError = "Konten artikel tidak boleh kosong"
            hasError = true
        }

        if (currentState.category.isBlank()) {
            categoryError = "Kategori harus dipilih"
            hasError = true
        }

        if (currentState.difficulty.isBlank()) {
            difficultyError = "Tingkat kesulitan harus dipilih"
            hasError = true
        }

        val durationInt = currentState.duration.toIntOrNull()
        if (currentState.duration.isBlank()) {
            durationError = "Durasi tidak boleh kosong"
            hasError = true
        } else if (durationInt == null || durationInt <= 0) {
            durationError = "Durasi harus berupa angka positif"
            hasError = true
        }

        // XP is calculated automatically, no validation needed

        if (hasError) {
            _uiState.value = currentState.copy(
                titleError = titleError,
                contentError = contentError,
                categoryError = categoryError,
                difficultyError = difficultyError,
                durationError = durationError
            )
            return
        }

        // Start adding article
        _uiState.value = currentState.copy(
            isLoading = true,
            errorMessage = null
        )

        viewModelScope.launch {
            try {
                val xpInt = currentState.xp.toIntOrNull() ?: 0
                val article = Article(
                    title = currentState.title,
                    content = currentState.content,
                    category = currentState.category,
                    difficulty = currentState.difficulty,
                    duration = durationInt!!,
                    xp = xpInt,
                    imageUrl = currentState.imageUrl.takeIf { it.isNotBlank() }
                )

                articleRepository.insertArticle(article)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    articleAdded = true,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Gagal menambahkan artikel: ${e.message}"
                )
            }
        }
    }
}
