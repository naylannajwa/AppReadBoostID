// File: presentation/screens/profile/ProfileViewModel.kt
package com.readboost.id.presentation.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.readboost.id.data.model.UserProgress
import com.readboost.id.domain.repository.UserDataRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ProfileUiState(
    val userProgress: UserProgress? = null,
    val isLoading: Boolean = true
)

class ProfileViewModel(
    private val userDataRepository: UserDataRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProgress()
    }

    private fun loadUserProgress() {
        viewModelScope.launch {
            userDataRepository.getUserProgress()
                .collect { progress ->
                    _uiState.update { it.copy(userProgress = progress, isLoading = false) }
                }
        }
    }

    fun updateDailyTarget(target: Int) {
        viewModelScope.launch {
            userDataRepository.updateDailyTarget(target)
        }
    }
}