// File: data/service/FirebaseAuthService.kt
package com.readboost.id.data.service

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class FirebaseAuthService {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    /**
     * Sign in dengan email dan password
     */
    suspend fun signInWithEmailAndPassword(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            result.user?.let { user ->
                Result.success(user)
            } ?: Result.failure(Exception("Login gagal: User tidak ditemukan"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Daftar user baru dengan email dan password
     */
    suspend fun createUserWithEmailAndPassword(
        email: String,
        password: String,
        displayName: String
    ): Result<FirebaseUser> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let { user ->
                // Update display name
                val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .build()

                user.updateProfile(profileUpdates).await()
                Result.success(user)
            } ?: Result.failure(Exception("Registrasi gagal: User tidak ditemukan"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sign out user
     */
    fun signOut() {
        firebaseAuth.signOut()
    }

    /**
     * Get current user yang sedang login
     */
    fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    /**
     * Kirim email reset password
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update profile user
     */
    suspend fun updateUserProfile(displayName: String): Result<Unit> {
        return try {
            val currentUser = firebaseAuth.currentUser
                ?: return Result.failure(Exception("Tidak ada user yang sedang login"))

            val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build()

            currentUser.updateProfile(profileUpdates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Check apakah user sedang login
     */
    fun isUserLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    /**
     * Reload current user data dari server
     */
    suspend fun reloadCurrentUser(): Result<Unit> {
        return try {
            firebaseAuth.currentUser?.reload()?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
