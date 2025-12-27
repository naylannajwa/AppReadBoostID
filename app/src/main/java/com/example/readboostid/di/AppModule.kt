// File: di/AppModule.kt
package com.readboost.id.di

import android.content.Context
import com.readboost.id.data.local.database.AppDatabase
import com.readboost.id.domain.repository.ArticleRepository
import com.readboost.id.domain.repository.UserDataRepository

object AppModule {
    fun provideDatabase(context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    fun provideArticleRepository(context: Context): ArticleRepository {
        val database = provideDatabase(context)
        return ArticleRepository(database.articleDao())
    }

    fun provideUserDataRepository(context: Context): UserDataRepository {
        val database = provideDatabase(context)
        return UserDataRepository(
            database.notesDao(),
            database.userProgressDao(),
            database.leaderboardDao()
        )
    }
}