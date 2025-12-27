// File: domain/repository/FirebaseAuthRepository.kt
package com.readboost.id.domain.repository

import com.google.firebase.auth.FirebaseUser
import com.readboost.id.data.model.User

interface FirebaseAuthRepository {
    suspend fun signInWithEmailAndPassword(email: String, password: String): Result<FirebaseUser>
    suspend fun createUserWithEmailAndPassword(email: String, password: String, username: String, fullName: String): Result<FirebaseUser>
    suspend fun signOut()
    fun getCurrentUser(): FirebaseUser?
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    suspend fun updateUserProfile(fullName: String): Result<Unit>
}
