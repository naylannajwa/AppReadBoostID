// File: data/repository/UserRepositoryImpl.kt
package com.readboost.id.data.repository

import com.readboost.id.data.local.dao.UserDao
import com.readboost.id.data.model.User
import com.readboost.id.domain.repository.UserRepository

class UserRepositoryImpl(
    private val userDao: UserDao
) : UserRepository {

    override suspend fun registerUser(user: User): Result<Long> {
        return try {
            val userId = userDao.insertUser(user)
            Result.success(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun loginUser(username: String, passwordHash: String): Result<User?> {
        return try {
            val user = userDao.authenticateUser(username, passwordHash)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserById(userId: Int): Result<User?> {
        return try {
            val user = userDao.getUserById(userId)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun isUsernameTaken(username: String): Result<Boolean> {
        return try {
            val count = userDao.isUsernameTaken(username)
            Result.success(count > 0)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun isEmailTaken(email: String): Result<Boolean> {
        return try {
            val count = userDao.isEmailTaken(email)
            Result.success(count > 0)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUser(user: User): Result<Unit> {
        return try {
            userDao.updateUser(user)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
