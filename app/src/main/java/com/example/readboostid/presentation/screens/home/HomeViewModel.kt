// File: presentation/screens/home/HomeViewModel.kt
package com.readboost.id.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.readboost.id.data.model.Article
import com.readboost.id.data.model.UserProgress
import com.readboost.id.domain.repository.ArticleRepository
import com.readboost.id.domain.repository.UserDataRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HomeUiState(
    val allArticles: List<Article> = emptyList(),
    val newCollectionArticles: List<Article> = emptyList(),
    val filteredArticles: List<Article> = emptyList(),
    val selectedCategory: String = "Populer",
    val userProgress: UserProgress? = null,
    val isLoading: Boolean = true
)

class HomeViewModel(
    private val articleRepository: ArticleRepository,
    private val userDataRepository: UserDataRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                articleRepository.getAllArticles(),
                userDataRepository.getUserProgress()
            ) { articles, progress ->
                val newCollection = articles.take(5) // 5 artikel terbaru untuk New Collection
                val filtered = filterArticlesByCategory(articles, "Populer")
                
                HomeUiState(
                    allArticles = articles,
                    newCollectionArticles = newCollection,
                    filteredArticles = filtered,
                    selectedCategory = "Populer",
                    userProgress = progress,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun filterByCategory(category: String) {
        val currentArticles = _uiState.value.allArticles
        if (currentArticles.isEmpty()) {
            // Jika belum ada artikel, load dulu
            viewModelScope.launch {
                val articles = articleRepository.getAllArticles().first()
                val filtered = filterArticlesByCategory(articles, category)
                _uiState.update { 
                    it.copy(
                        allArticles = articles,
                        selectedCategory = category,
                        filteredArticles = filtered
                    ) 
                }
            }
        } else {
            // Filter dari artikel yang sudah ada
            val filtered = filterArticlesByCategory(currentArticles, category)
            _uiState.update { 
                it.copy(
                    selectedCategory = category,
                    filteredArticles = filtered
                ) 
            }
        }
    }

    private fun filterArticlesByCategory(articles: List<Article>, category: String): List<Article> {
        return when (category) {
            "Populer" -> articles.sortedByDescending { it.id }.take(15) // Artikel terbaru sebagai populer
            "Ilmiah" -> articles.filter { 
                it.category.equals("Science", ignoreCase = true) || 
                it.category.equals("Ilmiah", ignoreCase = true) ||
                it.category.equals("Sains", ignoreCase = true)
            }
            "Fantasi" -> articles.filter { 
                it.category.equals("Fantasy", ignoreCase = true) || 
                it.category.equals("Fantasi", ignoreCase = true)
            }
            "Bisnis" -> articles.filter { 
                it.category.equals("Business", ignoreCase = true) || 
                it.category.equals("Bisnis", ignoreCase = true)
            }
            "Teknologi" -> articles.filter { 
                it.category.equals("Technology", ignoreCase = true) || 
                it.category.equals("Teknologi", ignoreCase = true)
            }
            "Seni" -> articles.filter { 
                it.category.equals("Art", ignoreCase = true) || 
                it.category.equals("Seni", ignoreCase = true)
            }
            else -> articles.filter { it.category.equals(category, ignoreCase = true) }
        }
    }
}