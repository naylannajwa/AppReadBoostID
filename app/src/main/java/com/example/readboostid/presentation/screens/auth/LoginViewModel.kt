// File: presentation/screens/auth/LoginViewModel.kt
package com.readboost.id.presentation.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.readboost.id.data.local.UserPreferences
import com.readboost.id.data.model.CurrentUser
import com.readboost.id.data.service.FirebaseAuthService
import com.readboost.id.domain.repository.UserRepository
import com.readboost.id.presentation.screens.admin.AdminAuthViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.security.MessageDigest

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val usernameError: String? = null,
    val passwordError: String? = null,
    val errorMessage: String? = null,
    val isLoading: Boolean = false,
    val loginSuccess: Boolean = false
)

class LoginViewModel(
    private val userRepository: UserRepository,
    private val userPreferences: UserPreferences,
    private val firebaseAuthService: FirebaseAuthService
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

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
            usernameError = "Username/Email tidak boleh kosong"
            hasError = true
        }

        if (currentState.password.isBlank()) {
            passwordError = "Password tidak boleh kosong"
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
                val loginResult = when {
                    // Check if it's admin login
                    isAdminLogin(currentState.username) -> {
                        loginAsAdmin(currentState.username, currentState.password)
                    }
                    // Check if username contains @ (email format) - use Firebase Auth
                    currentState.username.contains("@") -> {
                        loginWithFirebase(currentState.username, currentState.password)
                    }
                    // Otherwise, use local database (legacy users)
                    else -> {
                        loginWithLocalDatabase(currentState.username, currentState.password)
                    }
                }

                loginResult.onSuccess { currentUser ->
                        userPreferences.saveCurrentUser(currentUser)
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            loginSuccess = true,
                            errorMessage = null
                        )
                }.onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Login gagal"
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

    private fun isAdminLogin(username: String): Boolean {
        return username == "admin" || username == "superadmin"
    }

    private suspend fun loginAsAdmin(username: String, password: String): Result<CurrentUser> {
        return if (AdminAuthViewModel.isValidAdmin(username, password)) {
            val adminName = AdminAuthViewModel.getAdminName(username)
            val currentUser = CurrentUser(
                id = 1, // Admin ID
                username = username,
                fullName = adminName,
                email = "${username}@admin.com",
                role = "admin"
            )
            Result.success(currentUser)
        } else {
            Result.failure(Exception("Username atau password admin salah"))
        }
    }

    private suspend fun loginWithFirebase(email: String, password: String): Result<CurrentUser> {
        return firebaseAuthService.signInWithEmailAndPassword(email, password).fold(
            onSuccess = { firebaseUser ->
                val currentUser = CurrentUser(
                    id = firebaseUser.uid.hashCode(), // Convert UID to Int for compatibility
                    username = firebaseUser.email ?: email,
                    fullName = firebaseUser.displayName ?: "User",
                    email = firebaseUser.email ?: email,
                    role = "user"
                )
                Result.success(currentUser)
            },
            onFailure = { exception ->
                Result.failure(Exception("Login Firebase gagal: ${exception.message}"))
            }
        )
    }

    private suspend fun loginWithLocalDatabase(username: String, password: String): Result<CurrentUser> {
        val passwordHash = hashPassword(password)
        return userRepository.loginUser(username, passwordHash).fold(
            onSuccess = { user ->
                if (user != null) {
                    val currentUser = CurrentUser(
                        id = user.id,
                        username = user.username,
                        fullName = user.fullName,
                        email = user.email,
                        role = user.role
                    )
                    Result.success(currentUser)
                } else {
                    Result.failure(Exception("Username atau password salah"))
                }
            },
            onFailure = { exception ->
                Result.failure(exception)
            }
        )
    }

    private fun hashPassword(password: String): String {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }
}
