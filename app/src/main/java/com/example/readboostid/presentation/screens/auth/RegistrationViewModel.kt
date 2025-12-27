// File: presentation/screens/auth/RegistrationViewModel.kt
package com.readboost.id.presentation.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.readboost.id.data.model.User
import com.readboost.id.data.service.FirebaseAuthService
import com.readboost.id.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.security.MessageDigest

data class RegistrationUiState(
    val fullName: String = "",
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val fullNameError: String? = null,
    val usernameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val errorMessage: String? = null,
    val isLoading: Boolean = false,
    val registrationSuccess: Boolean = false
)

class RegistrationViewModel(
    private val userRepository: UserRepository,
    private val firebaseAuthService: FirebaseAuthService
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistrationUiState())
    val uiState: StateFlow<RegistrationUiState> = _uiState.asStateFlow()

    fun onFullNameChange(fullName: String) {
        _uiState.value = _uiState.value.copy(
            fullName = fullName,
            fullNameError = null,
            errorMessage = null
        )
    }

    fun onUsernameChange(username: String) {
        _uiState.value = _uiState.value.copy(
            username = username,
            usernameError = null,
            errorMessage = null
        )
    }

    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(
            email = email,
            emailError = null,
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

    fun onConfirmPasswordChange(confirmPassword: String) {
        _uiState.value = _uiState.value.copy(
            confirmPassword = confirmPassword,
            confirmPasswordError = null,
            errorMessage = null
        )
    }

    fun register() {
        val currentState = _uiState.value

        // Validation
        var hasError = false
        var fullNameError: String? = null
        var usernameError: String? = null
        var emailError: String? = null
        var passwordError: String? = null
        var confirmPasswordError: String? = null

        if (currentState.fullName.isBlank()) {
            fullNameError = "Nama lengkap tidak boleh kosong"
            hasError = true
        }

        if (currentState.username.isBlank()) {
            usernameError = "Username tidak boleh kosong"
            hasError = true
        } else if (currentState.username.length < 3) {
            usernameError = "Username minimal 3 karakter"
            hasError = true
        }

        if (currentState.email.isBlank()) {
            emailError = "Email tidak boleh kosong"
            hasError = true
        } else if (!isValidEmail(currentState.email)) {
            emailError = "Format email tidak valid"
            hasError = true
        }

        if (currentState.password.isBlank()) {
            passwordError = "Password tidak boleh kosong"
            hasError = true
        } else if (currentState.password.length < 6) {
            passwordError = "Password minimal 6 karakter"
            hasError = true
        }

        if (currentState.confirmPassword.isBlank()) {
            confirmPasswordError = "Konfirmasi password tidak boleh kosong"
            hasError = true
        } else if (currentState.password != currentState.confirmPassword) {
            confirmPasswordError = "Password tidak cocok"
            hasError = true
        }

        if (hasError) {
            _uiState.value = currentState.copy(
                fullNameError = fullNameError,
                usernameError = usernameError,
                emailError = emailError,
                passwordError = passwordError,
                confirmPasswordError = confirmPasswordError
            )
            return
        }

        // Start registration process
        _uiState.value = currentState.copy(
            isLoading = true,
            errorMessage = null
        )

        viewModelScope.launch {
            try {
                // Step 1: Register with Firebase Auth
                val firebaseResult = firebaseAuthService.createUserWithEmailAndPassword(
                    email = currentState.email,
                    password = currentState.password,
                    displayName = currentState.fullName
                )

                firebaseResult.onSuccess { firebaseUser ->
                    try {
                        // Step 2: Save user data to local database for offline access
                        // Check if username is already taken locally
                val usernameTakenResult = userRepository.isUsernameTaken(currentState.username)
                usernameTakenResult.onSuccess { isTaken ->
                    if (isTaken) {
                                // Try to delete Firebase user if local username is taken
                                firebaseAuthService.signOut()
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            usernameError = "Username sudah digunakan"
                        )
                                return@onSuccess
                    }
                }.onFailure { exception ->
                            // Continue with registration even if local check fails
                            // Firebase registration already succeeded
                        }

                        // Create local user record (password not needed since Firebase handles auth)
                        val passwordHash = hashPassword(currentState.password) // Keep for potential offline fallback
                val user = User(
                    username = currentState.username,
                    email = currentState.email,
                            passwordHash = passwordHash, // Store for potential offline access
                    fullName = currentState.fullName
                )

                        val localRegisterResult = userRepository.registerUser(user)
                        localRegisterResult.onSuccess { userId ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        registrationSuccess = true,
                        errorMessage = null
                    )
                        }.onFailure { exception ->
                            // Firebase registration succeeded but local failed
                            // Still consider registration successful since Firebase auth works
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                registrationSuccess = true,
                                errorMessage = "Registrasi berhasil, namun ada masalah penyimpanan lokal: ${exception.message}"
                            )
                        }
                    } catch (e: Exception) {
                        // Firebase registration succeeded but local database failed
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            registrationSuccess = true,
                            errorMessage = "Registrasi berhasil, namun ada masalah penyimpanan lokal: ${e.message}"
                        )
                    }
                }.onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Gagal mendaftarkan akun Firebase: ${exception.message}"
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

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        return emailRegex.matches(email)
    }

    private fun hashPassword(password: String): String {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }
}
