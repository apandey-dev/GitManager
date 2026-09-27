package com.gitmanager.app.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Dashboard : Screen("dashboard")
    object Repositories : Screen("repositories")
    object RepoDetail : Screen("repo_detail/{owner}/{repo}") {
        fun createRoute(owner: String, repo: String) = "repo_detail/$owner/$repo"
    }
    object UserSearch : Screen("user_search")
    object UserProfile : Screen("user_profile/{username}") {
        fun createRoute(username: String) = "user_profile/$username"
    }
    object Settings : Screen("settings")
}
