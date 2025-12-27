// File: presentation/screens/profile/ProfileViewModel.kt
package com.readboost.id.presentation.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.readboost.id.data.model.UserProgress
import com.readboost.id.data.service.HybridDataManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ProfileUiState(
    val userProgress: UserProgress? = null,
    val isLoading: Boolean = true
)

class ProfileViewModel(
    private val hybridDataManager: HybridDataManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProgress()
    }

    private fun loadUserProgress() {
        viewModelScope.launch {
            try {
                // Get user progress through hybrid manager
                val progress = hybridDataManager.getUserProgress()
                _uiState.update { it.copy(userProgress = progress ?: UserProgress(), isLoading = false) }
                println("ProfileViewModel: Loaded user progress - XP: ${progress?.totalXP}, Daily XP: ${progress?.dailyXPEarned}, Daily Minutes: ${progress?.dailyReadingMinutes}/${progress?.dailyTarget}, Streak: ${progress?.streakDays}")
            } catch (e: Exception) {
                println("ProfileViewModel: Error loading progress: ${e.message}")
                // Provide default progress if loading fails
                _uiState.update { it.copy(userProgress = UserProgress(), isLoading = false) }
            }
        }
    }

    fun updateDailyTarget(target: Int) {
        viewModelScope.launch {
            try {
                hybridDataManager.updateDailyTarget(target)
                // Reload progress after update
                loadUserProgress()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun refreshData() {
        println("ProfileViewModel: Manual data refresh requested")
        loadUserProgress()
    }

    fun forceRefreshData() {
        println("ProfileViewModel: Force refresh requested - clearing cache and reloading")
        viewModelScope.launch {
            try {
                // Force sync from Firestore
                hybridDataManager.syncUserProgressFromFirestore()
                loadUserProgress()
            } catch (e: Exception) {
                println("ProfileViewModel: Error force refreshing: ${e.message}")
                loadUserProgress()
            }
        }
    }
}