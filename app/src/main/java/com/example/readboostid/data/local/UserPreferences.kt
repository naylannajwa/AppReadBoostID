// File: data/local/UserPreferences.kt
package com.readboost.id.data.local

import android.content.Context
import android.content.SharedPreferences
import com.readboost.id.data.model.CurrentUser

class UserPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveCurrentUser(user: CurrentUser) {
        prefs.edit().apply {
            putInt(KEY_USER_ID, user.id)
            putString(KEY_USERNAME, user.username)
            putString(KEY_FULL_NAME, user.fullName)
            putString(KEY_EMAIL, user.email)
            putString(KEY_ROLE, user.role)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }

    fun getCurrentUser(): CurrentUser? {
        val isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        if (!isLoggedIn) return null

        val id = prefs.getInt(KEY_USER_ID, -1)
        val username = prefs.getString(KEY_USERNAME, "") ?: ""
        val fullName = prefs.getString(KEY_FULL_NAME, "") ?: ""
        val email = prefs.getString(KEY_EMAIL, "") ?: ""
        val role = prefs.getString(KEY_ROLE, "user") ?: "user"

        return if (id != -1 && username.isNotEmpty() && fullName.isNotEmpty()) {
            CurrentUser(id, username, fullName, email, role)
        } else {
            null
        }
    }

    fun logout() {
        prefs.edit().apply {
            clear()
            apply()
        }
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    companion object {
        private const val PREFS_NAME = "user_prefs"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_FULL_NAME = "full_name"
        private const val KEY_EMAIL = "email"
        private const val KEY_ROLE = "role"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }
}
