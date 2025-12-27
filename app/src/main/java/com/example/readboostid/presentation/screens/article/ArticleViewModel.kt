// File: presentation/screens/article/ArticleViewModel.kt
package com.readboost.id.presentation.screens.article

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.readboost.id.data.model.Article
import com.readboost.id.domain.repository.ArticleRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ArticleListUiState(
    val articles: List<Article> = emptyList(),
    val selectedCategory: String = "All",
    val selectedDuration: String = "All",
    val isLoading: Boolean = true
)

class ArticleViewModel(
    private val articleRepository: ArticleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArticleListUiState())
    val uiState: StateFlow<ArticleListUiState> = _uiState.asStateFlow()

    init {
        loadArticles()
    }

    private fun loadArticles() {
        viewModelScope.launch {
            articleRepository.getAllArticles()
                .collect { articles ->
                    _uiState.update { it.copy(articles = articles, isLoading = false) }
                }
        }
    }

    fun filterByCategory(category: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(selectedCategory = category, isLoading = true) }

            val flow = if (category == "All") {
                articleRepository.getAllArticles()
            } else {
                articleRepository.getArticlesByCategory(category)
            }

            flow.collect { articles ->
                _uiState.update { it.copy(articles = articles, isLoading = false) }
            }
        }
    }

    fun filterByDuration(duration: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(selectedDuration = duration, isLoading = true) }

            val flow = when (duration) {
                "Short" -> articleRepository.getArticlesByDuration(3)
                "Medium" -> articleRepository.getArticlesByDuration(7)
                else -> articleRepository.getAllArticles()
            }

            flow.collect { articles ->
                _uiState.update { it.copy(articles = articles, isLoading = false) }
            }
        }
    }
}
