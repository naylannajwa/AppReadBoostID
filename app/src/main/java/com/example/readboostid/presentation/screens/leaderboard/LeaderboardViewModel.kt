// File: presentation/screens/leaderboard/LeaderboardViewModel.kt
package com.readboost.id.presentation.screens.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.readboost.id.data.model.Leaderboard
import com.readboost.id.domain.repository.UserDataRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class LeaderboardUiState(
    val leaderboard: List<Leaderboard> = emptyList(),
    val weeklyLeaderboard: List<Leaderboard> = emptyList(),
    val allTimeLeaderboard: List<Leaderboard> = emptyList(),
    val selectedFilter: TimeFilter = TimeFilter.Weekly,
    val isLoading: Boolean = true
)

enum class TimeFilter {
    Weekly, AllTime
}

class LeaderboardViewModel(
    private val userDataRepository: UserDataRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LeaderboardUiState())
    val uiState: StateFlow<LeaderboardUiState> = _uiState.asStateFlow()

    // Store previous ranks for comparison (to show up/down arrows)
    private var previousRanks: Map<Int, Int> = emptyMap()

    init {
        loadLeaderboard()
    }

    private fun loadLeaderboard() {
        viewModelScope.launch {
            userDataRepository.getAllLeaderboard()
                .collect { leaderboard ->
                    // Sort by totalXP descending
                    val sorted = leaderboard.sortedByDescending { it.totalXP }
                        .mapIndexed { index, entry -> entry.copy(rank = index + 1) }
                    
                    // For now, use same data for weekly and all-time
                    // In real app, you would fetch separate weekly data
                    val weeklyData = sorted // TODO: Replace with actual weekly data
                    val allTimeData = sorted
                    
                    // Calculate rank changes
                    val currentRanks = sorted.associate { it.userId to it.rank }
                    val rankChanges = previousRanks.mapValues { (userId, oldRank) ->
                        val newRank = currentRanks[userId] ?: oldRank
                        newRank - oldRank // Negative = improved, Positive = declined
                    }
                    previousRanks = currentRanks
                    
                    _uiState.update { 
                        it.copy(
                            leaderboard = if (it.selectedFilter == TimeFilter.Weekly) weeklyData else allTimeData,
                            weeklyLeaderboard = weeklyData,
                            allTimeLeaderboard = allTimeData,
                            isLoading = false
                        ) 
                    }
                }
        }
    }

    fun setTimeFilter(filter: TimeFilter) {
        val leaderboard = when (filter) {
            TimeFilter.Weekly -> _uiState.value.weeklyLeaderboard
            TimeFilter.AllTime -> _uiState.value.allTimeLeaderboard
        }
        _uiState.update { 
            it.copy(
                selectedFilter = filter,
                leaderboard = leaderboard
            ) 
        }
    }
}