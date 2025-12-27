// File: presentation/screens/leaderboard/LeaderboardViewModel.kt
package com.readboost.id.presentation.screens.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.readboost.id.data.model.Leaderboard
import com.readboost.id.data.service.DummyDataGenerator
import com.readboost.id.domain.repository.FirestoreLeaderboardRepository
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
    private val userDataRepository: UserDataRepository,
    private val firestoreRepository: FirestoreLeaderboardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LeaderboardUiState())
    val uiState: StateFlow<LeaderboardUiState> = _uiState.asStateFlow()

    // Store previous ranks for comparison (to show up/down arrows)
    private var previousRanks: Map<Int, Int> = emptyMap()

    init {
        loadLeaderboard()
    }

    fun loadLeaderboard() {
        viewModelScope.launch {
            try {
                println("LeaderboardViewModel: Loading leaderboard from Firestore...")

                // Load both weekly and all-time data
                val allTimeData = firestoreRepository.getAllTimeLeaderboard(10)
                val weeklyData = firestoreRepository.getWeeklyLeaderboard(10)

                println("LeaderboardViewModel: Loaded ${allTimeData.size} all-time entries")
                println("LeaderboardViewModel: Loaded ${weeklyData.size} weekly entries")

                // If no data found, try to generate dummy data
                if (allTimeData.isEmpty() || weeklyData.isEmpty()) {
                    println("LeaderboardViewModel: No leaderboard data found, generating dummy data...")
                    try {
                        com.readboost.id.data.service.DummyDataGenerator.generateDummyLeaderboardData()
                        // Reload data after generation
                        val newAllTimeData = firestoreRepository.getAllTimeLeaderboard(50)
                        val newWeeklyData = firestoreRepository.getWeeklyLeaderboard(50)
                        println("LeaderboardViewModel: After generation - ${newAllTimeData.size} all-time, ${newWeeklyData.size} weekly entries")

                        // Update with new data
                        _uiState.update {
                            it.copy(
                                leaderboard = if (it.selectedFilter == TimeFilter.Weekly) newWeeklyData else newAllTimeData,
                                weeklyLeaderboard = newWeeklyData,
                                allTimeLeaderboard = newAllTimeData,
                                isLoading = false
                            )
                        }
                        return@launch
                    } catch (e: Exception) {
                        println("LeaderboardViewModel: Failed to generate dummy data: ${e.message}")
                    }
                }

                allTimeData.forEach { entry ->
                    println("LeaderboardViewModel: All-time - ${entry.username}: ${entry.totalXP} XP (Rank: ${entry.rank})")
                }

                // Calculate rank changes for current filter
                val currentLeaderboard = if (_uiState.value.selectedFilter == TimeFilter.Weekly) weeklyData else allTimeData
                val currentRanks = currentLeaderboard.associate { it.userId to it.rank }
                val rankChanges = previousRanks.mapValues { (userId, oldRank) ->
                    val newRank = currentRanks[userId] ?: oldRank
                    newRank - oldRank // Negative = improved, Positive = declined
                }
                previousRanks = currentRanks

                _uiState.update {
                    it.copy(
                        leaderboard = currentLeaderboard,
                        weeklyLeaderboard = weeklyData,
                        allTimeLeaderboard = allTimeData,
                        isLoading = false
                    )
                }

                println("LeaderboardViewModel: Leaderboard state updated - showing ${currentLeaderboard.size} entries")

            } catch (e: Exception) {
                println("LeaderboardViewModel: Error loading from Firestore: ${e.message}")
                e.printStackTrace()

                // Fallback to local database
                try {
                    userDataRepository.getAllLeaderboard()
                        .collect { leaderboard ->
                            val sorted = leaderboard.sortedByDescending { it.totalXP }
                                .mapIndexed { index, entry -> entry.copy(rank = index + 1) }

                            _uiState.update {
                                it.copy(
                                    leaderboard = sorted,
                                    weeklyLeaderboard = sorted,
                                    allTimeLeaderboard = sorted,
                                    isLoading = false
                                )
                            }

                            println("LeaderboardViewModel: Fallback to local database - ${sorted.size} entries")
                        }
                } catch (localException: Exception) {
                    println("LeaderboardViewModel: Error loading from local database: ${localException.message}")
                    _uiState.update { it.copy(isLoading = false) }
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

    // Method untuk testing - force refresh dengan dummy data
    fun refreshWithDummyData() {
        viewModelScope.launch {
            try {
                println("LeaderboardViewModel: Refreshing with dummy data...")
                _uiState.update { it.copy(isLoading = true) }

                // Force regenerate dummy data
                DummyDataGenerator.forceRegenerateDummyData()

                // Reload leaderboard
                loadLeaderboard()

                println("LeaderboardViewModel: Dummy data refresh completed")
            } catch (e: Exception) {
                println("LeaderboardViewModel: Error refreshing dummy data: ${e.message}")
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun forceRefreshLeaderboard() {
        println("LeaderboardViewModel: Force refresh leaderboard requested")
        refreshWithDummyData() // Force regenerate dummy data first
    }
}