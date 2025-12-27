// File: presentation/screens/profile/ProfileViewModel.kt
package com.readboost.id.presentation.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.readboost.id.data.model.UserProgress
import com.readboost.id.domain.repository.FirestoreLeaderboardRepository
import com.readboost.id.domain.repository.UserDataRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ProfileUiState(
    val userProgress: UserProgress? = null,
    val isLoading: Boolean = true
)

class ProfileViewModel(
    private val userDataRepository: UserDataRepository,
    private val firestoreRepository: FirestoreLeaderboardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProgress()
    }

    private fun loadUserProgress() {
        viewModelScope.launch {
            // Try Firestore first, fallback to local if needed
            try {
                // Assuming we have a userId - in real app, get from FirebaseAuth
                val userId = "current_user_id" // TODO: Get from FirebaseAuth
                firestoreRepository.getUserProgressFlow(userId)
                    .collect { progress ->
                        _uiState.update { it.copy(userProgress = progress, isLoading = false) }
                    }
            } catch (e: Exception) {
                // Fallback to local database
                userDataRepository.getUserProgress()
                    .collect { progress ->
                        _uiState.update { it.copy(userProgress = progress, isLoading = false) }
                    }
            }
        }
    }

    fun updateDailyTarget(target: Int) {
        viewModelScope.launch {
            try {
                val userId = "current_user_id" // TODO: Get from FirebaseAuth
                firestoreRepository.updateUserProgress(userId, mapOf("dailyTarget" to target))
            } catch (e: Exception) {
                // Fallback to local database
                userDataRepository.updateDailyTarget(target)
            }
        }
    }
}