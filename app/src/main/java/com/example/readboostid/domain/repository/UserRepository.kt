// File: domain/repository/UserRepository.kt
package com.readboost.id.domain.repository

import com.readboost.id.data.model.User

interface UserRepository {
    suspend fun registerUser(user: User): Result<Long>
    suspend fun loginUser(username: String, passwordHash: String): Result<User?>
    suspend fun getUserById(userId: Int): Result<User?>
    suspend fun isUsernameTaken(username: String): Result<Boolean>
    suspend fun isEmailTaken(email: String): Result<Boolean>
    suspend fun updateUser(user: User): Result<Unit>
}
