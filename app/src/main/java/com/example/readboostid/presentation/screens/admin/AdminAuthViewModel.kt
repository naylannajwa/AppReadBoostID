// File: presentation/screens/admin/AdminAuthViewModel.kt
package com.readboost.id.presentation.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.readboost.id.data.local.UserPreferences
import com.readboost.id.data.model.AdminUser
import com.readboost.id.data.model.CurrentUser
import com.readboost.id.domain.repository.ArticleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.security.MessageDigest

data class AdminAuthUiState(
    val username: String = "",
    val password: String = "",
    val usernameError: String? = null,
    val passwordError: String? = null,
    val errorMessage: String? = null,
    val isLoading: Boolean = false,
    val loginSuccess: Boolean = false
)

class AdminAuthViewModel(
    private val articleRepository: ArticleRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminAuthUiState())
    val uiState: StateFlow<AdminAuthUiState> = _uiState.asStateFlow()

    companion object {
        // In-memory storage for demo (in real app, use secure AdminUser table)
        private val adminCredentials = mutableMapOf<String, String>()

        init {
            // Initialize with default admin
            adminCredentials["admin"] = "admin123"
            adminCredentials["superadmin"] = "super123"
        }

        fun isValidAdmin(username: String, password: String): Boolean {
            return adminCredentials[username] == password
        }

        fun getAdminName(username: String): String {
            return when (username) {
                "admin" -> "Administrator"
                "superadmin" -> "Super Admin"
                else -> "Admin"
            }
        }
    }

    fun onUsernameChange(username: String) {
        _uiState.value = _uiState.value.copy(
            username = username,
            usernameError = null,
            errorMessage = null
        )
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(
            password = password,
            passwordError = null,
            errorMessage = null
        )
    }

    fun login() {
        val currentState = _uiState.value

        // Validation
        var hasError = false
        var usernameError: String? = null
        var passwordError: String? = null

        if (currentState.username.isBlank()) {
            usernameError = "Username admin tidak boleh kosong"
            hasError = true
        }

        if (currentState.password.isBlank()) {
            passwordError = "Password admin tidak boleh kosong"
            hasError = true
        }

        if (hasError) {
            _uiState.value = currentState.copy(
                usernameError = usernameError,
                passwordError = passwordError
            )
            return
        }

        // Start login process
        _uiState.value = currentState.copy(
            isLoading = true,
            errorMessage = null
        )

        viewModelScope.launch {
            try {
                // Check admin credentials using shared validation
                if (AdminAuthViewModel.isValidAdmin(currentState.username, currentState.password)) {
                    // Save admin session data
                    val adminName = AdminAuthViewModel.getAdminName(currentState.username)
                    val currentAdmin = CurrentUser(
                        id = 1, // Admin ID
                        username = currentState.username,
                        fullName = adminName,
                        email = "${currentState.username}@admin.com",
                        role = "admin"
                    )
                    userPreferences.saveCurrentUser(currentAdmin)

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        loginSuccess = true,
                        errorMessage = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Username atau password admin salah"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Terjadi kesalahan: ${e.message}"
                )
            }
        }
    }

    private fun hashPassword(password: String): String {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }
}
