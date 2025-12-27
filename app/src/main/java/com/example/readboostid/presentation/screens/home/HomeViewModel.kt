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
    val isLoading: Boolean = true,
    val searchQuery: String = ""
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
            articleRepository.getAllArticles().collect { articles ->
                _uiState.update {
                    it.copy(
                        allArticles = articles,
                        newCollectionArticles = articles.take(5),
                        isLoading = false
                    )
                }
                // Initial filter
                filterArticles()
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        filterArticles()
    }

    fun filterByCategory(category: String) {
        _uiState.update { it.copy(selectedCategory = category, searchQuery = "") } // Reset search query when category changes
        filterArticles()
    }

    private fun filterArticles() {
        val state = _uiState.value
        val articlesToFilter = state.allArticles

        val categoryFiltered = if (state.selectedCategory == "Populer") {
            articlesToFilter
        } else {
            articlesToFilter.filter { it.category.equals(state.selectedCategory, ignoreCase = true) }
        }

        val searchFiltered = if (state.searchQuery.isBlank()) {
            categoryFiltered
        } else {
            categoryFiltered.filter { 
                it.title.contains(state.searchQuery, ignoreCase = true) || 
                it.content.contains(state.searchQuery, ignoreCase = true)
            }
        }

        _uiState.update { it.copy(filteredArticles = searchFiltered) }
    }
}
