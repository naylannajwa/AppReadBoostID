// File: presentation/screens/admin/AdminDashboardViewModel.kt
package com.readboost.id.presentation.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.readboost.id.domain.repository.ArticleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar

data class AdminDashboardUiState(
    val totalArticles: Int = 0,
    val articlesByGenre: Map<String, Int> = emptyMap(),
    val isLoading: Boolean = false
)

class AdminDashboardViewModel(
    private val articleRepository: ArticleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminDashboardUiState())
    val uiState: StateFlow<AdminDashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardStats()
    }

    private fun loadDashboardStats() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                articleRepository.getAllArticles().collectLatest { articles ->
                    val totalArticles = articles.size

                    // Count articles by genre - handle empty/null categories
                    val articlesByGenre = articles
                        .filter { !it.category.isNullOrBlank() }
                        .groupBy { it.category }
                        .mapValues { it.value.size }

                    _uiState.value = _uiState.value.copy(
                        totalArticles = totalArticles,
                        articlesByGenre = articlesByGenre,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                // Log the error and set empty state
                println("AdminDashboardViewModel Error: ${e.message}")
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(
                    totalArticles = 0,
                    articlesByGenre = emptyMap(),
                    isLoading = false
                )
            }
        }
    }

    fun refreshStats() {
        loadDashboardStats()
    }
}
