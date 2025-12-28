// File: presentation/screens/admin/EditArticleViewModel.kt
package com.readboost.id.presentation.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.readboost.id.data.model.Article
import com.readboost.id.domain.repository.ArticleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EditArticleUiState(
    val article: Article? = null,
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
    val errorMessage: String? = null,
    val isLoading: Boolean = false,
    val isUpdating: Boolean = false,
    val articleUpdated: Boolean = false
)

class EditArticleViewModel(
    private val articleRepository: ArticleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditArticleUiState())
    val uiState: StateFlow<EditArticleUiState> = _uiState.asStateFlow()

    fun loadArticle(articleId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val article = articleRepository.getArticleById(articleId)
                if (article != null) {
                    _uiState.value = _uiState.value.copy(
                        article = article,
                        title = article.title,
                        content = article.content,
                        category = article.category,
                        difficulty = article.difficulty,
                        duration = article.duration.toString(),
                        xp = article.xp.toString(),
                        imageUrl = article.imageUrl ?: "",
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Artikel tidak ditemukan"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Gagal memuat artikel: ${e.message}"
                )
            }
        }
    }

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
            errorMessage = null
        )
    }

    fun onXpChange(xp: String) {
        _uiState.value = _uiState.value.copy(
            xp = xp,
            errorMessage = null
        )
    }

    fun onImageUrlChange(imageUrl: String) {
        _uiState.value = _uiState.value.copy(
            imageUrl = imageUrl,
            errorMessage = null
        )
    }

    fun updateArticle() {
        val currentState = _uiState.value
        val article = currentState.article ?: return

        // Validation
        var hasError = false
        var titleError: String? = null
        var contentError: String? = null
        var categoryError: String? = null
        var difficultyError: String? = null
        var durationError: String? = null

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

        // XP is now auto-calculated, no validation needed

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

        // Start updating article
        _uiState.value = currentState.copy(
            isUpdating = true,
            errorMessage = null
        )

        viewModelScope.launch {
            try {
                val updatedArticle = article.copy(
                    title = currentState.title,
                    content = currentState.content,
                    category = currentState.category,
                    difficulty = currentState.difficulty,
                    duration = durationInt!!,
                    xp = currentState.xp.toIntOrNull() ?: 0,
                    imageUrl = currentState.imageUrl.takeIf { it.isNotBlank() }
                )

                articleRepository.updateArticle(updatedArticle)

                _uiState.value = _uiState.value.copy(
                    isUpdating = false,
                    articleUpdated = true,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isUpdating = false,
                    errorMessage = "Gagal memperbarui artikel: ${e.message}"
                )
            }
        }
    }
}
