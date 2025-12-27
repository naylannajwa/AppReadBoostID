// File: data/model/User.kt
package com.readboost.id.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val username: String,
    val email: String,
    val passwordHash: String,
    val fullName: String,
    val role: String = "user", // "user" or "admin"
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)

// Data class for current logged in user (stored in preferences)
data class CurrentUser(
    val id: Int,
    val username: String,
    val fullName: String,
    val email: String,
    val role: String = "user"
)
