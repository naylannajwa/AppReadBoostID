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
    val errorMessage: String? = null
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
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
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
                    errorMessage = "Failed to load articles: ${e.message}"
                )
            }
        }
    }

    fun deleteArticle(articleId: Int) {
        viewModelScope.launch {
            try {
                // Get article first, then delete it
                val article = articleRepository.getArticleById(articleId)
                if (article != null) {
                    articleRepository.deleteArticle(article)
                    // Reload articles after deletion
                    loadArticles()
                } else {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Article not found"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to delete article: ${e.message}"
                )
            }
        }
    }

    fun refreshArticles() {
        loadArticles()
    }
}
