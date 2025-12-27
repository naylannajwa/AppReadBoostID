// File: presentation/navigation/NavGraph.kt
package com.readboost.id.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.readboostid.presentation.screens.admin.AddArticleScreen
import com.readboost.id.presentation.screens.splash.SplashScreen
import com.readboost.id.presentation.screens.welcome.WelcomeScreen
import com.readboost.id.presentation.screens.auth.LoginScreen
import com.readboost.id.presentation.screens.auth.RegistrationScreen
import com.readboost.id.presentation.screens.admin.AdminAuthScreen
import com.readboost.id.presentation.screens.admin.AdminDashboardScreen
import com.readboost.id.presentation.screens.admin.AdminScreen
import com.readboost.id.presentation.screens.admin.EditArticleScreen
import com.readboost.id.presentation.screens.home.HomeScreen
import com.readboost.id.presentation.screens.article.ArticleListScreen
import com.readboost.id.presentation.screens.article.ArticleDetailScreen
import com.readboost.id.presentation.screens.notes.NotesScreen
import com.readboost.id.presentation.screens.notes.NoteDetailScreen
import com.readboost.id.presentation.screens.profile.ProfileScreen
import com.readboost.id.presentation.screens.leaderboard.LeaderboardScreen
import com.readboost.id.presentation.screens.settings.SettingsScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Welcome.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { 
                            inclusive = true 
                        }
                    }
                }
            )
        }

        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onNavigateToUserLogin = {
                    navController.navigate(Screen.Login.route)
                },
                onNavigateToAdminLogin = {
                    navController.navigate(Screen.AdminAuth.route)
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) {
                            inclusive = true
                        }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Registration.route)
                }
            )
        }

        composable(Screen.Registration.route) {
            RegistrationScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Registration.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToArticleList = {
                    navController.navigate(Screen.ArticleList.route)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onNavigateToLeaderboard = {
                    navController.navigate(Screen.Leaderboard.route)
                },
                onNavigateToArticle = { articleId ->
                    navController.navigate(Screen.ArticleDetail.createRoute(articleId))
                },
                onLogout = {
                    // Clear user session and navigate to welcome
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(0) // Clear entire back stack
                    }
                }
            )
        }

        composable(Screen.ArticleList.route) {
            ArticleListScreen(
                onNavigateBack = { navController.navigateUp() },
                onArticleClick = { articleId ->
                    navController.navigate(Screen.ArticleDetail.createRoute(articleId))
                }
            )
        }

        composable(
            route = Screen.ArticleDetail.route,
            arguments = listOf(
                navArgument("articleId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val articleId = backStackEntry.arguments?.getInt("articleId") ?: return@composable
            ArticleDetailScreen(
                articleId = articleId,
                onNavigateBack = { navController.navigateUp() }
            )
        }

        composable(Screen.Notes.route) {
            NotesScreen(
                onNavigateBack = { navController.navigateUp() },
                onNoteClick = { noteId ->
                    navController.navigate(Screen.NoteDetail.createRoute(noteId))
                }
            )
        }

        composable(
            route = Screen.NoteDetail.route,
            arguments = listOf(
                navArgument("noteId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getInt("noteId") ?: return@composable
            NoteDetailScreen(
                noteId = noteId,
                onNavigateBack = { navController.navigateUp() }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateBack = { navController.navigateUp() },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToNotes = {
                    navController.navigate(Screen.Notes.route)
                }
            )
        }

        composable(Screen.Leaderboard.route) {
            LeaderboardScreen(
                onNavigateBack = { navController.navigateUp() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.navigateUp() }
            )
        }

        composable(Screen.AdminAuth.route) {
            AdminAuthScreen(
                onNavigateBack = { navController.navigateUp() },
                onNavigateToAdminPanel = {
                    navController.navigate(Screen.AdminDashboard.route) {
                        popUpTo(Screen.AdminAuth.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(Screen.AdminDashboard.route) {
            AdminDashboardScreen(
                onNavigateToArticleManagement = {
                    navController.navigate(Screen.Admin.route)
                },
                onNavigateToUserLogin = {
                    navController.navigate(Screen.Login.route)
                },
                onLogout = {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(0) // Clear entire back stack
                    }
                }
            )
        }

        composable(Screen.Admin.route) {
            AdminScreen(
                onNavigateBack = {
                    navController.navigate(Screen.AdminDashboard.route) {
                        popUpTo(Screen.AdminDashboard.route) {
                            inclusive = true
                        }
                    }
                },
                onNavigateToAddArticle = {
                    navController.navigate(Screen.AddArticle.route)
                },
                onNavigateToEditArticle = { articleId ->
                    navController.navigate(Screen.EditArticle.createRoute(articleId))
                },
                onLogout = {
                    // Clear admin session and navigate to welcome
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(0) // Clear entire back stack
                    }
                }
            )
        }

        composable(Screen.AddArticle.route) {
            AddArticleScreen(
                onNavigateBack = { navController.navigateUp() },
                onArticleAdded = {
                    navController.navigate(Screen.Admin.route) {
                        popUpTo(Screen.Admin.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(
            route = Screen.EditArticle.route,
            arguments = listOf(
                navArgument("articleId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val articleId = backStackEntry.arguments?.getInt("articleId") ?: return@composable
            EditArticleScreen(
                articleId = articleId,
                onNavigateBack = { navController.navigateUp() },
                onArticleUpdated = {
                    navController.navigate(Screen.Admin.route) {
                        popUpTo(Screen.Admin.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }
    }
}