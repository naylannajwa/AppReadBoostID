// File: presentation/screens/notes/NotesViewModel.kt
package com.readboost.id.presentation.screens.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.readboost.id.data.model.Notes
import com.readboost.id.domain.repository.UserDataRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class NotesUiState(
    val notes: List<Notes> = emptyList(),
    val isLoading: Boolean = true
)

class NotesViewModel(
    private val userDataRepository: UserDataRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotesUiState())
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    init {
        loadNotes()
    }

    private fun loadNotes() {
        viewModelScope.launch {
            userDataRepository.getAllNotes()
                .collect { notes ->
                    _uiState.update { it.copy(notes = notes, isLoading = false) }
                }
        }
    }

    fun deleteNote(note: Notes) {
        viewModelScope.launch {
            userDataRepository.deleteNote(note)
        }
    }
}
