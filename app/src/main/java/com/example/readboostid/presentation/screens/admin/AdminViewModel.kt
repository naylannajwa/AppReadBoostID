// File: presentation/screens/admin/AdminViewModel.kt
package com.readboost.id.presentation.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.readboost.id.data.model.Article
import com.readboost.id.domain.repository.ArticleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class AdminUiState(
    val articles: List<Article> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class AdminViewModel(
    private val articleRepository: ArticleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init {
        loadArticles()
    }

    private fun loadArticles() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null)
            try {
                articleRepository.getAllArticles().collectLatest { articles ->
                    _uiState.value = _uiState.value.copy(
                        articles = articles,
                        isLoading = false
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

    fun deleteArticle(articleId: Int) {
        viewModelScope.launch {
            try {
                articleRepository.deleteArticleById(articleId)
                val updatedArticles = _uiState.value.articles.filterNot { it.id == articleId }
                _uiState.value = _uiState.value.copy(
                    articles = updatedArticles,
                    successMessage = "Artikel berhasil dihapus",
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Gagal menghapus artikel: ${e.message}",
                    successMessage = null
                )
            }
        }
    }

    fun refreshArticles() {
        loadArticles()
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            successMessage = null,
            errorMessage = null
        )
    }
}
