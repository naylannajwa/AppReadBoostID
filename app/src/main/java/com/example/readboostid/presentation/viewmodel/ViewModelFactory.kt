// File: presentation/viewmodel/ViewModelFactory.kt
package com.readboost.id.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.readboost.id.di.AppContainer
import com.readboost.id.presentation.screens.admin.AdminViewModel
import com.readboost.id.presentation.screens.article.ArticleDetailViewModel
import com.readboost.id.presentation.screens.article.ArticleViewModel
import com.readboost.id.presentation.screens.auth.LoginViewModel
import com.readboost.id.presentation.screens.auth.RegistrationViewModel
import com.readboost.id.presentation.screens.home.HomeViewModel
import com.readboost.id.presentation.screens.leaderboard.LeaderboardViewModel
import com.readboost.id.presentation.screens.notes.NotesViewModel
import com.readboost.id.presentation.screens.profile.ProfileViewModel

class ViewModelFactory(
    private val appContainer: AppContainer
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(appContainer.articleRepository, appContainer.userDataRepository) as T
            }
            modelClass.isAssignableFrom(ArticleViewModel::class.java) -> {
                ArticleViewModel(appContainer.articleRepository) as T
            }
            modelClass.isAssignableFrom(NotesViewModel::class.java) -> {
                NotesViewModel(appContainer.userDataRepository) as T
            }
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
                ProfileViewModel(appContainer.hybridDataManager) as T
            }
            modelClass.isAssignableFrom(LeaderboardViewModel::class.java) -> {
                LeaderboardViewModel(appContainer.userDataRepository, appContainer.firestoreLeaderboardRepository) as T
            }
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel(appContainer.userRepository, appContainer.userPreferences, appContainer.firebaseAuthService) as T
            }
            modelClass.isAssignableFrom(RegistrationViewModel::class.java) -> {
                RegistrationViewModel(appContainer.userRepository, appContainer.firebaseAuthService) as T
            }
            modelClass.isAssignableFrom(AdminViewModel::class.java) -> {
                AdminViewModel(appContainer.articleRepository) as T
            }
            modelClass.isAssignableFrom(com.readboost.id.presentation.screens.admin.AdminAuthViewModel::class.java) -> {
                com.readboost.id.presentation.screens.admin.AdminAuthViewModel(appContainer.articleRepository, appContainer.userPreferences) as T
            }
            modelClass.isAssignableFrom(com.readboost.id.presentation.screens.admin.AddArticleViewModel::class.java) -> {
                com.readboost.id.presentation.screens.admin.AddArticleViewModel(appContainer.articleRepository) as T
            }
            modelClass.isAssignableFrom(com.readboost.id.presentation.screens.admin.EditArticleViewModel::class.java) -> {
                com.readboost.id.presentation.screens.admin.EditArticleViewModel(appContainer.articleRepository) as T
            }
            modelClass.isAssignableFrom(com.readboost.id.presentation.screens.admin.AdminDashboardViewModel::class.java) -> {
                com.readboost.id.presentation.screens.admin.AdminDashboardViewModel(appContainer.articleRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

// Special factory for ArticleDetailViewModel
class ArticleDetailViewModelFactory(
    private val appContainer: AppContainer,
    private val articleId: Int
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ArticleDetailViewModel::class.java)) {
            return ArticleDetailViewModel(
                appContainer.articleRepository,
                appContainer.userDataRepository,
                appContainer.hybridDataManager,
                articleId
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}