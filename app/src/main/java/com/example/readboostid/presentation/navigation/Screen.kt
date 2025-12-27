// File: presentation/navigation/Screen.kt
package com.readboost.id.presentation.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Welcome : Screen("welcome")
    object Login : Screen("login")
    object Registration : Screen("registration")
    object Home : Screen("home")
    object ArticleList : Screen("article_list")
    object ArticleDetail : Screen("article_detail/{articleId}") {
        fun createRoute(articleId: Int) = "article_detail/$articleId"
    }
    object Notes : Screen("notes")
    object NoteDetail : Screen("note_detail/{noteId}") {
        fun createRoute(noteId: Int) = "note_detail/$noteId"
    }
    object Profile : Screen("profile")
    object Leaderboard : Screen("leaderboard")
    object Settings : Screen("settings")
    object AdminAuth : Screen("admin_auth")
    object AdminDashboard : Screen("admin_dashboard")
    object Admin : Screen("admin")
    object AddArticle : Screen("add_article")
    object EditArticle : Screen("edit_article/{articleId}") {
        fun createRoute(articleId: Int) = "edit_article/$articleId"
    }
}